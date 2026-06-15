package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.dao.AuditLogDAO;
import com.cinema.dao.DeletionRequestDAO;
import com.cinema.dao.NotificationDAO;
import com.cinema.model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class DeletionRequestController extends HttpServlet {

    private final DeletionRequestDAO deletionRequestDAO = new DeletionRequestDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }
        Account account = (Account) session.getAttribute("account");
        boolean hasPending = deletionRequestDAO.hasPendingRequest(account.getAccountId());
        request.setAttribute("hasPendingRequest", hasPending);
        request.getRequestDispatcher("/WEB-INF/manager/users/delete-request.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }
        Account account = (Account) session.getAttribute("account");

        if (deletionRequestDAO.hasPendingRequest(account.getAccountId())) {
            request.setAttribute("error", "You already have a pending deletion request.");
            request.getRequestDispatcher("/WEB-INF/manager/users/delete-request.jsp").forward(request, response);
            return;
        }

        String reason = request.getParameter("reason");
        deletionRequestDAO.createRequest(account.getAccountId(), reason);

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        auditLogDAO.log(account.getAccountId(), "DELETE_REQUEST",
            account.getProfile() != null ? account.getProfile().getFullName() : account.getEmail()
            + " requested account deletion", ip);

        notificationDAO.createNotification("DELETE_REQUEST",
            (account.getProfile() != null ? account.getProfile().getFullName() : account.getEmail())
            + " requested account deletion",
            request.getContextPath() + "/manager/deletion-requests");

        request.setAttribute("success", "Your deletion request has been submitted. An admin will review it.");
        request.setAttribute("hasPendingRequest", true);
        request.getRequestDispatcher("/WEB-INF/manager/users/delete-request.jsp").forward(request, response);
    }
}
