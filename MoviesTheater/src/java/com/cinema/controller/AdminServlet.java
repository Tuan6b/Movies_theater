package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AdminServlet extends HttpServlet {

    private static final String ADMIN_DASHBOARD = "/WEB-INF/admin/dashboard.jsp";
    private final AccountDAO accountDAO = new AccountDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // gather stats for dashboard
        int totalAccounts = accountDAO.countAccounts(null, null);
        int staffCount = accountDAO.countStaff();
        int lockedCount = accountDAO.countLocked();
        int activeCount = totalAccounts - lockedCount;

        request.setAttribute("totalAccounts", totalAccounts);
        request.setAttribute("staffCount", staffCount);
        request.setAttribute("lockedCount", lockedCount);
        request.setAttribute("activeCount", activeCount);

        request.getRequestDispatcher(ADMIN_DASHBOARD).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
