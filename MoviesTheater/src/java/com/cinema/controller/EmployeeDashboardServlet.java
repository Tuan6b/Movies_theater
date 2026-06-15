package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.dao.RoomDAO;
import com.cinema.dao.SeatDAO;
import com.cinema.dao.TicketDAO;
import com.cinema.dao.tbMovie;
import com.cinema.dao.tbSchedule;
import com.cinema.model.Account;
import com.cinema.model.Room;
import com.cinema.model.Seat;
import com.cinema.model.Ticket;
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
import java.util.List;

public class EmployeeDashboardServlet extends HttpServlet {

    private static final String DASHBOARD_JSP = "/WEB-INF/employee/dashboard.jsp";
    private static final String SCHEDULES_JSP = "/WEB-INF/employee/schedules.jsp";
    private static final String TICKETS_JSP = "/WEB-INF/employee/tickets.jsp";
    private static final String BOOK_JSP = "/WEB-INF/employee/book.jsp";
    private static final String CHECKIN_JSP = "/WEB-INF/employee/checkin.jsp";
    private static final String PROFILE_JSP = "/WEB-INF/employee/profile.jsp";

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
        }

        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method)) {
            switch (path) {
                case "/":
                case "/dashboard":
                    showDashboard(request, response);
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
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        }
    }

    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
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
        request.setAttribute("empShiftStatus", "Đang ca");

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

            request.setAttribute("schedule", schedule);
            request.setAttribute("tickets", bookedTickets);
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
        tbSchedule scheduleDAO = new tbSchedule();
        SeatDAO seatDAO = new SeatDAO();
        TicketDAO ticketDAO = new TicketDAO();
        AccountDAO accountDAO = new AccountDAO();

        String scheduleIdStr = request.getParameter("scheduleId");
        String[] seatIdStrs = request.getParameterValues("seatIds");
        String customerEmail = request.getParameter("customerEmail");
        String customerName = request.getParameter("customerName");
        String customerPhone = request.getParameter("customerPhone");

        if (scheduleIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/employee/schedules");
            return;
        }

        if (seatIdStrs == null || seatIdStrs.length == 0) {
            request.getSession().setAttribute("flashError", "Please select at least one seat.");
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

            int customerId = -1;
            if (customerEmail != null && !customerEmail.trim().isEmpty()) {
                Account existing = accountDAO.getAccountByEmail(customerEmail.trim());
                if (existing != null) {
                    customerId = existing.getAccountId();
                } else {
                    Account newAcc = new Account();
                    newAcc.setEmail(customerEmail.trim());
                    newAcc.setFullName(customerName == null || customerName.trim().isEmpty() ? "Walk-in Customer" : customerName.trim());
                    newAcc.setPhoneNumber(customerPhone == null || customerPhone.trim().isEmpty() ? null : customerPhone.trim());
                    newAcc.setPassword("cgv12345");
                    newAcc.setRoleId(2);
                    customerId = accountDAO.register(newAcc);
                }
            } else {
                Account defaultWalkin = accountDAO.getAccountByEmail("walkin@cinema.vn");
                if (defaultWalkin != null) {
                    customerId = defaultWalkin.getAccountId();
                } else {
                    Account newAcc = new Account();
                    newAcc.setEmail("walkin@cinema.vn");
                    newAcc.setFullName("Walk-in Customer");
                    newAcc.setPhoneNumber(null);
                    newAcc.setPassword("cgv12345");
                    newAcc.setRoleId(2);
                    customerId = accountDAO.register(newAcc);

                    if (customerId <= 0) {
                        HttpSession session = request.getSession(false);
                        Account empAcc = (Account) session.getAttribute("account");
                        customerId = empAcc.getAccountId();
                    }
                }
            }

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

            boolean success = ticketDAO.createManualBooking(scheduleId, selectedSeats, customerId, schedule.getBaseTicketPrice());
            if (success) {
                request.getSession().setAttribute("flashSuccess", "Ticket booked successfully!");
                response.sendRedirect(request.getContextPath() + "/employee/tickets?scheduleId=" + scheduleId);
            } else {
                request.getSession().setAttribute("flashError", "Failed to book tickets. The seats might have been booked in the meantime.");
                response.sendRedirect(request.getContextPath() + "/employee/book?scheduleId=" + scheduleId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("flashError", "An error occurred: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/employee/book?scheduleId=" + scheduleIdStr);
        }
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
