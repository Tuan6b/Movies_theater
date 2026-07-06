package com.cinema.controller;

import com.cinema.dao.AccountDAO;
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
import com.cinema.model.Promotion;
import com.cinema.model.Room;
import com.cinema.model.Seat;
import com.cinema.model.ShiftExchangeRequest;
import com.cinema.model.Ticket;
import com.cinema.model.WorkShift;
import com.cinema.model.clsMovie;
import com.cinema.model.clsSchedule;
import com.cinema.util.DBUtils;
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

    private final AccountDAO      accountDAO      = new AccountDAO();
    private final EmployeeDAO     employeeDAO     = new EmployeeDAO();
    private final WorkShiftDAO    shiftDAO        = new WorkShiftDAO();
    private final ShiftExchangeDAO exchangeDAO    = new ShiftExchangeDAO();

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
                    handleCheckin(request, response);
                    break;
                case "/setup":
                    handleSetup(request, response);
                    break;
                case "/my-shifts":
                    handleMyShiftsPost(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        }
    }

    private void showDashboard(HttpServletRequest request, HttpServletResponse response, boolean noShift)
            throws ServletException, IOException {
        int screeningsToday = 0;
        int checkinsToday = 0;
        int activeMovies = 0;

        try (Connection conn = DBUtils.getConnection()) {
            String q1 = "SELECT COUNT(*) FROM Schedule WHERE CAST(StartTime AS DATE) = CAST(GETDATE() AS DATE)";
            try (PreparedStatement ps = conn.prepareStatement(q1); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) screeningsToday = rs.getInt(1);
            }
            String q2 = "SELECT COUNT(*) FROM Ticket WHERE IsCheckedIn = 1 AND CAST(CheckedInAt AS DATE) = CAST(GETDATE() AS DATE)";
            try (PreparedStatement ps = conn.prepareStatement(q2); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) checkinsToday = rs.getInt(1);
            }
            String q3 = "SELECT COUNT(*) FROM Movie WHERE IsActive = 1";
            try (PreparedStatement ps = conn.prepareStatement(q3); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) activeMovies = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("empScreeningsToday", screeningsToday);
        request.setAttribute("empCheckinsToday", checkinsToday);
        request.setAttribute("empActiveMovies", activeMovies);
        request.setAttribute("empShiftStatus", noShift ? "Ngoài ca" : "Đang ca");
        request.setAttribute("noShift", noShift);

        Account emp = (Account) request.getSession().getAttribute("account");
        int sang = 0, chieu = 0, toi = 0;
        if (emp != null) {
            java.time.LocalDate now = java.time.LocalDate.now();
            List<WorkShift> myShifts = shiftDAO.getByEmployeeAndMonth(emp.getAccountId(), now.getYear(), now.getMonthValue());
            for (WorkShift ws : myShifts) {
                java.time.LocalTime st = ws.getStartTime();
                if (st == null) continue;
                int hour = st.getHour();
                if (hour < 12) sang++;
                else if (hour < 18) chieu++;
                else toi++;
            }
        }
        Map<String, Object> shiftChartMap = new LinkedHashMap<>();
        shiftChartMap.put("labels", java.util.Arrays.asList("Ca sáng", "Ca chiều", "Ca tối"));
        shiftChartMap.put("values", java.util.Arrays.asList(sang, chieu, toi));
        request.setAttribute("shiftChartJson", new com.google.gson.Gson().toJson(shiftChartMap));

        request.getRequestDispatcher(DASHBOARD_JSP).forward(request, response);
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

        String movieIdStr = request.getParameter("movieId");
        Integer movieId = (movieIdStr != null && !movieIdStr.trim().isEmpty()) ? Integer.parseInt(movieIdStr) : null;

        String roomIdStr = request.getParameter("roomId");
        Integer roomId = (roomIdStr != null && !roomIdStr.trim().isEmpty()) ? Integer.parseInt(roomIdStr) : null;

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
                if ("Percentage".equalsIgnoreCase(promo.getDiscountType())) {
                    discountAmount = subtotal * promo.getDiscountValue().doubleValue() / 100.0;
                    if (promo.getMaxDiscountAmount() != null) {
                        discountAmount = Math.min(discountAmount, promo.getMaxDiscountAmount().doubleValue());
                    }
                } else {
                    discountAmount = Math.min(promo.getDiscountValue().doubleValue(), subtotal);
                }
                promotionId = promo.getPromotionId();
            }

            List<String> newCodes = ticketDAO.createManualBooking(
                    scheduleId, selectedSeats, customerId,
                    schedule.getBaseTicketPrice(), paymentMethod, promotionId, discountAmount);

            if (!newCodes.isEmpty()) {
                request.getSession().setAttribute("flashSuccess", "Xuất vé thành công!");
                request.getSession().setAttribute("flashNewCodes", newCodes);
                response.sendRedirect(request.getContextPath() + "/employee/tickets?scheduleId=" + scheduleId);
            } else {
                request.getSession().setAttribute("flashError",
                        "Không thể xuất vé. Ghế có thể đã được đặt trước.");
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
            newAcc.setPassword("cgv12345");
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
        newWalkin.setPassword("cgv12345");
        newWalkin.setRoleId(2);
        int id = accountDAO.register(newWalkin);
        if (id > 0) {
            return id;
        }
        HttpSession session = request.getSession(false);
        Account empAcc = (Account) session.getAttribute("account");
        return empAcc.getAccountId();
    }

    private double computeSubtotal(List<Seat> seats, double basePrice) {
        double total = 0.0;
        for (Seat seat : seats) {
            if ("VIP".equalsIgnoreCase(seat.getSeatType())) {
                total += basePrice * 1.5;
            } else if ("Couple".equalsIgnoreCase(seat.getSeatType())) {
                total += basePrice * 2.0;
            } else {
                total += basePrice;
            }
        }
        return total;
    }

    private void showCheckin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String filter = request.getParameter("filter");
        if (filter == null || filter.trim().isEmpty()) {
            filter = "today";
        }
        String q = request.getParameter("q"); // ticket code or email

        List<BookingView> bookingList = new ArrayList<>();
        String sql = """
            SELECT t.TicketID, t.Code, t.IsCheckedIn, t.PriceAtBooking,
                   u.FullName AS CustomerName,
                   m.MovieName,
                   CAST(sc.StartTime AS DATE) AS ShowDate,
                   CONVERT(VARCHAR(5), sc.StartTime, 108) AS StartTime,
                   s.RowChar + CAST(s.ColNumber AS VARCHAR) AS SeatName
            FROM Ticket t
            INNER JOIN Schedule sc ON t.ScheduleID = sc.ScheduleID
            INNER JOIN Seat s ON t.SeatID = s.SeatID
            INNER JOIN Invoice i ON t.InvoiceID = i.InvoiceID
            INNER JOIN Account a ON i.AccountID = a.AccountID
            LEFT JOIN UserProfile u ON a.AccountID = u.AccountID
            WHERE 1=1
            """;

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
                    bookingList.add(bv);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
                String sqlVerify = """
                    SELECT t.TicketID, t.Code, t.IsCheckedIn, t.PriceAtBooking,
                           u.FullName AS CustomerName,
                           m.MovieName,
                           CAST(sc.StartTime AS DATE) AS ShowDate,
                           CONVERT(VARCHAR(5), sc.StartTime, 108) AS StartTime,
                           s.RowChar + CAST(s.ColNumber AS VARCHAR) AS SeatName
                    FROM Ticket t
                    INNER JOIN Schedule sc ON t.ScheduleID = sc.ScheduleID
                    INNER JOIN Seat s ON t.SeatID = s.SeatID
                    INNER JOIN Invoice i ON t.InvoiceID = i.InvoiceID
                    INNER JOIN Account a ON i.AccountID = a.AccountID
                    LEFT JOIN UserProfile u ON a.AccountID = u.AccountID
                    WHERE t.Code = ?
                    """;
                try (Connection conn = DBUtils.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sqlVerify)) {
                    ps.setString(1, verifyCode.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            match = new BookingView();
                            match.setCode(rs.getString("Code"));
                            match.setCustomerName(rs.getNString("CustomerName"));
                            match.setMovieTitle(rs.getNString("MovieName"));
                            match.setShowDate(rs.getDate("ShowDate").toString());
                            match.setStartTime(rs.getString("StartTime"));
                            match.setSeats(rs.getString("SeatName"));
                            match.setTotalAmount(rs.getDouble("PriceAtBooking"));
                            match.setCheckedIn(rs.getBoolean("IsCheckedIn"));
                            match.setBookingId(rs.getInt("TicketID"));
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

    private void handleCheckin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookingIdStr = request.getParameter("bookingId");
        if (bookingIdStr == null || bookingIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/employee/checkin");
            return;
        }

        try (Connection conn = DBUtils.getConnection()) {
            int ticketId = Integer.parseInt(bookingIdStr);
            String sql = "UPDATE Ticket SET IsCheckedIn = 1, CheckedInAt = GETDATE() WHERE TicketID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, ticketId);
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    request.getSession().setAttribute("flashSuccess", "Ticket checked in successfully!");
                } else {
                    request.getSession().setAttribute("flashError", "Failed to check in ticket.");
                }
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            request.getSession().setAttribute("flashError", "Error processing check-in: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/employee/checkin");
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

        Account updated = new Account();
        updated.setAccountId(current.getAccountId());
        updated.setEmail(current.getEmail());
        updated.setFullName(fullName.trim());
        updated.setPhoneNumber(phoneNumber);
        updated.setAddress(address);
        updated.setDateOfBirth(dateOfBirth);
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            updated.setPassword(newPassword.trim());
        }

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
                    if (targetShift == null || targetShift.getEmployeeId() != empId
                            || !"Scheduled".equals(targetShift.getStatus())
                            || targetShift.getShiftDate().isBefore(java.time.LocalDate.now())) {
                        session.setAttribute("flashError", "Ca này không thể chuyển (đã qua hoặc không thuộc về bạn).");
                        break;
                    }
                    int created = exchangeDAO.createRequest(shiftId, empId, targetId, message);
                    if (created > 0) {
                        session.setAttribute("flashSuccess", "Yêu cầu chuyển ca đã được gửi.");
                    } else {
                        session.setAttribute("flashError", "Không thể gửi yêu cầu. Vui lòng thử lại.");
                    }
                    break;
                }
                case "accept_exchange": {
                    int requestId = Integer.parseInt(request.getParameter("requestId"));
                    boolean ok = exchangeDAO.accept(requestId, empId);
                    session.setAttribute(ok ? "flashSuccess" : "flashError",
                            ok ? "Đã nhận ca thành công." : "Không thể nhận ca. Yêu cầu có thể đã hết hạn.");
                    break;
                }
                case "reject_exchange": {
                    int requestId = Integer.parseInt(request.getParameter("requestId"));
                    boolean ok = exchangeDAO.reject(requestId, empId);
                    session.setAttribute(ok ? "flashSuccess" : "flashError",
                            ok ? "Đã từ chối yêu cầu." : "Không thể từ chối. Vui lòng thử lại.");
                    break;
                }
                case "cancel_exchange": {
                    int requestId = Integer.parseInt(request.getParameter("requestId"));
                    boolean ok = exchangeDAO.cancel(requestId, empId);
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
