package com.cinema.controller;

import com.cinema.util.DBUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class AdminServlet extends HttpServlet {

    private static final String DASHBOARD_JSP = "/view/admin/dashboard.jsp";
    private static final String LOGS_JSP      = "/view/admin/logs/index.jsp";

    private static final int PAGE_SIZE = 50;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String path = request.getPathInfo();
        if (path == null || path.equals("/") || path.equals("/dashboard")) {
            path = "/dashboard";
        }

        switch (path) {
            case "/logs":
                showLogs(request, response);
                break;
            default:
                showDashboard(request, response);
        }
    }

    /**
     * Landing page for the System Admin. It carries no figures of its own: the user
     * and staff counts it used to show were a report nobody had asked for, and the
     * activity chart only restated what UC51 (View System Logs) already lists in
     * full. What is left is a way in to the pages that do map to a use case.
     */
    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(DASHBOARD_JSP).forward(request, response);
    }

    // ─── UC 51: System Logs ──────────────────────────────────────────────────

    private void showLogs(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String actionFilter = request.getParameter("action");
        String searchQuery  = request.getParameter("q");
        String pageParam    = request.getParameter("page");
        int page = 1;
        try { page = Math.max(1, Integer.parseInt(pageParam)); } catch (Exception ignored) {}

        int offset = (page - 1) * PAGE_SIZE;
        boolean hasAction = actionFilter != null && !actionFilter.trim().isEmpty();
        boolean hasSearch = searchQuery  != null && !searchQuery.trim().isEmpty();

        StringBuilder where = new StringBuilder();
        List<Object> params = new ArrayList<>();

        if (hasAction) {
            where.append("l.ActionType = ? ");
            params.add(actionFilter.trim());
        }
        if (hasSearch) {
            if (where.length() > 0) where.append("AND ");
            where.append("(l.Description LIKE ? OR a.Email LIKE ?) ");
            String like = "%" + searchQuery.trim() + "%";
            params.add(like);
            params.add(like);
        }

        String whereClause = where.length() > 0 ? "WHERE " + where : "";

        String countSql = "SELECT COUNT(*) FROM SystemLog l "
                + "LEFT JOIN Account a ON a.AccountID = l.AccountID " + whereClause;
        String dataSql  = "SELECT l.LogID, l.ActionType, l.Description, l.IPAddress, l.CreatedAt, "
                + "a.Email, up.FullName "
                + "FROM SystemLog l "
                + "LEFT JOIN Account a ON a.AccountID = l.AccountID "
                + "LEFT JOIN UserProfile up ON up.AccountID = l.AccountID "
                + whereClause
                + "ORDER BY l.CreatedAt DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        int totalLogs = 0;
        List<Map<String, Object>> logs = new ArrayList<>();

        try (Connection conn = DBUtils.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) totalLogs = rs.getInt(1);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(dataSql)) {
                int idx = 1;
                for (Object p : params) ps.setObject(idx++, p);
                ps.setInt(idx++, offset);
                ps.setInt(idx,   PAGE_SIZE);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("logId",      rs.getInt("LogID"));
                        row.put("actionType", rs.getString("ActionType"));
                        row.put("description",rs.getString("Description"));
                        row.put("ipAddress",  rs.getString("IPAddress"));
                        row.put("createdAt",  rs.getTimestamp("CreatedAt"));
                        row.put("email",      rs.getString("Email"));
                        row.put("fullName",   rs.getString("FullName"));
                        logs.add(row);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<String> actionTypes = new ArrayList<>();
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT DISTINCT ActionType FROM SystemLog ORDER BY ActionType");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) actionTypes.add(rs.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int totalPages = totalLogs == 0 ? 1 : (int) Math.ceil((double) totalLogs / PAGE_SIZE);

        request.setAttribute("logs",         logs);
        request.setAttribute("totalLogs",    totalLogs);
        request.setAttribute("totalPages",   totalPages);
        request.setAttribute("currentPage",  page);
        request.setAttribute("actionFilter", actionFilter);
        request.setAttribute("searchQuery",  searchQuery);
        request.setAttribute("actionTypes",  actionTypes);
        request.getRequestDispatcher(LOGS_JSP).forward(request, response);
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
        return "System Admin Dashboard Servlet";
    }
}
