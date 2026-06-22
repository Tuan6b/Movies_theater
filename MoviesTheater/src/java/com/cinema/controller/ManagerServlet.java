package com.cinema.controller;

import com.cinema.util.DBUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManagerServlet extends HttpServlet {

    private static final String DASHBOARD_JSP = "/WEB-INF/manager/dashboard.jsp";
    private static final String ANALYTICS_JSP = "/WEB-INF/manager/analytics/index.jsp";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String path = request.getPathInfo();
        if (path == null || path.equals("/") || path.equals("/dashboard")) {
            path = "/dashboard";
        }

        if ("/analytics".equals(path)) {
            showAnalytics(request, response);
        } else {
            request.getRequestDispatcher(DASHBOARD_JSP).forward(request, response);
        }
    }

    private void showAnalytics(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String yearParam   = request.getParameter("year");
        String sortByParam = request.getParameter("sortBy");
        String dirParam    = request.getParameter("dir");

        int year    = yearParam != null && yearParam.matches("\\d{4}") ? Integer.parseInt(yearParam) : java.time.Year.now().getValue();
        String sortCol = "revenue".equals(sortByParam) ? "TotalRevenue" : "Month";
        String dir     = "ASC".equalsIgnoreCase(dirParam) ? "ASC" : "DESC";

        // ── Monthly revenue table ──────────────────────────────────────────
        String sqlMonthly =
                "SELECT inv.Month, inv.TotalInvoices, inv.TotalRevenue, "
                + "       COALESCE(tkt.TicketsSold, 0) AS TicketsSold "
                + "FROM ("
                + "  SELECT MONTH(i.CreatedAt) AS Month, COUNT(*) AS TotalInvoices, "
                + "         SUM(i.TotalAmount) AS TotalRevenue "
                + "  FROM Invoice i WHERE i.PaymentStatus = 'Paid' AND YEAR(i.CreatedAt) = ? "
                + "  GROUP BY MONTH(i.CreatedAt)"
                + ") inv "
                + "LEFT JOIN ("
                + "  SELECT MONTH(i2.CreatedAt) AS Month, COUNT(t.TicketID) AS TicketsSold "
                + "  FROM Ticket t JOIN Invoice i2 ON t.InvoiceID = i2.InvoiceID "
                + "  WHERE i2.PaymentStatus = 'Paid' AND YEAR(i2.CreatedAt) = ? "
                + "  GROUP BY MONTH(i2.CreatedAt)"
                + ") tkt ON inv.Month = tkt.Month "
                + "ORDER BY inv." + sortCol + " " + dir;

        List<MonthlyRevenue> monthlyData = new ArrayList<>();
        double grandTotal   = 0;
        int    grandTickets = 0;

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlMonthly)) {
            ps.setInt(1, year);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MonthlyRevenue mr = new MonthlyRevenue();
                    mr.month         = rs.getInt("Month");
                    mr.totalInvoices = rs.getInt("TotalInvoices");
                    mr.ticketsSold   = rs.getInt("TicketsSold");
                    mr.totalRevenue  = rs.getDouble("TotalRevenue");
                    grandTotal   += mr.totalRevenue;
                    grandTickets += mr.ticketsSold;
                    monthlyData.add(mr);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // ── This-month KPIs ────────────────────────────────────────────────
        int curMonth = java.time.LocalDate.now().getMonthValue();
        int curYear  = java.time.Year.now().getValue();

        double monthRevenue  = 0;
        int    monthTickets  = 0;
        int    newCustomers  = 0;

        String sqlCurRev = "SELECT COALESCE(SUM(i.TotalAmount),0) FROM Invoice i "
                + "WHERE i.PaymentStatus='Paid' AND YEAR(i.CreatedAt)=? AND MONTH(i.CreatedAt)=?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlCurRev)) {
            ps.setInt(1, curYear); ps.setInt(2, curMonth);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) monthRevenue = rs.getDouble(1); }
        } catch (SQLException e) { e.printStackTrace(); }

        String sqlCurTkt = "SELECT COUNT(t.TicketID) FROM Ticket t JOIN Invoice i ON t.InvoiceID=i.InvoiceID "
                + "WHERE i.PaymentStatus='Paid' AND YEAR(i.CreatedAt)=? AND MONTH(i.CreatedAt)=?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlCurTkt)) {
            ps.setInt(1, curYear); ps.setInt(2, curMonth);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) monthTickets = rs.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }

        String sqlNewCust = "SELECT COUNT(*) FROM Account WHERE RoleID=2 AND YEAR(CreatedAt)=? AND MONTH(CreatedAt)=?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlNewCust)) {
            ps.setInt(1, curYear); ps.setInt(2, curMonth);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) newCustomers = rs.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }

        // ── Top 5 movies by ticket revenue (UC49) ─────────────────────────
        String sqlTopMovies =
                "SELECT TOP 5 m.MovieID, m.MovieName, "
                + "COUNT(t.TicketID) AS TotalTicketsSold, "
                + "SUM(t.PriceAtBooking) AS TicketRevenue "
                + "FROM Movie m "
                + "JOIN Schedule sc ON sc.MovieID = m.MovieID "
                + "JOIN Ticket t ON t.ScheduleID = sc.ScheduleID "
                + "JOIN Invoice i ON i.InvoiceID = t.InvoiceID "
                + "  AND i.PaymentStatus = 'Paid' AND YEAR(i.CreatedAt) = ? "
                + "GROUP BY m.MovieID, m.MovieName "
                + "ORDER BY TicketRevenue DESC";

        List<MovieRevenueStat> topMovies = new ArrayList<>();
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlTopMovies)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MovieRevenueStat ms = new MovieRevenueStat();
                    ms.movieName    = rs.getString("MovieName");
                    ms.totalTickets = rs.getInt("TotalTicketsSold");
                    ms.revenue      = rs.getDouble("TicketRevenue");
                    topMovies.add(ms);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // ── Revenue by payment method (UC49) ──────────────────────────────
        String sqlPayment =
                "SELECT i.PaymentMethod, COUNT(*) AS TotalInvoices, SUM(i.TotalAmount) AS TotalRevenue "
                + "FROM Invoice i "
                + "WHERE i.PaymentStatus = 'Paid' AND YEAR(i.CreatedAt) = ? "
                + "GROUP BY i.PaymentMethod "
                + "ORDER BY TotalRevenue DESC";

        List<PaymentStat> paymentStats = new ArrayList<>();
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlPayment)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PaymentStat stat = new PaymentStat();
                    stat.method        = rs.getString("PaymentMethod");
                    stat.totalInvoices = rs.getInt("TotalInvoices");
                    stat.revenue       = rs.getDouble("TotalRevenue");
                    paymentStats.add(stat);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("monthlyData",   monthlyData);
        request.setAttribute("grandTotal",     String.format("%,.0f", grandTotal));
        request.setAttribute("grandTickets",   grandTickets);
        request.setAttribute("selectedYear",   year);
        request.setAttribute("sortBy",         sortByParam != null ? sortByParam : "month");
        request.setAttribute("sortDir",        dirParam    != null ? dirParam    : "DESC");
        request.setAttribute("monthRevenue",   String.format("%,.0f", monthRevenue));
        request.setAttribute("monthTickets",   monthTickets);
        request.setAttribute("newCustomers",   newCustomers);
        request.setAttribute("topMovies",      topMovies);
        request.setAttribute("paymentStats",   paymentStats);
        request.getRequestDispatcher(ANALYTICS_JSP).forward(request, response);
    }

    // ─── DTOs ─────────────────────────────────────────────────────────────────

    public static class MonthlyRevenue {
        public int month;
        public int totalInvoices;
        public int ticketsSold;
        public double totalRevenue;

        public int    getMonth()           { return month; }
        public int    getTotalInvoices()   { return totalInvoices; }
        public int    getTicketsSold()     { return ticketsSold; }
        public double getTotalRevenue()    { return totalRevenue; }
        public String getMonthName() {
            String[] n = {"","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
            return month >= 1 && month <= 12 ? n[month] : String.valueOf(month);
        }
        public String getFormattedRevenue() { return String.format("%,.0f", totalRevenue); }
    }

    public static class PaymentStat {
        public String method;
        public int    totalInvoices;
        public double revenue;

        public String getMethod()          { return method; }
        public int    getTotalInvoices()   { return totalInvoices; }
        public String getFormattedRevenue(){ return String.format("%,.0f", revenue); }
    }

    public static class MovieRevenueStat {
        public String movieName;
        public int    totalTickets;
        public double revenue;

        public String getMovieName()       { return movieName; }
        public int    getTotalTickets()    { return totalTickets; }
        public String getFormattedRevenue(){ return String.format("%,.0f", revenue); }
        public double getRevenue()         { return revenue; }
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
        return "Manager Dashboard + Analytics Servlet";
    }
}
