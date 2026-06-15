package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.dao.AuditLogDAO;
import com.cinema.dao.NotificationDAO;
import com.cinema.model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class AccountManageController extends HttpServlet {

    private static final int PAGE_SIZE = 10;
    private final AccountDAO accountDAO = new AccountDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    private boolean isAdmin(HttpServletRequest request) {
        Account account = (Account) request.getSession().getAttribute("account");
        return account != null && account.getRoleId() == 5;
    }

    private int currentAccountId(HttpServletRequest request) {
        Account account = (Account) request.getSession().getAttribute("account");
        return account != null ? account.getAccountId() : -1;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return ip;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("edit".equals(action)) {
            String idStr = request.getParameter("id");
            if (idStr != null) {
                try {
                    int id = Integer.parseInt(idStr);
                    Account account = accountDAO.getAccountById(id);
                    if (account != null) {
                        request.setAttribute("editAccount", account);
                        request.getRequestDispatcher("/WEB-INF/manager/users/edit.jsp").forward(request, response);
                        return;
                    }
                } catch (NumberFormatException e) {
                    // fall through
                }
            }
            request.setAttribute("flashError", "User not found.");
        }

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null) {
            try {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            } catch (NumberFormatException ignored) {}
        }

        String search = request.getParameter("q");
        String roleFilter = request.getParameter("role");

        List<Account> userList = accountDAO.getAllAccounts(page, PAGE_SIZE, search, roleFilter);
        int totalUsers = accountDAO.countAccounts(search, roleFilter);
        int totalPages = (int) Math.ceil((double) totalUsers / PAGE_SIZE);
        if (totalPages < 1) totalPages = 1;

        int staffCount = accountDAO.countStaff();
        int lockedCount = accountDAO.countLocked();
        List<Account> recentAccounts = accountDAO.getRecentAccounts(5);

        request.setAttribute("userList", userList);
        request.setAttribute("totalUsers", totalUsers);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("staffCount", staffCount);
        request.setAttribute("lockedCount", lockedCount);
        request.setAttribute("recentActivity", recentAccounts);
        request.setAttribute("isAdmin", isAdmin(request));

        request.getRequestDispatcher("/WEB-INF/manager/users/list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if (!isAdmin(request)) {
            request.getSession().setAttribute("flashError", "Only Admin can perform this action.");
            response.sendRedirect(request.getContextPath() + "/manager/users");
            return;
        }

        int adminId = currentAccountId(request);
        Account admin = accountDAO.getAccountById(adminId);
        String adminName = admin != null && admin.getProfile() != null ? admin.getProfile().getFullName() : "Admin";
        String ip = getClientIp(request);

        if ("updateRole".equals(action)) {
            handleUpdateRole(request, adminId, adminName, ip);
        } else if ("toggleBlock".equals(action)) {
            handleToggleBlock(request, adminId, adminName, ip);
        } else if ("bulkBlock".equals(action)) {
            handleBulkAction(request, true, adminId, adminName, ip);
        } else if ("bulkUnblock".equals(action)) {
            handleBulkAction(request, false, adminId, adminName, ip);
        } else if ("bulkRole".equals(action)) {
            handleBulkRole(request, adminId, adminName, ip);
        }

        String redirect = request.getContextPath() + "/manager/users";
        String q = request.getParameter("q");
        String role = request.getParameter("role");
        String page = request.getParameter("page");
        StringBuilder sb = new StringBuilder(redirect);
        if (q != null || role != null || page != null) {
            sb.append("?");
            if (q != null) sb.append("q=").append(q);
            if (role != null) sb.append((q != null ? "&" : "")).append("role=").append(role);
            if (page != null) sb.append((q != null || role != null ? "&" : "")).append("page=").append(page);
        }
        response.sendRedirect(sb.toString());
    }

    private void handleUpdateRole(HttpServletRequest request, int adminId, String adminName, String ip) {
        String idStr = request.getParameter("id");
        String newRoleStr = request.getParameter("newRole");
        if (idStr != null && newRoleStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                int newRole = Integer.parseInt(newRoleStr);
                if (id == currentAccountId(request)) {
                    request.getSession().setAttribute("flashError", "Cannot change your own role.");
                } else if (newRole >= 5) {
                    request.getSession().setAttribute("flashError", "Cannot assign Admin role.");
                } else if (newRole < 2) {
                    request.getSession().setAttribute("flashError", "Invalid role.");
                } else {
                    Account target = accountDAO.getAccountById(id);
                    String oldRole = target != null ? target.getRoleName() : "?";
                    accountDAO.updateRole(id, newRole);
                    String[] roleNames = {"", "", "Customer", "Employee", "Manager"};
                    String newRoleName = newRole < roleNames.length ? roleNames[newRole] : "?";
                    auditLogDAO.log(adminId, "UPDATE_ROLE",
                        adminName + " changed role of " + (target != null ? target.getEmail() : "ID:" + id)
                        + " from " + oldRole + " to " + newRoleName, ip);
                    notificationDAO.createNotification("ROLE_CHANGE",
                        adminName + " changed " + (target != null ? target.getEmail() : "ID:" + id) + " role to " + newRoleName,
                        request.getContextPath() + "/manager/users?action=edit&id=" + id);
                    request.getSession().setAttribute("flashSuccess", "Role updated successfully.");
                }
            } catch (NumberFormatException e) {
                request.getSession().setAttribute("flashError", "Invalid input.");
            }
        }
    }

    private void handleToggleBlock(HttpServletRequest request, int adminId, String adminName, String ip) {
        String idStr = request.getParameter("id");
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                if (id == currentAccountId(request)) {
                    request.getSession().setAttribute("flashError", "Cannot block yourself.");
                } else {
                    Account target = accountDAO.getAccountById(id);
                    boolean wasBlocked = target != null && target.isIsBlocked();
                    accountDAO.toggleBlock(id);
                    String action = wasBlocked ? "unblocked" : "blocked";
                    auditLogDAO.log(adminId, wasBlocked ? "UNBLOCK_USER" : "BLOCK_USER",
                        adminName + " " + action + " " + (target != null ? target.getEmail() : "ID:" + id), ip);
                    notificationDAO.createNotification(wasBlocked ? "UNBLOCK_USER" : "BLOCK_USER",
                        adminName + " " + action + " " + (target != null ? target.getEmail() : "ID:" + id),
                        request.getContextPath() + "/manager/users");
                    request.getSession().setAttribute("flashSuccess", "User " + action + " successfully.");
                }
            } catch (NumberFormatException e) {
                request.getSession().setAttribute("flashError", "Invalid user ID.");
            }
        }
    }

    private void handleBulkAction(HttpServletRequest request, boolean block, int adminId, String adminName, String ip) {
        String[] ids = request.getParameterValues("selectedIds");
        if (ids == null || ids.length == 0) {
            request.getSession().setAttribute("flashError", "No users selected.");
            return;
        }
        int count = 0;
        int selfId = currentAccountId(request);
        for (String idStr : ids) {
            try {
                int id = Integer.parseInt(idStr);
                if (id == selfId) continue;
                Account target = accountDAO.getAccountById(id);
                if (target == null) continue;
                boolean currentlyBlocked = target.isIsBlocked();
                if (block && !currentlyBlocked) {
                    accountDAO.toggleBlock(id);
                    count++;
                } else if (!block && currentlyBlocked) {
                    accountDAO.toggleBlock(id);
                    count++;
                }
            } catch (NumberFormatException ignored) {}
        }
        String actionLabel = block ? "blocked" : "unblocked";
        auditLogDAO.log(adminId, "BULK_" + (block ? "BLOCK" : "UNBLOCK"),
            adminName + " " + actionLabel + " " + count + " user(s)", ip);
        notificationDAO.createNotification("BULK_" + (block ? "BLOCK" : "UNBLOCK"),
            adminName + " " + actionLabel + " " + count + " user(s)",
            request.getContextPath() + "/manager/users");
        request.getSession().setAttribute("flashSuccess", count + " user(s) " + actionLabel + ".");
    }

    private void handleBulkRole(HttpServletRequest request, int adminId, String adminName, String ip) {
        String[] ids = request.getParameterValues("selectedIds");
        String newRoleStr = request.getParameter("bulkRole");
        if (ids == null || ids.length == 0) {
            request.getSession().setAttribute("flashError", "No users selected.");
            return;
        }
        if (newRoleStr == null) {
            request.getSession().setAttribute("flashError", "No role selected.");
            return;
        }
        try {
            int newRole = Integer.parseInt(newRoleStr);
            if (newRole >= 5 || newRole < 2) {
                request.getSession().setAttribute("flashError", "Invalid role.");
                return;
            }
            int count = 0;
            int selfId = currentAccountId(request);
            for (String idStr : ids) {
                try {
                    int id = Integer.parseInt(idStr);
                    if (id == selfId) continue;
                    accountDAO.updateRole(id, newRole);
                    count++;
                } catch (NumberFormatException ignored) {}
            }
            String[] roleNames = {"", "", "Customer", "Employee", "Manager"};
            String newRoleName = newRole < roleNames.length ? roleNames[newRole] : "?";
            auditLogDAO.log(adminId, "BULK_ROLE",
                adminName + " changed role of " + count + " user(s) to " + newRoleName, ip);
            notificationDAO.createNotification("BULK_ROLE",
                adminName + " changed role of " + count + " user(s) to " + newRoleName,
                request.getContextPath() + "/manager/users");
            request.getSession().setAttribute("flashSuccess", count + " user(s) role changed to " + newRoleName + ".");
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("flashError", "Invalid role.");
        }
    }
}
