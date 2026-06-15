package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.dao.AuditLogDAO;
import com.cinema.dao.DeletionRequestDAO;
import com.cinema.model.Account;
import com.cinema.model.DeletionRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class DeletionRequestAdminController extends HttpServlet {

    private static final int PAGE_SIZE = 10;
    private final DeletionRequestDAO deletionRequestDAO = new DeletionRequestDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("review".equals(action)) {
            String idStr = request.getParameter("id");
            if (idStr != null) {
                try {
                    DeletionRequest req = deletionRequestDAO.getRequestById(Integer.parseInt(idStr));
                    if (req != null) {
                        request.setAttribute("deletionRequest", req);
                        request.getRequestDispatcher("/WEB-INF/manager/users/delete-review.jsp").forward(request, response);
                        return;
                    }
                } catch (NumberFormatException ignored) {}
            }
            request.setAttribute("flashError", "Request not found.");
        }

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null) {
            try { page = Integer.parseInt(pageStr); if (page < 1) page = 1; }
            catch (NumberFormatException ignored) {}
        }

        String statusFilter = request.getParameter("status");
        List<DeletionRequest> requests = deletionRequestDAO.getRequests(page, PAGE_SIZE, statusFilter);
        int total = deletionRequestDAO.countRequests(statusFilter);
        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
        if (totalPages < 1) totalPages = 1;

        request.setAttribute("requests", requests);
        request.setAttribute("total", total);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.getRequestDispatcher("/WEB-INF/manager/users/delete-list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Account admin = (Account) request.getSession().getAttribute("account");
        if (admin == null || admin.getRoleId() < 5) {
            request.getSession().setAttribute("flashError", "Access denied.");
            response.sendRedirect(request.getContextPath() + "/manager/users");
            return;
        }

        String action = request.getParameter("action");
        String idStr = request.getParameter("id");
        String note = request.getParameter("reviewNote");

        if (idStr == null) {
            request.getSession().setAttribute("flashError", "Invalid request.");
            response.sendRedirect(request.getContextPath() + "/manager/deletion-requests");
            return;
        }

        try {
            int requestId = Integer.parseInt(idStr);
            DeletionRequest req = deletionRequestDAO.getRequestById(requestId);
            if (req == null) {
                request.getSession().setAttribute("flashError", "Request not found.");
                response.sendRedirect(request.getContextPath() + "/manager/deletion-requests");
                return;
            }

            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
            String adminName = admin.getProfile() != null ? admin.getProfile().getFullName() : "Admin";

            if ("approve".equals(action)) {
                deletionRequestDAO.approveRequest(requestId, admin.getAccountId(), note);
                deletionRequestDAO.deleteAccountByRequest(req.getAccountId());
                auditLogDAO.log(admin.getAccountId(), "DELETE_APPROVE",
                    adminName + " approved deletion of " + req.getAccountEmail(), ip);
                request.getSession().setAttribute("flashSuccess", "Deletion approved. Account has been deleted.");
            } else if ("reject".equals(action)) {
                deletionRequestDAO.rejectRequest(requestId, admin.getAccountId(), note);
                auditLogDAO.log(admin.getAccountId(), "DELETE_REJECT",
                    adminName + " rejected deletion request from " + req.getAccountEmail(), ip);
                request.getSession().setAttribute("flashSuccess", "Deletion request rejected.");
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("flashError", "Invalid request ID.");
        }

        response.sendRedirect(request.getContextPath() + "/manager/deletion-requests");
    }
}
