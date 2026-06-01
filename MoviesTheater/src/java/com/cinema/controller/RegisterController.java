package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.regex.Pattern;

public class RegisterController extends HttpServlet {

    private final AccountDAO accountDAO = new AccountDAO();
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("account") != null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String phoneNumber = request.getParameter("phoneNumber");

        String error = validateInput(fullName, email, password, confirmPassword);
        if (error != null) {
            setFormAttributes(request, fullName, email, phoneNumber, error);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (accountDAO.isEmailExist(email.trim())) {
            setFormAttributes(request, fullName, email, phoneNumber, "Email này đã được đăng ký.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        Account account = new Account();
        account.setFullName(fullName.trim());
        account.setEmail(email.trim());
        account.setPassword(password);
        account.setPhoneNumber(phoneNumber != null ? phoneNumber.trim() : null);
        account.setRoleId(2);

        int accountId = accountDAO.register(account);
        if (accountId > 0) {
            account.setAccountId(accountId);
            account.setPassword(null);

            HttpSession session = request.getSession();
            session.setAttribute("account", account);
            session.setMaxInactiveInterval(30 * 60);

            response.sendRedirect(request.getContextPath() + "/");
        } else {
            setFormAttributes(request, fullName, email, phoneNumber,
                    "Đăng ký thất bại. Vui lòng thử lại sau.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }

    private String validateInput(String fullName, String email, String password, String confirmPassword) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Vui lòng nhập họ tên.";
        }
        if (email == null || email.trim().isEmpty()) {
            return "Vui lòng nhập email.";
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "Email không hợp lệ.";
        }
        if (password == null || password.isEmpty()) {
            return "Vui lòng nhập mật khẩu.";
        }
        if (password.length() < 6) {
            return "Mật khẩu phải có ít nhất 6 ký tự.";
        }
        if (confirmPassword == null || !confirmPassword.equals(password)) {
            return "Xác nhận mật khẩu không khớp.";
        }
        return null;
    }

    private void setFormAttributes(HttpServletRequest request, String fullName,
            String email, String phoneNumber, String error) {
        request.setAttribute("error", error);
        request.setAttribute("fullName", fullName);
        request.setAttribute("email", email);
        request.setAttribute("phoneNumber", phoneNumber);
    }
}
