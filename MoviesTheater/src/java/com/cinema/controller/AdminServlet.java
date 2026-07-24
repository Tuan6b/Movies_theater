package com.cinema.controller;

import com.cinema.dao.AccountAdminDAO;
import com.cinema.dao.AccountDAO;
import com.cinema.model.Account;
import com.cinema.util.DBUtils;
import com.cinema.util.MailUtil;
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
    private static final String USERS_JSP     = "/view/admin/users.jsp";
    private static final String CUSTOMERS_JSP = "/view/admin/customers.jsp";
    private static final String STAFF_JSP     = "/view/admin/staff.jsp";
    private static final String CREATE_ACCOUNT_JSP = "/view/admin/create-account.jsp";
    private static final char[] CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final Random RANDOM = new Random();

    private String generateCaptchaText() {
        StringBuilder sb = new StringBuilder(5);
        for (int i = 0; i < 5; i++) {
            sb.append(CAPTCHA_CHARS[RANDOM.nextInt(CAPTCHA_CHARS.length)]);
        }
        return sb.toString();
    }

    private static final int PAGE_SIZE = 50;

    private final AccountAdminDAO adminDAO = new AccountAdminDAO();
    private final AccountDAO accountDAO = new AccountDAO();

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
            case "/users":
            case "/customers":
                handleCustomers(request, response);
                break;
            case "/staff":
                handleStaff(request, response);
                break;
            case "/create-account":
                handleCreateAccount(request, response);
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

    private void sendBlockNotification(int userId, boolean blocked, String remoteAddr) {
        try {
            Account u = accountDAO.getAccountById(userId);
            if (u == null || u.getEmail() == null) return;
            String subject = blocked ? "Tài khoản CGV Cinema của bạn đã bị khóa" : "Tài khoản CGV Cinema của bạn đã được mở khóa";
            String body = blocked
                ? "<p>Xin chào <strong>" + MailUtil.escape(u.getFullName()) + "</strong>,</p>"
                + "<p>Tài khoản CGV Cinema (<strong>" + MailUtil.escape(u.getEmail()) + "</strong>) của bạn đã bị khóa bởi quản trị viên.</p>"
                + "<p>Nếu bạn cho rằng đây là sai sót, vui lòng gửi yêu cầu mở khóa qua trang web.</p>"
                + "<p>Trân trọng,<br><strong>CGV Cinema Team</strong></p>"
                : "<p>Xin chào <strong>" + MailUtil.escape(u.getFullName()) + "</strong>,</p>"
                + "<p>Tài khoản CGV Cinema (<strong>" + MailUtil.escape(u.getEmail()) + "</strong>) của bạn đã được mở khóa.</p>"
                + "<p>Bạn có thể đăng nhập và tiếp tục sử dụng dịch vụ của chúng tôi.</p>"
                + "<p>Trân trọng,<br><strong>CGV Cinema Team</strong></p>";
            MailUtil.sendNotificationEmail(u.getEmail(), u.getFullName(), subject, body);
        } catch (Exception e) {
            System.err.println("[EMAIL_NOTIFICATION_FAILED] userId=" + userId + " blocked=" + blocked + " : " + e.getMessage());
        }
    }

    private void handleCustomers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String method = request.getMethod();
        HttpSession session = request.getSession(false);
        Account admin = (Account) session.getAttribute("account");
        int adminId = admin != null ? admin.getAccountId() : 0;

        if ("POST".equalsIgnoreCase(method)) {
            String action = request.getParameter("action");

            if ("batch-block".equals(action) || "batch-unblock".equals(action)) {
                String[] ids = request.getParameterValues("userIds");
                if (ids != null) {
                    boolean block = "batch-block".equals(action);
                    for (String id : ids) {
                        int uid = Integer.parseInt(id);
                        adminDAO.setBlocked(uid, block);
                        sendBlockNotification(uid, block, request.getRemoteAddr());
                    }
                    SystemLogService.log(adminId, block ? "BATCH_BLOCK" : "BATCH_UNBLOCK",
                            "Batch " + (block ? "blocked" : "unblocked") + " " + ids.length + " users",
                            request.getRemoteAddr());
                }
                response.sendRedirect(request.getContextPath() + "/admin/customers"
                    + "?q=" + (request.getParameter("q") != null ? request.getParameter("q") : "")
                    + "&status=" + (request.getParameter("status") != null ? request.getParameter("status") : "")
                    + "&sortBy=" + (request.getParameter("sortBy") != null ? request.getParameter("sortBy") : "")
                    + "&sortOrder=" + (request.getParameter("sortOrder") != null ? request.getParameter("sortOrder") : ""));
                return;
            }

            String actionType = request.getParameter("actionType");
            if ("export-csv".equals(actionType)) {
                exportCustomersCsv(request, response);
                return;
            }

            String userIdParam = request.getParameter("userId");
            if (userIdParam != null) {
                int userId = Integer.parseInt(userIdParam);
                if ("block".equals(action)) {
                    adminDAO.setBlocked(userId, true);
                    sendBlockNotification(userId, true, request.getRemoteAddr());
                    SystemLogService.log(adminId, "USER_BLOCKED", "Blocked user #" + userId, request.getRemoteAddr());
                } else if ("unblock".equals(action)) {
                    adminDAO.setBlocked(userId, false);
                    sendBlockNotification(userId, false, request.getRemoteAddr());
                    SystemLogService.log(adminId, "USER_UNBLOCKED", "Unblocked user #" + userId, request.getRemoteAddr());
                } else if ("role".equals(action)) {
                    int newRole = Integer.parseInt(request.getParameter("roleId"));
                    adminDAO.updateRole(userId, newRole);
                    SystemLogService.log(adminId, "ROLE_CHANGED", "Changed user #" + userId + " to role " + newRole, request.getRemoteAddr());
                }
            }

            String redirect = request.getContextPath() + "/admin/customers";
            String q = request.getParameter("q");
            String status = request.getParameter("status");
            String sortBy = request.getParameter("sortBy");
            String sortOrder = request.getParameter("sortOrder");
            StringBuilder sb = new StringBuilder(redirect);
            boolean first = true;
            if (q != null && !q.isEmpty()) { sb.append(first ? "?" : "&").append("q=").append(java.net.URLEncoder.encode(q, "UTF-8")); first = false; }
            if (status != null && !status.isEmpty()) { sb.append(first ? "?" : "&").append("status=").append(status); first = false; }
            if (sortBy != null && !sortBy.isEmpty()) { sb.append(first ? "?" : "&").append("sortBy=").append(sortBy); first = false; }
            if (sortOrder != null && !sortOrder.isEmpty()) { sb.append(first ? "?" : "&").append("sortOrder=").append(sortOrder); }
            response.sendRedirect(sb.toString());
            return;
        }

        String query = request.getParameter("q");
        String statusFilter = request.getParameter("status");
        String sortBy = request.getParameter("sortBy");
        String sortOrder = request.getParameter("sortOrder");

        List<Account> customers = adminDAO.searchByRoles(
                java.util.Arrays.asList(2), query, statusFilter, sortBy, sortOrder);
        Map<String, Integer> roleStats = adminDAO.getRoleStats();

        request.setAttribute("customers", customers);
        request.setAttribute("roleStats", roleStats);
        request.setAttribute("q", query);
        request.setAttribute("statusFilter", statusFilter);
        request.setAttribute("sortBy", sortBy);
        request.setAttribute("sortOrder", sortOrder);
        request.getRequestDispatcher(CUSTOMERS_JSP).forward(request, response);
    }

    private void exportCustomersCsv(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String query = request.getParameter("q");
        String statusFilter = request.getParameter("status");

        List<Account> customers = adminDAO.searchByRoles(
                java.util.Arrays.asList(2), query, statusFilter, null, null);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=customers.csv");

        try (java.io.PrintWriter w = response.getWriter()) {
            w.write('\uFEFF');
            w.println("ID,H\u1ecd v\u00e0 t\u00ean,Email,S\u1ed1 \u0111i\u1ec7n tho\u1ea1i,Vai tr\u00f2,Tr\u1ea1ng th\u00e1i,Ng\u00e0y t\u1ea1o");
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (Account a : customers) {
                w.print(a.getAccountId());
                w.print(",");
                String name = a.getFullName() != null ? a.getFullName().replace("\"", "\"\"") : "";
                w.print("\"" + name + "\"");
                w.print(",");
                w.print(a.getEmail());
                w.print(",");
                w.print(a.getPhoneNumber() != null ? a.getPhoneNumber() : "");
                w.print(",");
                w.print("Customer");
                w.print(",");
                w.print(a.isIsBlocked() ? "B\u1ecb kh\u00f3a" : "Ho\u1ea1t \u0111\u1ed9ng");
                w.print(",");
                w.print(a.getCreatedAt() != null ? sdf.format(java.sql.Timestamp.valueOf(a.getCreatedAt())) : "");
                w.println();
            }
        }
    }

    private void handleStaff(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String method = request.getMethod();
        HttpSession session = request.getSession(false);
        Account admin = (Account) session.getAttribute("account");
        int adminId = admin != null ? admin.getAccountId() : 0;

        if ("POST".equalsIgnoreCase(method)) {
            String action = request.getParameter("action");

            if ("batch-block".equals(action) || "batch-unblock".equals(action)) {
                String[] ids = request.getParameterValues("userIds");
                if (ids != null) {
                    boolean block = "batch-block".equals(action);
                    for (String id : ids) {
                        int uid = Integer.parseInt(id);
                        adminDAO.setBlocked(uid, block);
                        sendBlockNotification(uid, block, request.getRemoteAddr());
                    }
                    SystemLogService.log(adminId, block ? "BATCH_BLOCK" : "BATCH_UNBLOCK",
                            "Batch " + (block ? "blocked" : "unblocked") + " " + ids.length + " staff",
                            request.getRemoteAddr());
                }
                String redirect = buildStaffRedirect(request);
                response.sendRedirect(redirect);
                return;
            }

            String actionType = request.getParameter("actionType");
            if ("export-csv".equals(actionType)) {
                exportStaffCsv(request, response);
                return;
            }

            String userIdParam = request.getParameter("userId");
            if (userIdParam != null) {
                int userId = Integer.parseInt(userIdParam);
                if ("block".equals(action)) {
                    adminDAO.setBlocked(userId, true);
                    sendBlockNotification(userId, true, request.getRemoteAddr());
                    SystemLogService.log(adminId, "USER_BLOCKED", "Blocked staff #" + userId, request.getRemoteAddr());
                } else if ("unblock".equals(action)) {
                    adminDAO.setBlocked(userId, false);
                    sendBlockNotification(userId, false, request.getRemoteAddr());
                    SystemLogService.log(adminId, "USER_UNBLOCKED", "Unblocked staff #" + userId, request.getRemoteAddr());
                } else if ("role".equals(action)) {
                    int newRole = Integer.parseInt(request.getParameter("roleId"));
                    adminDAO.updateRole(userId, newRole);
                    SystemLogService.log(adminId, "ROLE_CHANGED", "Changed staff #" + userId + " to role " + newRole, request.getRemoteAddr());
                }
            }

            response.sendRedirect(buildStaffRedirect(request));
            return;
        }

        String query = request.getParameter("q");
        String statusFilter = request.getParameter("status");
        String sortBy = request.getParameter("sortBy");
        String sortOrder = request.getParameter("sortOrder");

        List<Account> staff = adminDAO.searchByRoles(
                java.util.Arrays.asList(3, 4, 5), query, statusFilter, sortBy, sortOrder);
        Map<String, Integer> roleStats = adminDAO.getRoleStats();

        request.setAttribute("staff", staff);
        request.setAttribute("roleStats", roleStats);
        request.setAttribute("q", query);
        request.setAttribute("statusFilter", statusFilter);
        request.setAttribute("sortBy", sortBy);
        request.setAttribute("sortOrder", sortOrder);
        request.getRequestDispatcher(STAFF_JSP).forward(request, response);
    }

    private void handleCreateAccount(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            request.setCharacterEncoding("UTF-8");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String fullName = request.getParameter("fullName");
            String phoneNumber = request.getParameter("phoneNumber");
            String roleIdStr = request.getParameter("roleId");
            String captcha = request.getParameter("captcha");

            HttpSession session = request.getSession();
            String captchaExpected = (String) session.getAttribute("captcha");

            Map<String, String> errors = new HashMap<>();
            if (fullName == null || fullName.trim().isEmpty()) errors.put("fullName", "Vui lòng nhập họ tên.");
            if (email == null || email.trim().isEmpty()) errors.put("email", "Vui lòng nhập email.");
            else if (!java.util.regex.Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matcher(email.trim()).matches())
                errors.put("email", "Email không hợp lệ.");
            if (password == null || password.length() < 6) errors.put("password", "Mật khẩu phải có ít nhất 6 ký tự.");
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                if (!java.util.regex.Pattern.compile("^(0[35789])([0-9]{8})$").matcher(phoneNumber.trim()).matches())
                    errors.put("phoneNumber", "Số điện thoại không hợp lệ (phải gồm 10 chữ số, bắt đầu bằng 03/05/07/08/09).");
            }
            if (captchaExpected == null || captcha == null || !captchaExpected.equalsIgnoreCase(captcha.trim())) {
                errors.put("captcha", "Mã xác nhận không đúng.");
            }

            int roleId = 2;
            if (roleIdStr != null) {
                try { roleId = Integer.parseInt(roleIdStr); if (roleId < 2 || roleId > 5) roleId = 2; }
                catch (NumberFormatException e) { roleId = 2; }
            }
            Account admin = (Account) session.getAttribute("account");
            if (admin != null && admin.getRoleId() != 5) {
                if (roleId == 5) roleId = 4;
            }

            if (!errors.isEmpty()) {
                String newCaptcha = generateCaptchaText();
                session.setAttribute("captcha", newCaptcha);
                request.setAttribute("captchaText", newCaptcha);
                request.setAttribute("errors", errors);
                request.setAttribute("fullName", fullName);
                request.setAttribute("email", email);
                request.setAttribute("phoneNumber", phoneNumber);
                request.setAttribute("selectedRole", roleId);
                request.getRequestDispatcher(CREATE_ACCOUNT_JSP).forward(request, response);
                return;
            }
            session.removeAttribute("captcha");

            if (adminDAO.isEmailExist(email.trim())) {
                Map<String, String> err = new HashMap<>();
                err.put("email", "Email này đã được sử dụng.");
                String newCaptcha = generateCaptchaText();
                session.setAttribute("captcha", newCaptcha);
                request.setAttribute("captchaText", newCaptcha);
                request.setAttribute("errors", err);
                request.setAttribute("fullName", fullName);
                request.setAttribute("email", email);
                request.setAttribute("phoneNumber", phoneNumber);
                request.setAttribute("selectedRole", roleId);
                request.getRequestDispatcher(CREATE_ACCOUNT_JSP).forward(request, response);
                return;
            }

            int accountId = adminDAO.createAccount(email, password, fullName, phoneNumber, roleId);
            if (accountId > 0) {
                int adminId = admin != null ? admin.getAccountId() : 0;
                SystemLogService.log(adminId, "ACCOUNT_CREATED",
                        "Admin created account #" + accountId + " (" + email + ") role=" + roleId,
                        request.getRemoteAddr());
                try {
                    MailUtil.sendWelcomeEmail(email.trim(), fullName != null ? fullName.trim() : "");
                } catch (Exception ignored) {}
                session.removeAttribute("captcha");
                request.getSession().setAttribute("flashSuccess", "Tạo tài khoản thành công.");
                response.sendRedirect(request.getContextPath() + "/admin/create-account");
            } else {
                request.setAttribute("error", "Lỗi khi tạo tài khoản. Vui lòng thử lại.");
                String newCaptcha = generateCaptchaText();
                session.setAttribute("captcha", newCaptcha);
                request.setAttribute("captchaText", newCaptcha);
                request.setAttribute("fullName", fullName);
                request.setAttribute("email", email);
                request.setAttribute("phoneNumber", phoneNumber);
                request.setAttribute("selectedRole", roleId);
                request.getRequestDispatcher(CREATE_ACCOUNT_JSP).forward(request, response);
            }
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                String ok  = (String) session.getAttribute("flashSuccess");
                String err = (String) session.getAttribute("flashError");
                if (ok  != null) { request.setAttribute("flashSuccess", ok);  session.removeAttribute("flashSuccess"); }
                if (err != null) { request.setAttribute("flashError",   err); session.removeAttribute("flashError"); }
            }
            // Generate captcha on GET
            String captchaText = generateCaptchaText();
            if (session == null) session = request.getSession(true);
            session.setAttribute("captcha", captchaText);
            request.setAttribute("captchaText", captchaText);
            // AJAX refresh captcha
            if ("1".equals(request.getParameter("refreshCaptcha"))) {
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write(captchaText);
                return;
            }
            request.getRequestDispatcher(CREATE_ACCOUNT_JSP).forward(request, response);
        }
    }

    private String buildStaffRedirect(HttpServletRequest request) {
        String redirect = request.getContextPath() + "/admin/staff";
        String q = request.getParameter("q");
        String status = request.getParameter("status");
        String sortBy = request.getParameter("sortBy");
        String sortOrder = request.getParameter("sortOrder");
        StringBuilder sb = new StringBuilder(redirect);
        boolean first = true;
        try {
            if (q != null && !q.isEmpty()) { sb.append(first ? "?" : "&").append("q=").append(java.net.URLEncoder.encode(q, "UTF-8")); first = false; }
            if (status != null && !status.isEmpty()) { sb.append(first ? "?" : "&").append("status=").append(status); first = false; }
            if (sortBy != null && !sortBy.isEmpty()) { sb.append(first ? "?" : "&").append("sortBy=").append(sortBy); first = false; }
            if (sortOrder != null && !sortOrder.isEmpty()) { sb.append(first ? "?" : "&").append("sortOrder=").append(sortOrder); }
        } catch (Exception e) {}
        return sb.toString();
    }

    private void exportStaffCsv(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String query = request.getParameter("q");
        String statusFilter = request.getParameter("status");

        List<Account> staff = adminDAO.searchByRoles(
                java.util.Arrays.asList(3, 4, 5), query, statusFilter, null, null);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=staff.csv");

        try (java.io.PrintWriter w = response.getWriter()) {
            w.write('\uFEFF');
            w.println("ID,H\u1ecd v\u00e0 t\u00ean,Email,S\u1ed1 \u0111i\u1ec7n tho\u1ea1i,Vai tr\u00f2,Tr\u1ea1ng th\u00e1i,Ng\u00e0y t\u1ea1o");
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (Account a : staff) {
                w.print(a.getAccountId());
                w.print(",");
                String name = a.getFullName() != null ? a.getFullName().replace("\"", "\"\"") : "";
                w.print("\"" + name + "\"");
                w.print(",");
                w.print(a.getEmail());
                w.print(",");
                w.print(a.getPhoneNumber() != null ? a.getPhoneNumber() : "");
                w.print(",");
                w.print(a.getRoleName() != null ? a.getRoleName() : "");
                w.print(",");
                w.print(a.isIsBlocked() ? "B\u1ecb kh\u00f3a" : "Ho\u1ea1t \u0111\u1ed9ng");
                w.print(",");
                w.print(a.getCreatedAt() != null ? sdf.format(java.sql.Timestamp.valueOf(a.getCreatedAt())) : "");
                w.println();
            }
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
        return "System Admin Dashboard Servlet";
    }
}
