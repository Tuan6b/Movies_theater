package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResetPasswordController extends HttpServlet {

    private final AccountDAO accountDAO = new AccountDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = request.getParameter("token");
        if (token == null || token.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }

        Account account = accountDAO.getAccountByResetToken(token);
        if (account == null) {
            request.setAttribute("error", "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
        }
        request.setAttribute("token", token);
        request.getRequestDispatcher("/new-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = request.getParameter("token");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (token == null || token.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }

        if (password == null || password.length() < 6) {
            request.setAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
            request.setAttribute("token", token);
            request.getRequestDispatcher("/new-password.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Mật khẩu xác nhận không khớp.");
            request.setAttribute("token", token);
            request.getRequestDispatcher("/new-password.jsp").forward(request, response);
            return;
        }

        Account account = accountDAO.getAccountByResetToken(token);
        if (account == null) {
            request.setAttribute("error", "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            request.getRequestDispatcher("/new-password.jsp").forward(request, response);
            return;
        }

        if (accountDAO.updatePassword(account.getAccountId(), password)) {
            accountDAO.clearResetToken(account.getAccountId());
            request.setAttribute("message", "Mật khẩu đã được đặt lại thành công. <a href=\""
                    + request.getContextPath() + "/Login\">Đăng nhập ngay</a>");
        } else {
            request.setAttribute("error", "Có lỗi xảy ra, vui lòng thử lại.");
        }
        request.getRequestDispatcher("/new-password.jsp").forward(request, response);
    }
}
