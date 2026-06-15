package com.cinema.controller;

import com.cinema.dao.AuditLogDAO;
import com.cinema.model.AuditLog;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class AuditLogServlet extends HttpServlet {

    private static final int PAGE_SIZE = 20;
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null) {
            try { page = Integer.parseInt(pageStr); if (page < 1) page = 1; }
            catch (NumberFormatException ignored) {}
        }

        String search = request.getParameter("q");
        List<AuditLog> logs = auditLogDAO.getLogs(page, PAGE_SIZE, search);
        int totalLogs = auditLogDAO.countLogs(search);
        int totalPages = (int) Math.ceil((double) totalLogs / PAGE_SIZE);
        if (totalPages < 1) totalPages = 1;

        request.setAttribute("logs", logs);
        request.setAttribute("totalLogs", totalLogs);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        request.getRequestDispatcher("/WEB-INF/manager/users/audit.jsp").forward(request, response);
    }
}
