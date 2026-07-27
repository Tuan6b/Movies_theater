package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.dao.BookingConflictException;
import com.cinema.dao.EmployeeDAO;
import com.cinema.dao.PromotionDAO;
import com.cinema.dao.RoomDAO;
import com.cinema.dao.SeatDAO;
import com.cinema.dao.ShiftExchangeDAO;
import com.cinema.dao.TicketDAO;
import com.cinema.dao.WorkShiftDAO;
import com.cinema.dao.tbMovie;
import com.cinema.dao.tbSchedule;
import com.cinema.model.Account;
import com.cinema.model.Notification;
import com.cinema.model.Promotion;
import com.cinema.model.Room;
import com.cinema.model.Seat;
import com.cinema.model.ShiftExchangeRequest;
import com.cinema.model.Ticket;
import com.cinema.model.WorkShift;
import com.cinema.model.clsMovie;
import com.cinema.model.clsSchedule;
import com.cinema.service.NotificationService;
import com.cinema.util.DBUtils;
import com.cinema.util.PasswordHash;
import com.cinema.util.SeatPricing;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EmployeeDashboardServlet extends HttpServlet {

    private static final String DASHBOARD_JSP  = "/view/employee/dashboard.jsp";
    private static final String SCHEDULES_JSP  = "/view/employee/schedules.jsp";
    private static final String TICKETS_JSP    = "/view/employee/tickets.jsp";
    private static final String BOOK_JSP       = "/view/employee/book.jsp";
    private static final String CHECKIN_JSP    = "/view/employee/checkin.jsp";
    private static final String PROFILE_JSP    = "/view/employee/profile.jsp";
    private static final String SETUP_JSP      = "/view/employee/setup.jsp";
    private static final String MY_SHIFTS_JSP  = "/view/employee/my-shifts.jsp";

    // Shared by the check-in list and the single-code lookup: both need the same
    // columns, so the SELECT/FROM half lives here and each caller appends its own
    // WHERE. Movie has to be joined explicitly — Schedule only carries MovieID.
    private static final String CHECKIN_SELECT = """
        SELECT t.TicketID, t.Code, t.IsCheckedIn, t.PriceAtBooking,
               u.FullName AS CustomerName,
               m.MovieName,
               CAST(sc.StartTime AS DATE) AS ShowDate,
               CONVERT(VARCHAR(5), sc.StartTime, 108) AS StartTime,
               s.RowChar + CAST(s.ColNumber AS VARCHAR) AS SeatName
        FROM Ticket t
        INNER JOIN Schedule sc ON t.ScheduleID = sc.ScheduleID
        INNER JOIN Movie m ON sc.MovieID = m.MovieID
        INNER JOIN Seat s ON t.SeatID = s.SeatID
        INNER JOIN Invoice i ON t.InvoiceID = i.InvoiceID
        INNER JOIN Account a ON i.AccountID = a.AccountID
        LEFT JOIN UserProfile u ON a.AccountID = u.AccountID
        """;

    private final AccountDAO      accountDAO      = new AccountDAO();
    private final EmployeeDAO     employeeDAO     = new EmployeeDAO();
    private final WorkShiftDAO    shiftDAO        = new WorkShiftDAO();
    private final ShiftExchangeDAO exchangeDAO    = new ShiftExchangeDAO();
    private final NotificationService notificationService = new NotificationService();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String path = request.getPathInfo();
        if (path == null) {
            path = "/";
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            transferFlash(session, request, "flashSuccess");
            transferFlash(session, request, "flashError");
            transferFlash(session, request, "flashNewCodes");
        }

        Account sessionAcc = (session != null) ? (Account) session.getAttribute("account") : null;

        // First-login guard: redirect to setup until profile is completed
        if (sessionAcc != null && sessionAcc.isNeedsSetup() && !"/setup".equals(path)) {
            response.sendRedirect(request.getContextPath() + "/employee/setup");
            return;
        }

        // Off-shift guard: block operational paths if employee has no active shift
        boolean noShift = (session != null && Boolean.TRUE.equals(session.getAttribute("noShift")));
        if (noShift && ("/book".equals(path) || "/checkin".equals(path))) {
            session.setAttribute("flashError", "Bạn không có ca làm việc. Chức năng này chỉ khả dụng trong giờ làm.");
            response.sendRedirect(request.getContextPath() + "/employee");
            return;
        }

        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method)) {
            switch (path) {
                case "/":
                case "/dashboard":
                    showDashboard(request, response, noShift);
                    break;
                case "/schedules":
                    showSchedules(request, response);
                    break;
                case "/tickets":
                    showTickets(request, response);
                    break;
                case "/book":
                    showBookForm(request, response);
                    break;
                case "/checkin":
                    showCheckin(request, response);
                    break;
                case "/profile":
                    showProfile(request, response);
                    break;
                case "/setup":
                    showSetup(request, response);
                    break;
                case "/my-shifts":
                    showMyShifts(request, response);
                    break;
                case "/notifications":
                    handleNotificationsGet(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        } else if ("POST".equalsIgnoreCase(method)) {
            switch (path) {
                case "/book":
                    handleBook(request, response);
                    break;
                case "/checkin":
                    if ("checkin_by_code".equals(request.getParameter("action"))) {
                        handleCheckinByCode(request, response);
                    } else {
                        handleCheckin(request, response);
                    }
                    break;
                case "/setup":
                    handleSetup(request, response);
                    break;
                case "/my-shifts":
                    handleMyShiftsPost(request, response);
                    break;
                case "/notifications":
                    handleNotificationsPost(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        }
    }

    /** How far ahead the dashboard looks when counting tickets still to be scanned. */
    private static final int UPCOMING_WINDOW_HOURS = 2;

    /** Payment methods rung up at the desk; everything else counts as paid online. */
    private static final List<String> COUNTER_METHODS = java.util.Arrays.asList("Cash", "Card");

    /**
     * Figures shown on the counter's landing page. The money side is cinema-wide,
     * not per-employee: Invoice.AccountID is the customer, and no column records
     * which employee rang the sale, so "what I sold today" is not derivable.
     */
    private void showDashboard(HttpServletRequest request, HttpServletResponse response, boolean noShift)
            throws ServletException, IOException {
        Account emp = (Account) request.getSession().getAttribute("account");

        double revenueToday = 0;
        int invoicesToday = 0;
        int ticketsToday = 0;
        int screeningsToday = 0;
        int checkinsToday = 0;
        int pendingSoon = 0;
        List<PaymentSlice> paymentToday = new ArrayList<>();
        List<UpcomingShow> upcoming = new ArrayList<>();

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate weekStart = today.minusDays(6);
        // Zero-filled up front so a day without invoices still gets a bar rather than
        // silently collapsing the axis to the days that happen to have sales.
        Map<java.time.LocalDate, Double> revByDay = new LinkedHashMap<>();
        for (java.time.LocalDate d = weekStart; !d.isAfter(today); d = d.plusDays(1)) {
            revByDay.put(d, 0.0);
        }

        try (Connection conn = DBUtils.getConnection()) {

            String sqlRevenue = """
                SELECT COALESCE(SUM(i.TotalAmount),0) AS Rev, COUNT(*) AS Cnt
                FROM Invoice i
                WHERE i.PaymentStatus = 'Paid'
                  AND CAST(i.CreatedAt AS DATE) = CAST(GETDATE() AS DATE)
                """;
            try (PreparedStatement ps = conn.prepareStatement(sqlRevenue);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    revenueToday  = rs.getDouble("Rev");
                    invoicesToday = rs.getInt("Cnt");
                }
            }

            String sqlTickets = """
                SELECT COUNT(*) FROM Ticket t
                INNER JOIN Invoice i ON t.InvoiceID = i.InvoiceID
                WHERE i.PaymentStatus = 'Paid'
                  AND CAST(i.CreatedAt AS DATE) = CAST(GETDATE() AS DATE)
                """;
            try (PreparedStatement ps = conn.prepareStatement(sqlTickets);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) ticketsToday = rs.getInt(1);
            }

            String sqlScreenings =
                "SELECT COUNT(*) FROM Schedule WHERE CAST(StartTime AS DATE) = CAST(GETDATE() AS DATE)";
            try (PreparedStatement ps = conn.prepareStatement(sqlScreenings);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) screeningsToday = rs.getInt(1);
            }

            String sqlCheckins =
                "SELECT COUNT(*) FROM Ticket WHERE IsCheckedIn = 1 AND CAST(CheckedInAt AS DATE) = CAST(GETDATE() AS DATE)";
            try (PreparedStatement ps = conn.prepareStatement(sqlCheckins);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) checkinsToday = rs.getInt(1);
            }

            // Tickets the door still has to scan: paid, unscanned, for a show that has
            // not ended and starts inside the window. Mirrors the check-in rules so the
            // number matches what /employee/checkin will actually let through.
            String sqlPendingSoon = """
                SELECT COUNT(*) FROM Ticket t
                INNER JOIN Schedule sc ON t.ScheduleID = sc.ScheduleID
                INNER JOIN Invoice i ON t.InvoiceID = i.InvoiceID
                WHERE t.IsCheckedIn = 0
                  AND i.PaymentStatus = 'Paid'
                  AND sc.EndTime >= GETDATE()
                  AND sc.StartTime <= DATEADD(HOUR, ?, GETDATE())
                """;
            try (PreparedStatement ps = conn.prepareStatement(sqlPendingSoon)) {
                ps.setInt(1, UPCOMING_WINDOW_HOURS);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) pendingSoon = rs.getInt(1);
                }
            }

            String sqlPayment = """
                SELECT i.PaymentMethod, COUNT(*) AS Cnt, COALESCE(SUM(i.TotalAmount),0) AS Rev
                FROM Invoice i
                WHERE i.PaymentStatus = 'Paid'
                  AND CAST(i.CreatedAt AS DATE) = CAST(GETDATE() AS DATE)
                GROUP BY i.PaymentMethod
                ORDER BY SUM(i.TotalAmount) DESC
                """;
            try (PreparedStatement ps = conn.prepareStatement(sqlPayment);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PaymentSlice slice = new PaymentSlice();
                    slice.setMethod(rs.getString("PaymentMethod"));
                    slice.setCount(rs.getInt("Cnt"));
                    slice.setAmount(rs.getDouble("Rev"));
                    slice.setFormattedAmount(formatMoney(rs.getDouble("Rev")));
                    slice.setCounter(COUNTER_METHODS.contains(rs.getString("PaymentMethod")));
                    slice.setPercent(revenueToday > 0
                            ? (int) Math.round(rs.getDouble("Rev") / revenueToday * 100) : 0);
                    paymentToday.add(slice);
                }
            }

            // Shows still to run today, with seats sold and tickets left to scan, so the
            // counter can see what is coming without opening the schedule page.
            String sqlUpcoming = """
                SELECT TOP 6 sc.ScheduleID, m.MovieName, r.RoomNumber, r.Capacity,
                       CONVERT(VARCHAR(5), sc.StartTime, 108) AS StartHHMM,
                       CASE WHEN sc.StartTime <= GETDATE() THEN 1 ELSE 0 END AS Ongoing,
                       (SELECT COUNT(*) FROM Ticket t WHERE t.ScheduleID = sc.ScheduleID) AS Sold,
                       (SELECT COUNT(*) FROM Ticket t
                         WHERE t.ScheduleID = sc.ScheduleID AND t.IsCheckedIn = 0) AS Pending
                FROM Schedule sc
                INNER JOIN Movie m ON sc.MovieID = m.MovieID
                INNER JOIN Room r ON sc.RoomID = r.RoomID
                WHERE sc.Status <> 'Cancelled'
                  AND sc.EndTime >= GETDATE()
                  AND CAST(sc.StartTime AS DATE) = CAST(GETDATE() AS DATE)
                ORDER BY sc.StartTime
                """;
            try (PreparedStatement ps = conn.prepareStatement(sqlUpcoming);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UpcomingShow show = new UpcomingShow();
                    show.setScheduleId(rs.getInt("ScheduleID"));
                    show.setMovieName(rs.getNString("MovieName"));
                    show.setRoomNumber(rs.getNString("RoomNumber"));
                    show.setCapacity(rs.getInt("Capacity"));
                    show.setStartTime(rs.getString("StartHHMM"));
                    show.setOngoing(rs.getInt("Ongoing") == 1);
                    show.setSold(rs.getInt("Sold"));
                    show.setPending(rs.getInt("Pending"));
                    upcoming.add(show);
                }
            }

            String sqlWeek = """
                SELECT CAST(i.CreatedAt AS DATE) AS D, SUM(i.TotalAmount) AS Rev
                FROM Invoice i
                WHERE i.PaymentStatus = 'Paid'
                  AND CAST(i.CreatedAt AS DATE) BETWEEN ? AND ?
                GROUP BY CAST(i.CreatedAt AS DATE)
                """;
            try (PreparedStatement ps = conn.prepareStatement(sqlWeek)) {
                ps.setDate(1, Date.valueOf(weekStart));
                ps.setDate(2, Date.valueOf(today));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        revByDay.put(rs.getDate("D").toLocalDate(), rs.getDouble("Rev"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("flashError",
                    "Không tải được số liệu hôm nay. Vui lòng tải lại trang hoặc báo quản lý.");
        }

        // Today's shift and any hand-off waiting on this employee — both already
        // available through the DAOs the shift pages use.
        String shiftToday = null;
        int pendingExchanges = 0;
        if (emp != null) {
            java.time.LocalTime nowTime = java.time.LocalTime.now();
            for (WorkShift ws : shiftDAO.getByEmployeeAndMonth(
                    emp.getAccountId(), today.getYear(), today.getMonthValue())) {
                if (!today.equals(ws.getShiftDate())
                        || ws.getStartTime() == null || ws.getEndTime() == null) {
                    continue;
                }
                String label = formatTime(ws.getStartTime()) + " – " + formatTime(ws.getEndTime());
                boolean covering = !nowTime.isBefore(ws.getStartTime())
                        && !nowTime.isAfter(ws.getEndTime());
                // The shift being served right now wins; otherwise the first one today
                // stands in, so an employee before clock-on still sees when to arrive.
                if (covering || shiftToday == null) {
                    shiftToday = label;
                }
            }
            pendingExchanges = exchangeDAO.getIncoming(emp.getAccountId()).size();
        }

        java.time.format.DateTimeFormatter dayFmt =
                java.time.format.DateTimeFormatter.ofPattern("dd/MM");
        List<String> chartLabels = new ArrayList<>();
        List<Double> chartValues = new ArrayList<>();
        for (Map.Entry<java.time.LocalDate, Double> entry : revByDay.entrySet()) {
            chartLabels.add(entry.getKey().format(dayFmt));
            chartValues.add(entry.getValue());
        }
        Map<String, Object> chartMap = new LinkedHashMap<>();
        chartMap.put("labels", chartLabels);
        chartMap.put("values", chartValues);
        request.setAttribute("revenueChartJson", new Gson().toJson(chartMap));

        request.setAttribute("empRevenueToday", formatMoney(revenueToday));
        request.setAttribute("empInvoicesToday", invoicesToday);
        request.setAttribute("empTicketsToday", ticketsToday);
        request.setAttribute("empScreeningsToday", screeningsToday);
        request.setAttribute("empCheckinsToday", checkinsToday);
        request.setAttribute("empPendingSoon", pendingSoon);
        request.setAttribute("empWindowHours", UPCOMING_WINDOW_HOURS);
        request.setAttribute("empPaymentToday", paymentToday);
        request.setAttribute("empUpcoming", upcoming);
        request.setAttribute("empUpcomingCount", upcoming.size());
        request.setAttribute("empShiftToday", shiftToday);
        request.setAttribute("empPendingExchanges", pendingExchanges);
        request.setAttribute("empShiftStatus", noShift ? "Ngoài ca" : "Đang ca");
        request.setAttribute("noShift", noShift);

        request.getRequestDispatcher(DASHBOARD_JSP).forward(request, response);
    }

    /** Thousands-separated whole VND, matching how prices read elsewhere in the UI. */
    private static String formatMoney(double amount) {
        return String.format("%,.0f", amount);
    }

    private static String formatTime(java.time.LocalTime time) {
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    private void showSchedules(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        tbSchedule scheduleDAO = new tbSchedule();
        tbMovie movieDAO = new tbMovie();
        RoomDAO roomDAO = new RoomDAO();

        String dateStr = request.getParameter("date");
        if (dateStr == null || dateStr.trim().isEmpty()) {
            dateStr = new Date(System.currentTimeMillis()).toString();
        }

        // parseOptionalInt, not Integer.parseInt: a hand-edited ?movieId=abc used to
        // throw out of processRequest and render a 500 page instead of the schedule list.
        Integer movieId = parseOptionalInt(request.getParameter("movieId"));
        Integer roomId  = parseOptionalInt(request.getParameter("roomId"));

        List<clsSchedule> schedules = scheduleDAO.getSchedules(dateStr, movieId, roomId);
        List<clsMovie> movieList = movieDAO.getAllActiveMovies();
        List<Room> roomList = roomDAO.getAllRooms();

        TicketDAO ticketDAO = new TicketDAO();
        List<Integer> seatsSoldList = new ArrayList<>();
        for (clsSchedule s : schedules) {
            seatsSoldList.add(ticketDAO.getBookedSeatIdsByScheduleId(s.getScheduleId()).size());
        }

        request.setAttribute("scheduleList", schedules);
        request.setAttribute("seatsSoldList", seatsSoldList);
        request.setAttribute("movieList", movieList);
        request.setAttribute("roomList", roomList);
        request.setAttribute("selectedDate", dateStr);
        request.setAttribute("selectedMovieId", movieId);
        request.setAttribute("selectedRoomId", roomId);

        request.getRequestDispatcher(SCHEDULES_JSP).forward(request, response);
    }

    private void showTickets(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        tbSchedule scheduleDAO = new tbSchedule();
        TicketDAO ticketDAO = new TicketDAO();

        String scheduleIdStr = request.getParameter("scheduleId");
        if (scheduleIdStr == null || scheduleIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/employee/schedules");
            return;
        }

        try {
            int scheduleId = Integer.parseInt(scheduleIdStr);
            clsSchedule schedule = scheduleDAO.getScheduleById(scheduleId);
            if (schedule == null) {
                response.sendRedirect(request.getContextPath() + "/employee/schedules");
                return;
            }

            List<Ticket> bookedTickets = ticketDAO.getBookedTicketsByScheduleId(scheduleId);

            double totalRevenue = 0.0;
            for (Ticket t : bookedTickets) {
                totalRevenue += t.getPriceAtBooking();
            }

            request.setAttribute("schedule", schedule);
            request.setAttribute("tickets", bookedTickets);
            request.setAttribute("totalRevenue", totalRevenue);
            request.getRequestDispatcher(TICKETS_JSP).forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/employee/schedules");
        }
    }

    private void showBookForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        tbSchedule scheduleDAO = new tbSchedule();
        SeatDAO seatDAO = new SeatDAO();
        TicketDAO ticketDAO = new TicketDAO();

        String scheduleIdStr = request.getParameter("scheduleId");
        if (scheduleIdStr == null || scheduleIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/employee/schedules");
            return;
        }

        try {
            int scheduleId = Integer.parseInt(scheduleIdStr);
            clsSchedule schedule = scheduleDAO.getScheduleById(scheduleId);
            if (schedule == null) {
                response.sendRedirect(request.getContextPath() + "/employee/schedules");
                return;
            }

            List<Seat> seats = seatDAO.getSeatsByRoom(schedule.getRoomId());
            List<Integer> bookedSeatIds = ticketDAO.getBookedSeatIdsByScheduleId(scheduleId);

            request.setAttribute("schedule", schedule);
            request.setAttribute("seats", seats);
            request.setAttribute("bookedSeatIds", bookedSeatIds);
            request.getRequestDispatcher(BOOK_JSP).forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/employee/schedules");
        }
    }

    private void handleBook(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        tbSchedule scheduleDAO = new tbSchedule();
        SeatDAO seatDAO = new SeatDAO();
        TicketDAO ticketDAO = new TicketDAO();
        AccountDAO accountDAO = new AccountDAO();
        PromotionDAO promotionDAO = new PromotionDAO();

        String scheduleIdStr = request.getParameter("scheduleId");
        String[] seatIdStrs = request.getParameterValues("seatIds");
        String customerEmail = request.getParameter("customerEmail");
        String customerName = request.getParameter("customerName");
        String customerPhone = request.getParameter("customerPhone");
        String promoCode = request.getParameter("promoCode");

        String paymentMethod = request.getParameter("paymentMethod");
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "Cash";
        }
        switch (paymentMethod) {
            case "Cash": case "Card": case "VNPay": break;
            default: paymentMethod = "Cash";
        }

        if (scheduleIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/employee/schedules");
            return;
        }

        if (seatIdStrs == null || seatIdStrs.length == 0) {
            request.getSession().setAttribute("flashError", "Vui lòng chọn ít nhất một ghế.");
            response.sendRedirect(request.getContextPath() + "/employee/book?scheduleId=" + scheduleIdStr);
            return;
        }

        try {
            int scheduleId = Integer.parseInt(scheduleIdStr);
            clsSchedule schedule = scheduleDAO.getScheduleById(scheduleId);
            if (schedule == null) {
                response.sendRedirect(request.getContextPath() + "/employee/schedules");
                return;
            }

            int customerId = resolveCustomerId(accountDAO, customerEmail, customerName, customerPhone, request);

            List<Seat> selectedSeats = new ArrayList<>();
            List<Seat> allSeats = seatDAO.getSeatsByRoom(schedule.getRoomId());
            for (String seatIdStr : seatIdStrs) {
                int seatId = Integer.parseInt(seatIdStr);
                for (Seat s : allSeats) {
                    if (s.getSeatId() == seatId) {
                        selectedSeats.add(s);
                        break;
                    }
                }
            }

            if (selectedSeats.isEmpty()) {
                request.getSession().setAttribute("flashError", "Lựa chọn ghế không hợp lệ.");
                response.sendRedirect(request.getContextPath() + "/employee/book?scheduleId=" + scheduleId);
                return;
            }

            double subtotal = computeSubtotal(selectedSeats, schedule.getBaseTicketPrice());

            Integer promotionId = null;
            double discountAmount = 0.0;
            if (promoCode != null && !promoCode.trim().isEmpty()) {
                Promotion promo = promotionDAO.findByActiveCode(promoCode.trim());
                if (promo == null) {
                    request.getSession().setAttribute("flashError",
                            "Mã khuyến mãi \"" + promoCode.trim() + "\" không hợp lệ hoặc đã hết hạn.");
                    response.sendRedirect(request.getContextPath() + "/employee/book?scheduleId=" + scheduleId);
                    return;
                }
                if (promo.getMinOrderAmount() != null
                        && subtotal < promo.getMinOrderAmount().doubleValue()) {
                    request.getSession().setAttribute("flashError",
                            "Đơn tối thiểu " + String.format("%,.0f", promo.getMinOrderAmount().doubleValue())
                            + " VND để áp dụng mã " + promoCode.trim() + ".");
                    response.sendRedirect(request.getContextPath() + "/employee/book?scheduleId=" + scheduleId);
                    return;
                }
                discountAmount = computeDiscount(promo, subtotal);
                promotionId = promo.getPromotionId();
            }

            List<String> newCodes;
            try {
                newCodes = ticketDAO.createManualBooking(
                        scheduleId, selectedSeats, customerId,
                        schedule.getBaseTicketPrice(), paymentMethod, promotionId, discountAmount);
            } catch (BookingConflictException conflict) {
                request.getSession().setAttribute("flashError", messageFor(conflict));
                response.sendRedirect(request.getContextPath() + "/employee/book?scheduleId=" + scheduleId);
                return;
            }

            if (!newCodes.isEmpty()) {
                request.getSession().setAttribute("flashSuccess", "Xuất vé thành công!");
                request.getSession().setAttribute("flashNewCodes", newCodes);
                response.sendRedirect(request.getContextPath() + "/employee/tickets?scheduleId=" + scheduleId);
            } else {
                // A refused booking now arrives as BookingConflictException, so an
                // empty list only means the transaction itself failed.
                request.getSession().setAttribute("flashError",
                        "Không thể xuất vé do lỗi hệ thống. Vui lòng thử lại hoặc báo quản lý.");
                response.sendRedirect(request.getContextPath() + "/employee/book?scheduleId=" + scheduleId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("flashError", "Đã có lỗi xảy ra: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/employee/book?scheduleId=" + scheduleIdStr);
        }
    }

    private int resolveCustomerId(AccountDAO accountDAO, String customerEmail,
            String customerName, String customerPhone, HttpServletRequest request) {
        if (customerEmail != null && !customerEmail.trim().isEmpty()) {
            Account existing = accountDAO.getAccountByEmail(customerEmail.trim());
            if (existing != null) {
                return existing.getAccountId();
            }
            Account newAcc = new Account();
            newAcc.setEmail(customerEmail.trim());
            newAcc.setFullName(customerName == null || customerName.trim().isEmpty()
                    ? "Walk-in Customer" : customerName.trim());
            newAcc.setPhoneNumber(customerPhone == null || customerPhone.trim().isEmpty()
                    ? null : customerPhone.trim());
            // Random, not a shared default: anyone who knows a customer's email would
            // otherwise be able to log into the account created for them at the counter.
            // The customer resets it through Forgot Password.
            newAcc.setPassword(EmployeeDAO.generatePassword(12));
            newAcc.setRoleId(2);
            int id = accountDAO.register(newAcc);
            if (id > 0) {
                return id;
            }
        }
        Account walkin = accountDAO.getAccountByEmail("walkin@cinema.vn");
        if (walkin != null) {
            return walkin.getAccountId();
        }
        Account newWalkin = new Account();
        newWalkin.setEmail("walkin@cinema.vn");
        newWalkin.setFullName("Walk-in Customer");
        // Shared placeholder account, never meant to be logged into.
        newWalkin.setPassword(EmployeeDAO.generatePassword(24));
        newWalkin.setRoleId(2);
        int id = accountDAO.register(newWalkin);
        if (id > 0) {
            return id;
        }
        HttpSession session = request.getSession(false);
        Account empAcc = (Account) session.getAttribute("account");
        return empAcc.getAccountId();
    }

    // Turns a booking refused by a business rule into the message shown at the
    // counter. Kept in the servlet so no user-facing text has to live in the DAO.
    private String messageFor(BookingConflictException conflict) {
        switch (conflict.getReason()) {
            case SEAT_TAKEN:
                return "Ghế vừa được bán ở quầy khác. Vui lòng tải lại sơ đồ và chọn ghế còn trống.";
            case PROMOTION_EXHAUSTED:
                return "Mã khuyến mãi vừa hết lượt sử dụng. Vui lòng bỏ mã hoặc dùng mã khác.";
            default:
                return "Không thể xuất vé. Vui lòng thử lại.";
        }
    }

    // Package-private (not private) so EmployeeDashboardServletComputeSubtotalTest,
    // in the same package under test/, can call it directly without reflection.
    // Delegates to SeatPricing so the basket priced here and the rows priced inside
    // TicketDAO.createManualBooking can never drift apart.
    double computeSubtotal(List<Seat> seats, double basePrice) {
        return SeatPricing.subtotal(seats, basePrice);
    }

    // Package-private (not private) so EmployeeDashboardServletComputeDiscountTest,
    // in the same package under test/, can call it directly without reflection.
    // Assumes promo has already been validated as active and meeting minOrderAmount.
    double computeDiscount(Promotion promo, double subtotal) {
        if ("Percentage".equalsIgnoreCase(promo.getDiscountType())) {
            double discount = subtotal * promo.getDiscountValue().doubleValue() / 100.0;
            if (promo.getMaxDiscountAmount() != null) {
                discount = Math.min(discount, promo.getMaxDiscountAmount().doubleValue());
            }
            return discount;
        }
        return Math.min(promo.getDiscountValue().doubleValue(), subtotal);
    }

    // Package-private (not private) so EmployeeDashboardServletShiftExchangeEligibilityTest,
    // in the same package under test/, can call it directly without a live WorkShiftDAO.
    // Mirrors BR-44.1: only a future, still-Scheduled shift owned by the requester
    // may be handed off. Self-transfer (targetEmpId == requesterId, BR-44.2) is
    // checked separately by the caller before this, since it has its own error message.
    static boolean isShiftExchangeable(WorkShift shift, int requesterId) {
        return shift != null
                && shift.getEmployeeId() == requesterId
                && "Scheduled".equals(shift.getStatus())
                && !shift.getShiftDate().isBefore(java.time.LocalDate.now());
    }

    private void showCheckin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String filter = request.getParameter("filter");
        if (filter == null || filter.trim().isEmpty()) {
            filter = "today";
        }
        String q = request.getParameter("q"); // ticket code or email

        List<BookingView> bookingList = new ArrayList<>();
        String sql = CHECKIN_SELECT + " WHERE 1=1";

        List<Object> params = new ArrayList<>();
        if ("today".equalsIgnoreCase(filter)) {
            sql += " AND CAST(sc.StartTime AS DATE) = CAST(GETDATE() AS DATE)";
        } else if ("pending".equalsIgnoreCase(filter)) {
            sql += " AND t.IsCheckedIn = 0";
        } else if ("checked".equalsIgnoreCase(filter)) {
            sql += " AND t.IsCheckedIn = 1";
        }

        if (q != null && !q.trim().isEmpty()) {
            sql += " AND (t.Code LIKE ? OR a.Email LIKE ? OR u.FullName LIKE ?)";
            String likeParam = "%" + q.trim() + "%";
            params.add(likeParam);
            params.add(likeParam);
            params.add(likeParam);
        }

        sql += " ORDER BY sc.StartTime DESC, s.RowChar, s.ColNumber";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookingList.add(mapBookingView(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("flashError",
                    "Không tải được danh sách vé. Vui lòng thử lại hoặc báo quản lý.");
        }

        // Stats
        int todayTotal = 0, checkedInCount = 0, pendingCount = 0;
        try (Connection conn = DBUtils.getConnection()) {
            String q1 = "SELECT COUNT(*) FROM Ticket t INNER JOIN Schedule sc ON t.ScheduleID = sc.ScheduleID WHERE CAST(sc.StartTime AS DATE) = CAST(GETDATE() AS DATE)";
            try (PreparedStatement ps = conn.prepareStatement(q1); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) todayTotal = rs.getInt(1);
            }
            String q2 = "SELECT COUNT(*) FROM Ticket t INNER JOIN Schedule sc ON t.ScheduleID = sc.ScheduleID WHERE t.IsCheckedIn = 1 AND CAST(sc.StartTime AS DATE) = CAST(GETDATE() AS DATE)";
            try (PreparedStatement ps = conn.prepareStatement(q2); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) checkedInCount = rs.getInt(1);
            }
            pendingCount = todayTotal - checkedInCount;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("bookingList", bookingList);
        request.setAttribute("todayTotal", todayTotal);
        request.setAttribute("checkedInCount", checkedInCount);
        request.setAttribute("pendingCount", pendingCount);

        // Verify/Lookup single code if parameter "code" is provided
        String verifyCode = request.getParameter("code");
        if (verifyCode != null && !verifyCode.trim().isEmpty()) {
            BookingView match = null;
            for (BookingView b : bookingList) {
                if (verifyCode.trim().equalsIgnoreCase(b.getCode())) {
                    match = b;
                    break;
                }
            }
            if (match == null) {
                // query database directly for verification code
                String sqlVerify = CHECKIN_SELECT + " WHERE t.Code = ?";
                try (Connection conn = DBUtils.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sqlVerify)) {
                    ps.setString(1, verifyCode.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            match = mapBookingView(rs);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (match != null) {
                request.setAttribute("booking", match);
            } else {
                request.setAttribute("flashError", "Ticket Code not found.");
            }
        }

        request.getRequestDispatcher(CHECKIN_JSP).forward(request, response);
    }

    private BookingView mapBookingView(ResultSet rs) throws SQLException {
        BookingView bv = new BookingView();
        bv.setCode(rs.getString("Code"));
        bv.setCustomerName(rs.getNString("CustomerName"));
        bv.setMovieTitle(rs.getNString("MovieName"));
        bv.setShowDate(rs.getDate("ShowDate").toString());
        bv.setStartTime(rs.getString("StartTime"));
        bv.setSeats(rs.getString("SeatName"));
        bv.setTotalAmount(rs.getDouble("PriceAtBooking"));
        bv.setCheckedIn(rs.getBoolean("IsCheckedIn"));
        bv.setBookingId(rs.getInt("TicketID"));
        return bv;
    }

    // ========== UC46: check-in outcomes ==========

    static final String CHECKIN_OK         = "CHECKIN_OK";
    static final String CHECKIN_NOT_FOUND  = "CHECKIN_NOT_FOUND";
    static final String CHECKIN_ALREADY    = "CHECKIN_ALREADY";
    static final String CHECKIN_UNPAID     = "CHECKIN_UNPAID";
    static final String CHECKIN_TOO_EARLY  = "CHECKIN_TOO_EARLY";
    static final String CHECKIN_SHOW_ENDED = "CHECKIN_SHOW_ENDED";
    static final String CHECKIN_ERROR      = "CHECKIN_ERROR";

    /** The door opens this long before the showtime starts. */
    static final int CHECKIN_OPENS_MINUTES_BEFORE = 30;

    /**
     * Decides whether a ticket may be let through. Takes plain values rather than the
     * DAO row so EmployeeDashboardServletCheckinOutcomeTest, in the same package under
     * test/, can cover the whole table without a database.
     *
     * Order matters: "already checked in" is reported before the showtime checks so a
     * second scan of a valid ticket reads as a duplicate rather than as a late arrival.
     *
     * @param found whether a ticket with that code exists at all
     * @param alreadyCheckedIn Ticket.IsCheckedIn
     * @param paymentStatus Invoice.PaymentStatus — only 'Paid' may enter
     * @param showStart Schedule.StartTime, null when unknown
     * @param showEnd Schedule.EndTime, null when unknown
     * @param now current time, injected so the test does not depend on the clock
     */
    static String decideCheckinOutcome(boolean found, boolean alreadyCheckedIn,
            String paymentStatus, java.time.LocalDateTime showStart,
            java.time.LocalDateTime showEnd, java.time.LocalDateTime now) {
        if (!found) {
            return CHECKIN_NOT_FOUND;
        }
        if (alreadyCheckedIn) {
            return CHECKIN_ALREADY;
        }
        if (!"Paid".equalsIgnoreCase(paymentStatus)) {
            return CHECKIN_UNPAID;
        }
        if (showStart != null
                && now.isBefore(showStart.minusMinutes(CHECKIN_OPENS_MINUTES_BEFORE))) {
            return CHECKIN_TOO_EARLY;
        }
        if (showEnd != null && now.isAfter(showEnd)) {
            return CHECKIN_SHOW_ENDED;
        }
        return CHECKIN_OK;
    }

    private String checkinMessage(String outcome) {
        switch (outcome) {
            case CHECKIN_OK:         return "Đã check-in vé thành công!";
            case CHECKIN_ALREADY:    return "Vé này đã được check-in trước đó.";
            case CHECKIN_NOT_FOUND:  return "Không tìm thấy vé với mã này.";
            case CHECKIN_UNPAID:     return "Vé chưa được thanh toán, không thể vào cửa.";
            case CHECKIN_TOO_EARLY:  return "Chưa tới giờ vào cửa (mở trước suất chiếu "
                    + CHECKIN_OPENS_MINUTES_BEFORE + " phút).";
            case CHECKIN_SHOW_ENDED: return "Suất chiếu đã kết thúc.";
            default:                 return "Lỗi hệ thống. Vui lòng thử lại.";
        }
    }

    /**
     * Runs the decision for one ticket and, when it passes, marks it checked in.
     * Returns the final outcome. Shared by the manual button (ticket id) and the QR
     * scanner (ticket code) so both go through the same rules.
     */
    private String resolveAndCheckIn(TicketDAO ticketDAO, TicketDAO.CheckinInfo info)
            throws SQLException {
        String outcome = decideCheckinOutcome(
                info != null,
                info != null && info.isCheckedIn(),
                info != null ? info.getPaymentStatus() : null,
                info != null ? info.getShowStart() : null,
                info != null ? info.getShowEnd() : null,
                java.time.LocalDateTime.now());
        if (CHECKIN_OK.equals(outcome) && !ticketDAO.markCheckedIn(info.getTicketId())) {
            // Another counter scanned the same ticket between the lookup and the
            // update — the UPDATE guard refused it, so report it as a duplicate.
            outcome = CHECKIN_ALREADY;
        }
        return outcome;
    }

    private void handleCheckin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookingIdStr = request.getParameter("bookingId");
        if (bookingIdStr == null || bookingIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/employee/checkin");
            return;
        }

        try {
            int ticketId = Integer.parseInt(bookingIdStr.trim());
            TicketDAO ticketDAO = new TicketDAO();
            String outcome = resolveAndCheckIn(ticketDAO, ticketDAO.findCheckinInfoById(ticketId));
            request.getSession().setAttribute(
                    CHECKIN_OK.equals(outcome) ? "flashSuccess" : "flashError",
                    checkinMessage(outcome));
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            request.getSession().setAttribute("flashError", checkinMessage(CHECKIN_ERROR));
        }

        response.sendRedirect(request.getContextPath() + "/employee/checkin");
    }

    /**
     * Scanner endpoint: POST /employee/checkin with action=checkin_by_code&amp;code=...
     * Answers JSON so the scanner can show the result and keep the camera running
     * instead of reloading the page for every ticket.
     */
    private void handleCheckinByCode(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new LinkedHashMap<>();
        String code = request.getParameter("code");

        if (code == null || code.trim().isEmpty()) {
            result.put("outcome", CHECKIN_NOT_FOUND);
            result.put("ok", false);
            result.put("message", checkinMessage(CHECKIN_NOT_FOUND));
            response.getWriter().write(new Gson().toJson(result));
            return;
        }

        try {
            TicketDAO ticketDAO = new TicketDAO();
            TicketDAO.CheckinInfo info = ticketDAO.findCheckinInfoByCode(code.trim());
            String outcome = resolveAndCheckIn(ticketDAO, info);

            result.put("outcome", outcome);
            result.put("ok", CHECKIN_OK.equals(outcome));
            result.put("message", checkinMessage(outcome));
            if (info != null) {
                result.put("code", info.getCode());
                result.put("movie", info.getMovieName());
                result.put("seat", info.getSeatName());
                result.put("customer", info.getCustomerName());
                result.put("showStart", info.getShowStart() != null
                        ? info.getShowStart().toString() : null);
            } else {
                result.put("code", code.trim());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            result.put("outcome", CHECKIN_ERROR);
            result.put("ok", false);
            result.put("message", checkinMessage(CHECKIN_ERROR));
        }

        response.getWriter().write(new Gson().toJson(result));
    }

    private void showSetup(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(SETUP_JSP).forward(request, response);
    }

    private void handleSetup(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        Account current = (session != null) ? (Account) session.getAttribute("account") : null;
        if (current == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }

        String fullName = request.getParameter("fullName");
        String phoneNumber = request.getParameter("phoneNumber");
        String address = request.getParameter("address");
        String dateOfBirth = request.getParameter("dateOfBirth");
        String newPassword = request.getParameter("newPassword");

        if (fullName == null || fullName.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập họ và tên.");
            request.getRequestDispatcher(SETUP_JSP).forward(request, response);
            return;
        }

        // BR-42.2: the temporary password issued at account creation must not
        // outlive setup, so a replacement is mandatory here rather than optional.
        // Leaving this blank used to clear NeedsSetup anyway, which kept a
        // Manager-known password valid indefinitely.
        if (newPassword == null || newPassword.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng đặt mật khẩu mới để thay thế mật khẩu tạm.");
            request.getRequestDispatcher(SETUP_JSP).forward(request, response);
            return;
        }

        // E1: same 6-character minimum as Register, Change Password and Reset Password.
        String password = newPassword.trim();
        if (password.length() < 6) {
            request.setAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
            request.getRequestDispatcher(SETUP_JSP).forward(request, response);
            return;
        }

        // "cannot be reused afterward" in BR-42.2 is about the temp password itself,
        // so re-typing it here has to be rejected too — a length check alone would
        // let the employee keep exactly the credential the Manager handed them.
        Account stored = accountDAO.getAccountById(current.getAccountId());
        if (stored != null && stored.getPassword() != null
                && PasswordHash.verify(password, stored.getPassword())) {
            request.setAttribute("error", "Mật khẩu mới phải khác mật khẩu tạm được cấp.");
            request.getRequestDispatcher(SETUP_JSP).forward(request, response);
            return;
        }

        Account updated = new Account();
        updated.setAccountId(current.getAccountId());
        updated.setEmail(current.getEmail());
        updated.setFullName(fullName.trim());
        updated.setPhoneNumber(phoneNumber);
        updated.setAddress(address);
        updated.setDateOfBirth(dateOfBirth);
        updated.setPassword(password);

        // clearNeedsSetup only runs when update() reported success, and update()
        // takes its password branch unconditionally now, so the setup flag can no
        // longer be cleared while the temp password is still in place.
        boolean ok = employeeDAO.update(updated);
        if (ok) {
            accountDAO.clearNeedsSetup(current.getAccountId());
            // Update session object so the guard doesn't redirect again
            current.setNeedsSetup(false);
            current.setFullName(fullName.trim());
            session.setAttribute("account", current);
            session.setAttribute("flashSuccess", "Thông tin cá nhân đã được cập nhật. Chào mừng bạn!");
            response.sendRedirect(request.getContextPath() + "/employee");
        } else {
            request.setAttribute("error", "Có lỗi xảy ra. Vui lòng thử lại.");
            request.getRequestDispatcher(SETUP_JSP).forward(request, response);
        }
    }

    private void showMyShifts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Account emp = (session != null) ? (Account) session.getAttribute("account") : null;
        if (emp == null) { response.sendRedirect(request.getContextPath() + "/Login"); return; }

        int empId = emp.getAccountId();
        int year  = java.time.LocalDate.now().getYear();
        int month = java.time.LocalDate.now().getMonthValue();

        String yearStr  = request.getParameter("year");
        String monthStr = request.getParameter("month");
        if (yearStr  != null && !yearStr.isEmpty())  { try { year  = Integer.parseInt(yearStr);  } catch (NumberFormatException ignored) {} }
        if (monthStr != null && !monthStr.isEmpty()) { try { month = Integer.parseInt(monthStr); } catch (NumberFormatException ignored) {} }
        if (month < 1)  month = 1;
        if (month > 12) month = 12;

        List<WorkShift> myShifts = shiftDAO.getByEmployeeAndMonth(empId, year, month);
        List<ShiftExchangeRequest> incoming = exchangeDAO.getIncoming(empId);
        List<ShiftExchangeRequest> outgoing = exchangeDAO.getOutgoing(empId);
        List<Account> colleagues = employeeDAO.getAll(null, 1, 200, "name", "ASC");

        int prevMonth = month == 1 ? 12 : month - 1;
        int prevYear  = month == 1 ? year - 1 : year;
        int nextMonth = month == 12 ? 1 : month + 1;
        int nextYear  = month == 12 ? year + 1 : year;

        request.setAttribute("myShifts",    myShifts);
        request.setAttribute("incoming",    incoming);
        request.setAttribute("outgoing",    outgoing);
        request.setAttribute("colleagues",  colleagues);
        request.setAttribute("selYear",     year);
        request.setAttribute("selMonth",    month);
        request.setAttribute("prevYear",    prevYear);
        request.setAttribute("prevMonth",   prevMonth);
        request.setAttribute("nextYear",    nextYear);
        request.setAttribute("nextMonth",   nextMonth);
        request.setAttribute("serverToday", java.time.LocalDate.now());
        request.getRequestDispatcher(MY_SHIFTS_JSP).forward(request, response);
    }

    private void handleMyShiftsPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        Account emp = (session != null) ? (Account) session.getAttribute("account") : null;
        if (emp == null) { response.sendRedirect(request.getContextPath() + "/Login"); return; }

        int    empId  = emp.getAccountId();
        String action = request.getParameter("action");
        String backUrl = request.getContextPath() + "/employee/my-shifts";

        try {
            switch (action != null ? action : "") {
                case "request_exchange": {
                    int    shiftId  = Integer.parseInt(request.getParameter("shiftId"));
                    int    targetId = Integer.parseInt(request.getParameter("targetEmpId"));
                    String message  = request.getParameter("message");
                    if (targetId == empId) {
                        session.setAttribute("flashError", "Không thể chuyển ca cho chính mình.");
                        break;
                    }
                    WorkShift targetShift = shiftDAO.getById(shiftId);
                    if (!isShiftExchangeable(targetShift, empId)) {
                        session.setAttribute("flashError", "Ca này không thể chuyển (đã qua hoặc không thuộc về bạn).");
                        break;
                    }
                    int created = exchangeDAO.createRequest(shiftId, empId, targetId, message);
                    if (created > 0) {
                        session.setAttribute("flashSuccess", "Yêu cầu chuyển ca đã được gửi.");
                        ShiftExchangeRequest newRequest = exchangeDAO.getById(created);
                        if (newRequest != null) {
                            notificationService.notifyShiftExchangeRequested(newRequest);
                        }
                    } else {
                        session.setAttribute("flashError",
                                "Không thể gửi yêu cầu. Người nhận phải là nhân viên đang hoạt động.");
                    }
                    break;
                }
                case "accept_exchange": {
                    int requestId = Integer.parseInt(request.getParameter("requestId"));
                    boolean ok = exchangeDAO.accept(requestId, empId);
                    if (ok) {
                        ShiftExchangeRequest accepted = exchangeDAO.getById(requestId);
                        if (accepted != null) {
                            notificationService.notifyShiftExchangeAccepted(accepted);
                        }
                    }
                    session.setAttribute(ok ? "flashSuccess" : "flashError",
                            ok ? "Đã nhận ca thành công." : "Không thể nhận ca. Yêu cầu có thể đã hết hạn.");
                    break;
                }
                case "reject_exchange": {
                    int requestId = Integer.parseInt(request.getParameter("requestId"));
                    boolean ok = exchangeDAO.reject(requestId, empId);
                    if (ok) {
                        ShiftExchangeRequest rejected = exchangeDAO.getById(requestId);
                        if (rejected != null) {
                            notificationService.notifyShiftExchangeRejected(rejected);
                        }
                    }
                    session.setAttribute(ok ? "flashSuccess" : "flashError",
                            ok ? "Đã từ chối yêu cầu." : "Không thể từ chối. Vui lòng thử lại.");
                    break;
                }
                case "cancel_exchange": {
                    int requestId = Integer.parseInt(request.getParameter("requestId"));
                    boolean ok = exchangeDAO.cancel(requestId, empId);
                    if (ok) {
                        ShiftExchangeRequest cancelled = exchangeDAO.getById(requestId);
                        if (cancelled != null) {
                            notificationService.notifyShiftExchangeCancelled(cancelled);
                        }
                    }
                    session.setAttribute(ok ? "flashSuccess" : "flashError",
                            ok ? "Đã hủy yêu cầu chuyển ca." : "Không thể hủy. Vui lòng thử lại.");
                    break;
                }
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("flashError", "Lỗi hệ thống. Vui lòng thử lại.");
        }
        response.sendRedirect(backUrl);
    }

    // Returns the current employee's unread count and recent notifications as JSON,
    // used by the bell icon popup on employee pages.
    private void handleNotificationsGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        HttpSession session = request.getSession(false);
        Account emp = (session != null) ? (Account) session.getAttribute("account") : null;
        if (emp == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Not logged in\"}");
            return;
        }

        List<Notification> recent = notificationService.getRecent(emp.getAccountId(), 10);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Notification n : recent) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", n.getNotificationId());
            item.put("type", n.getType());
            item.put("title", n.getTitle());
            item.put("message", n.getMessage());
            item.put("read", n.isRead());
            item.put("timeAgo", n.getTimeAgoDisplay());
            items.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("unreadCount", notificationService.getUnreadCount(emp.getAccountId()));
        result.put("items", items);

        response.getWriter().write(new Gson().toJson(result));
    }

    // Marks one notification (action=mark_read&notificationId=) or all
    // notifications (action=mark_all_read) as read for the current employee.
    private void handleNotificationsPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        HttpSession session = request.getSession(false);
        Account emp = (session != null) ? (Account) session.getAttribute("account") : null;
        if (emp == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false}");
            return;
        }

        String action = request.getParameter("action");
        boolean success;
        if ("mark_all_read".equals(action)) {
            notificationService.markAllAsRead(emp.getAccountId());
            success = true;
        } else if ("mark_read".equals(action)) {
            int notificationId = Integer.parseInt(request.getParameter("notificationId"));
            success = notificationService.markAsRead(notificationId, emp.getAccountId());
        } else {
            success = false;
        }

        response.getWriter().write("{\"success\":" + success + "}");
    }

    private void showProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Account current = (session != null) ? (Account) session.getAttribute("account") : null;
        if (current == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }
        request.getRequestDispatcher(PROFILE_JSP).forward(request, response);
    }

    private void transferFlash(HttpSession session, HttpServletRequest request, String key) {
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(key, value);
            session.removeAttribute(key);
        }
    }

    /** Returns null for a missing, blank or non-numeric parameter — never throws. */
    private Integer parseOptionalInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Employee Actions Controller";
    }

    // DTO for checkin list view mapping
    public static class BookingView {
        private String code;
        private String customerName;
        private String movieTitle;
        private String showDate;
        private String startTime;
        private String seats;
        private double totalAmount;
        private boolean checkedIn;
        private int bookingId;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public String getMovieTitle() { return movieTitle; }
        public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

        public String getShowDate() { return showDate; }
        public void setShowDate(String showDate) { this.showDate = showDate; }

        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }

        public String getSeats() { return seats; }
        public void setSeats(String seats) { this.seats = seats; }

        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

        public boolean isCheckedIn() { return checkedIn; }
        public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn; }

        public int getBookingId() { return bookingId; }
        public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    }

    /** One payment method's share of today's takings, for the dashboard breakdown. */
    public static class PaymentSlice {
        private String method;
        private int count;
        private double amount;
        private String formattedAmount;
        private int percent;
        private boolean counter;

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }

        public String getFormattedAmount() { return formattedAmount; }
        public void setFormattedAmount(String formattedAmount) { this.formattedAmount = formattedAmount; }

        public int getPercent() { return percent; }
        public void setPercent(int percent) { this.percent = percent; }

        /** True when the money was taken at the desk rather than paid online. */
        public boolean isCounter() { return counter; }
        public void setCounter(boolean counter) { this.counter = counter; }
    }

    /** A show still to run today, with what the counter still has to sell and scan. */
    public static class UpcomingShow {
        private int scheduleId;
        private String movieName;
        private String roomNumber;
        private String startTime;
        private int sold;
        private int capacity;
        private int pending;
        private boolean ongoing;

        public int getScheduleId() { return scheduleId; }
        public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }

        public String getMovieName() { return movieName; }
        public void setMovieName(String movieName) { this.movieName = movieName; }

        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }

        public int getSold() { return sold; }
        public void setSold(int sold) { this.sold = sold; }

        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }

        public int getPending() { return pending; }
        public void setPending(int pending) { this.pending = pending; }

        /** True once the show has started — the door is still open for latecomers. */
        public boolean isOngoing() { return ongoing; }
        public void setOngoing(boolean ongoing) { this.ongoing = ongoing; }

        /** Seat occupancy as a whole percent, for the fill bar. Zero when capacity is unknown. */
        public int getSoldPercent() {
            return capacity > 0 ? (int) Math.round(sold * 100.0 / capacity) : 0;
        }
    }
}
