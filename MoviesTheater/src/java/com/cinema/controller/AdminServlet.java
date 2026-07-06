package com.cinema.controller;

import com.cinema.model.Account;
import com.cinema.util.DBUtils;
import com.cinema.util.SystemLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class AdminServlet extends HttpServlet {

    private static final String DASHBOARD_JSP = "/view/admin/dashboard.jsp";
    private static final String LOGS_JSP      = "/view/admin/logs/index.jsp";
    private static final String CONFIG_JSP    = "/view/admin/config/index.jsp";

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
            case "/config":
                if ("POST".equalsIgnoreCase(request.getMethod())) {
                    saveConfig(request, response);
                } else {
                    showConfig(request, response);
                }
                break;
            default:
                showDashboard(request, response);
        }
    }

    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int totalUsers = 0;
        int totalStaff = 0;

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Account WHERE RoleID = 2");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) totalUsers = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Account WHERE RoleID IN (3,4)");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) totalStaff = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }

        // 7-day system activity chart, zero-filled
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate start = today.minusDays(6);
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
        Map<java.time.LocalDate, Integer> countByDay = new LinkedHashMap<>();
        for (java.time.LocalDate d = start; !d.isAfter(today); d = d.plusDays(1)) {
            countByDay.put(d, 0);
        }

        String sqlChart = "SELECT CAST(CreatedAt AS DATE) AS D, COUNT(*) AS Cnt FROM SystemLog "
                + "WHERE CAST(CreatedAt AS DATE) BETWEEN ? AND ? GROUP BY CAST(CreatedAt AS DATE)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlChart)) {
            ps.setDate(1, java.sql.Date.valueOf(start));
            ps.setDate(2, java.sql.Date.valueOf(today));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    countByDay.put(rs.getDate("D").toLocalDate(), rs.getInt("Cnt"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        List<String> chartLabels = new ArrayList<>();
        List<Integer> chartValues = new ArrayList<>();
        for (Map.Entry<java.time.LocalDate, Integer> entry : countByDay.entrySet()) {
            chartLabels.add(entry.getKey().format(fmt));
            chartValues.add(entry.getValue());
        }
        Map<String, Object> chartMap = new LinkedHashMap<>();
        chartMap.put("labels", chartLabels);
        chartMap.put("values", chartValues);

        request.setAttribute("adminTotalUsers",   totalUsers);
        request.setAttribute("adminTotalStaff",   totalStaff);
        request.setAttribute("activityChartJson", new com.google.gson.Gson().toJson(chartMap));
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

    // ─── UC 50: System Config ────────────────────────────────────────────────

    private void showConfig(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String ok  = (String) session.getAttribute("flashSuccess");
            String err = (String) session.getAttribute("flashError");
            if (ok  != null) { request.setAttribute("flashSuccess", ok);  session.removeAttribute("flashSuccess"); }
            if (err != null) { request.setAttribute("flashError",   err); session.removeAttribute("flashError"); }
        }

        Map<String, String> config = loadConfig();
        request.setAttribute("config", config);
        request.getRequestDispatcher(CONFIG_JSP).forward(request, response);
    }

    private void saveConfig(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String[] keys = {
            "cinema_name", "cinema_address", "cinema_phone", "cinema_email",
            "banner_url", "max_seats_per_booking", "cancel_hours_before", "base_ticket_price"
        };

        Account actor = (Account) request.getSession().getAttribute("account");
        Integer updatedBy = actor != null ? actor.getAccountId() : null;

        // SQL Server MERGE for upsert
        String mergeSql =
            "MERGE SystemConfig AS t "
            + "USING (VALUES (?, ?, ?)) AS s(ConfigKey, ConfigValue, UpdatedBy) ON t.ConfigKey = s.ConfigKey "
            + "WHEN MATCHED THEN "
            + "  UPDATE SET ConfigValue = s.ConfigValue, UpdatedAt = GETDATE(), UpdatedBy = s.UpdatedBy "
            + "WHEN NOT MATCHED THEN "
            + "  INSERT (ConfigKey, ConfigValue, UpdatedAt, UpdatedBy) "
            + "  VALUES (s.ConfigKey, s.ConfigValue, GETDATE(), s.UpdatedBy);";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(mergeSql)) {
            for (String key : keys) {
                String value = request.getParameter(key);
                if (value == null) value = "";
                ps.setString(1, key);
                ps.setString(2, value.trim());
                if (updatedBy != null) ps.setInt(3, updatedBy); else ps.setNull(3, Types.INTEGER);
                ps.addBatch();
            }
            ps.executeBatch();

            SystemLogService.log(updatedBy, "CONFIG_UPDATE",
                    "System configuration updated", request.getRemoteAddr());

            request.getSession().setAttribute("flashSuccess", "Cấu hình hệ thống đã được lưu.");
        } catch (SQLException e) {
            e.printStackTrace();
            request.getSession().setAttribute("flashError", "Lỗi khi lưu cấu hình: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/config");
    }

    private Map<String, String> loadConfig() {
        Map<String, String> cfg = new LinkedHashMap<>();
        cfg.put("cinema_name",           "CGV Cinema");
        cfg.put("cinema_address",        "Hà Nội, Việt Nam");
        cfg.put("cinema_phone",          "1900 6017");
        cfg.put("cinema_email",          "hotro@cgv.vn");
        cfg.put("banner_url",            "");
        cfg.put("max_seats_per_booking", "8");
        cfg.put("cancel_hours_before",   "2");
        cfg.put("base_ticket_price",     "90000");

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT ConfigKey, ConfigValue FROM SystemConfig");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cfg.put(rs.getString("ConfigKey"), rs.getString("ConfigValue"));
        } catch (SQLException e) {
            // SystemConfig table may not exist yet — defaults are used
            e.printStackTrace();
        }
        return cfg;
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
