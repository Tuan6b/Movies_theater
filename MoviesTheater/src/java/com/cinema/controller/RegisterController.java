package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.dao.NotificationDAO;
import com.cinema.model.Account;
import com.cinema.model.UserProfile;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterController extends HttpServlet {

    private final AccountDAO accountDAO = new AccountDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
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

        Map<String, String> fieldErrors = validateInput(fullName, email, password, confirmPassword);
        if (!fieldErrors.isEmpty()) {
            setFormAttributes(request, fullName, email, phoneNumber, fieldErrors, null);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (accountDAO.isEmailExist(email.trim())) {
            Map<String, String> emailErr = new HashMap<>();
            emailErr.put("email", "Email này đã được đăng ký.");
            setFormAttributes(request, fullName, email, phoneNumber, emailErr, "Email này đã được đăng ký.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        UserProfile profile = new UserProfile();
        profile.setFullName(fullName.trim());
        profile.setPhoneNumber(phoneNumber != null ? phoneNumber.trim() : null);

        Account account = new Account();
        account.setProfile(profile);
        account.setEmail(email.trim());
        account.setPassword(password);
        account.setRoleId(2);

        int accountId = accountDAO.register(account);
        if (accountId > 0) {
            account.setAccountId(accountId);
            account.setPassword(null);

            notificationDAO.createNotification("NEW_USER",
                "New user registered: " + fullName.trim() + " (" + email.trim() + ")",
                request.getContextPath() + "/manager/users");

            HttpSession session = request.getSession();
            session.setAttribute("account", account);
            session.setMaxInactiveInterval(30 * 60);

            response.sendRedirect(request.getContextPath() + "/");
        } else {
            Map<String, String> sysErr = new HashMap<>();
            sysErr.put("system", "Đăng ký thất bại. Vui lòng thử lại sau.");
            setFormAttributes(request, fullName, email, phoneNumber,
                    sysErr, "Đăng ký thất bại. Vui lòng thử lại sau.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }

    private Map<String, String> validateInput(String fullName, String email, String password, String confirmPassword) {
        Map<String, String> errors = new HashMap<>();
        if (fullName == null || fullName.trim().isEmpty()) {
            errors.put("fullName", "Vui lòng nhập họ tên.");
        }
        if (email == null || email.trim().isEmpty()) {
            errors.put("email", "Vui lòng nhập email.");
        } else if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            errors.put("email", "Email không hợp lệ.");
        }
        if (password == null || password.isEmpty()) {
            errors.put("password", "Vui lòng nhập mật khẩu.");
        } else if (password.length() < 6) {
            errors.put("password", "Mật khẩu phải có ít nhất 6 ký tự.");
        }
        if (confirmPassword == null || !confirmPassword.equals(password)) {
            errors.put("confirmPassword", "Xác nhận mật khẩu không khớp.");
        }
        return errors;
    }

    private void setFormAttributes(HttpServletRequest request, String fullName,
            String email, String phoneNumber, Map<String, String> fieldErrors, String generalError) {
        request.setAttribute("fieldErrors", fieldErrors);
        request.setAttribute("error", generalError);
        request.setAttribute("fullName", fullName);
        request.setAttribute("email", email);
        request.setAttribute("phoneNumber", phoneNumber);
    }
}
