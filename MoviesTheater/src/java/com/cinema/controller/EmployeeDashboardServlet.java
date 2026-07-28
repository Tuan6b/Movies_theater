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

    /**
     * The counter's landing page. It is a navigation screen, not a report: the
     * revenue, ticket and payment figures it used to carry belong to UC49 (View
     * Ticket Revenue Statistics), which the use case table assigns to the Manager.
     * All that is left is the employee's own shift, because every other feature on
     * the page is gated on whether a shift is running right now.
     */
    private void showDashboard(HttpServletRequest request, HttpServletResponse response, boolean noShift)
            throws ServletException, IOException {
        Account emp = (Account) request.getSession().getAttribute("account");

        String shiftToday = null;
        if (emp != null) {
            java.time.LocalDate today = java.time.LocalDate.now();
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
        }

        request.setAttribute("empShiftToday",  shiftToday);
        request.setAttribute("empShiftStatus", noShift ? "Ngoài ca" : "Đang ca");
        request.setAttribute("noShift",        noShift);

        request.getRequestDispatcher(DASHBOARD_JSP).forward(request, response);
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

        int page = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageParam);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        int pageSize = 10;
        int offset = (page - 1) * pageSize;

        List<BookingView> bookingList = new ArrayList<>();
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1");

        List<Object> params = new ArrayList<>();
        if ("today".equalsIgnoreCase(filter)) {
            whereClause.append(" AND CAST(sc.StartTime AS DATE) = CAST(GETDATE() AS DATE)");
        } else if ("pending".equalsIgnoreCase(filter)) {
            whereClause.append(" AND t.IsCheckedIn = 0");
        } else if ("checked".equalsIgnoreCase(filter)) {
            whereClause.append(" AND t.IsCheckedIn = 1");
        }

        if (q != null && !q.trim().isEmpty()) {
            whereClause.append(" AND (t.Code LIKE ? OR a.Email LIKE ? OR u.FullName LIKE ?)");
            String likeParam = "%" + q.trim() + "%";
            params.add(likeParam);
            params.add(likeParam);
            params.add(likeParam);
        }

        // Count total matching records for pagination
        int totalRecords = 0;
        String countSql = """
            SELECT COUNT(*)
            FROM Ticket t
            INNER JOIN Schedule sc ON t.ScheduleID = sc.ScheduleID
            INNER JOIN Movie m ON sc.MovieID = m.MovieID
            INNER JOIN Seat s ON t.SeatID = s.SeatID
            INNER JOIN Invoice i ON t.InvoiceID = i.InvoiceID
            INNER JOIN Account a ON i.AccountID = a.AccountID
            LEFT JOIN UserProfile u ON a.AccountID = u.AccountID
            """ + whereClause.toString();

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalRecords = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPages < 1) {
            totalPages = 1;
        }
        if (page > totalPages) {
            page = totalPages;
            offset = (page - 1) * pageSize;
        }

        String sql = CHECKIN_SELECT + whereClause.toString()
                + " ORDER BY sc.StartTime DESC, s.RowChar, s.ColNumber"
                + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(paramIndex++, params.get(i));
            }
            ps.setInt(paramIndex++, offset);
            ps.setInt(paramIndex, pageSize);

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
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

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
        forwardSetup(request, response, null);
    }

    /**
     * Renders the first-login screen. Name, phone, date of birth and address are
     * captured by the Manager at UC44, so setup only shows them back read-only —
     * and it reads them from the employee row rather than the session Account,
     * which carries neither address nor date of birth.
     */
    private void forwardSetup(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Account current = (session != null) ? (Account) session.getAttribute("account") : null;
        if (current != null) {
            request.setAttribute("profile", employeeDAO.getById(current.getAccountId()));
        }
        if (error != null) {
            request.setAttribute("error", error);
        }
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

        String newPassword = request.getParameter("newPassword");

        // BR-42.2: the temporary password issued at account creation must not
        // outlive setup, so a replacement is mandatory here rather than optional.
        // Leaving this blank used to clear NeedsSetup anyway, which kept a
        // Manager-known password valid indefinitely.
        if (newPassword == null || newPassword.trim().isEmpty()) {
            forwardSetup(request, response, "Vui lòng đặt mật khẩu mới để thay thế mật khẩu tạm.");
            return;
        }

        // E1: same 6-character minimum as Register, Change Password and Reset Password.
        String password = newPassword.trim();
        if (password.length() < 6) {
            forwardSetup(request, response, "Mật khẩu phải có ít nhất 6 ký tự.");
            return;
        }

        // "cannot be reused afterward" in BR-42.2 is about the temp password itself,
        // so re-typing it here has to be rejected too — a length check alone would
        // let the employee keep exactly the credential the Manager handed them.
        Account stored = accountDAO.getAccountById(current.getAccountId());
        if (stored != null && stored.getPassword() != null
                && PasswordHash.verify(password, stored.getPassword())) {
            forwardSetup(request, response, "Mật khẩu mới phải khác mật khẩu tạm được cấp.");
            return;
        }

        // resetPassword, not update(): the profile fields this form used to post are
        // the Manager's to fill in now, and update() rewrites the whole UserProfile
        // row, so going through it would blank out the phone, date of birth and
        // address entered at UC44. Both statements are scoped to RoleID = Employee.
        boolean ok = employeeDAO.resetPassword(current.getAccountId(), password);
        if (ok) {
            accountDAO.clearNeedsSetup(current.getAccountId());
            // Update session object so the guard doesn't redirect again
            current.setNeedsSetup(false);
            session.setAttribute("account", current);
            session.setAttribute("flashSuccess", "Tài khoản đã được kích hoạt. Chào mừng bạn!");
            response.sendRedirect(request.getContextPath() + "/employee");
        } else {
            forwardSetup(request, response, "Có lỗi xảy ra. Vui lòng thử lại.");
        }
    }

    /** Rows per page in the employee's "requests I sent" list. */
    private static final int REQUEST_PAGE_SIZE = 5;

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

        // The request history is paged; the pending subset is not, because the
        // calendar marks every shift with an open request no matter which page of
        // the history is showing.
        int reqPage = 1;
        String reqPageStr = request.getParameter("reqPage");
        if (reqPageStr != null && !reqPageStr.isEmpty()) {
            try { reqPage = Integer.parseInt(reqPageStr); } catch (NumberFormatException ignored) {}
        }
        if (reqPage < 1) reqPage = 1;

        int reqTotal = exchangeDAO.countOutgoing(empId);
        int reqTotalPages = reqTotal == 0 ? 1 : (int) Math.ceil((double) reqTotal / REQUEST_PAGE_SIZE);
        if (reqPage > reqTotalPages) reqPage = reqTotalPages;

        List<WorkShift> myShifts = shiftDAO.getByEmployeeAndMonth(empId, year, month);
        List<ShiftExchangeRequest> incoming = exchangeDAO.getIncoming(empId);
        List<ShiftExchangeRequest> outgoing = exchangeDAO.getOutgoing(empId, reqPage, REQUEST_PAGE_SIZE);
        List<ShiftExchangeRequest> outgoingPending = exchangeDAO.getOutgoingPending(empId);
        List<Account> colleagues = employeeDAO.getAll(null, 1, 200, "name", "ASC");

        int prevMonth = month == 1 ? 12 : month - 1;
        int prevYear  = month == 1 ? year - 1 : year;
        int nextMonth = month == 12 ? 1 : month + 1;
        int nextYear  = month == 12 ? year + 1 : year;

        request.setAttribute("myShifts",        myShifts);
        request.setAttribute("incoming",        incoming);
        request.setAttribute("outgoing",        outgoing);
        request.setAttribute("outgoingPending", outgoingPending);
        request.setAttribute("reqPage",         reqPage);
        request.setAttribute("reqTotal",        reqTotal);
        request.setAttribute("reqTotalPages",   reqTotalPages);
        request.setAttribute("colleagues",      colleagues);
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
                // accept_exchange / reject_exchange used to live here. Approving a
                // hand-off is the Manager's call now (/manager/shift-exchanges), so
                // the recipient has nothing to post from this page — leaving the
                // actions mapped would keep that authority in two places at once.
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
}
