package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

public class ForgotPasswordController extends HttpServlet {

    private final AccountDAO accountDAO = new AccountDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            request.setAttribute("error", "Email không hợp lệ.");
            request.getRequestDispatcher("/forgot-password.jsp").forward(request, response);
            return;
        }

        if (!accountDAO.isEmailExist(email)) {
            request.setAttribute("error", "Email không tồn tại trong hệ thống.");
            request.getRequestDispatcher("/forgot-password.jsp").forward(request, response);
            return;
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);
        boolean updated = accountDAO.updateResetToken(email, token, expiry);

        if (!updated) {
            request.setAttribute("error", "Có lỗi xảy ra khi tạo yêu cầu. Vui lòng thử lại sau.");
            request.getRequestDispatcher("/forgot-password.jsp").forward(request, response);
            return;
        }

        String resetLink = request.getRequestURL().toString().replace("/forgot-password", "/new-password")
                + "?token=" + token;
        long expirySeconds = java.time.Duration.between(LocalDateTime.now(), expiry).getSeconds();
        request.setAttribute("expirySeconds", expirySeconds);
        request.setAttribute("message", "Link đặt lại mật khẩu đã được tạo.<br>"
                + "<a href=\"" + resetLink + "\" style=\"color:#bd0000;\">Nhấp vào đây để đặt lại mật khẩu</a>");
        request.getRequestDispatcher("/forgot-password.jsp").forward(request, response);
    }
}
