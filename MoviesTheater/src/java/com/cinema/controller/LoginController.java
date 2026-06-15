package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class LoginController extends HttpServlet {

    private final AccountDAO accountDAO = new AccountDAO();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null
                && session.getAttribute("account") != null) {

            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        request.getRequestDispatcher("/login.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String remember = request.getParameter("remember");

        // Validate input
        if (email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {

            request.setAttribute("error",
                    "Vui lòng nhập email và mật khẩu.");

            request.setAttribute("email", email);

            request.getRequestDispatcher("/login.jsp")
                    .forward(request, response);

            return;
        }

        // Check account
        Account account = accountDAO.login(
                email.trim(),
                password
        );

        if (account == null) {

            request.setAttribute("error",
                    "Email hoặc mật khẩu không đúng.");

            request.setAttribute("email", email);

            request.getRequestDispatcher("/login.jsp")
                    .forward(request, response);

            return;
        }

        // Check blocked account
        if (account.isIsBlocked()) {

            request.setAttribute("error",
                    "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");

            request.setAttribute("email", email);

            request.getRequestDispatcher("/login.jsp")
                    .forward(request, response);

            return;
        }

        // Create session
        HttpSession session = request.getSession(true);

        session.setAttribute("account", account);

        // 30 phút
        session.setMaxInactiveInterval(30 * 60);

        // Remember me
        if ("on".equalsIgnoreCase(remember)) {
            session.setMaxInactiveInterval(7 * 24 * 60 * 60);
        }

        // Redirect về URL trước khi login
        String redirectAfterLogin =
                (String) session.getAttribute("redirectAfterLogin");

        if (redirectAfterLogin != null
                && !redirectAfterLogin.trim().isEmpty()) {

            session.removeAttribute("redirectAfterLogin");

            response.sendRedirect(redirectAfterLogin);

        } else {

            // Redirect theo role
            switch (account.getRoleId()) {

                case 5: // Admin
                    response.sendRedirect(
                            request.getContextPath() + "/");
                    break;

                case 4: // Manager
                case 3: // Employee
                    response.sendRedirect(
                            request.getContextPath() + "/employee");
                    break;

                default: // Customer
                    response.sendRedirect(
                            request.getContextPath() + "/");
                    break;
            }
        }
    }
}