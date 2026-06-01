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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("account") != null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String remember = request.getParameter("remember");

        if (email == null || password == null || email.trim().isEmpty() || password.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập email và mật khẩu.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        Account account = accountDAO.login(email.trim(), password);

        if (account == null) {
            request.setAttribute("error", "Email hoặc mật khẩu không đúng.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        if (account.isIsBlocked()) {
            request.setAttribute("error", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("account", account);
        session.setMaxInactiveInterval(30 * 60);

        if ("on".equals(remember)) {
            session.setMaxInactiveInterval(7 * 24 * 60 * 60);
        }

        String redirect = (String) session.getAttribute("redirectAfterLogin");
        if (redirect != null) {
            session.removeAttribute("redirectAfterLogin");
            response.sendRedirect(redirect);
        } else {
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}
