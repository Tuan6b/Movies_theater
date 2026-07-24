/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.model.Account;
import com.cinema.util.MailUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * RegisterController handles the registration flow for new customers.
 *
 * @author tuan6b
 */
public class RegisterController extends HttpServlet {

    private final AccountDAO accountDAO = new AccountDAO();
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^(0[35789])([0-9]{8})$"); // Matches Vietnamese mobile phone numbers (10 digits)
    private static final char[] CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final Random RANDOM = new Random();

    private String generateCaptchaText() {
        StringBuilder sb = new StringBuilder(5);
        for (int i = 0; i < 5; i++) {
            sb.append(CAPTCHA_CHARS[RANDOM.nextInt(CAPTCHA_CHARS.length)]);
        }
        return sb.toString();
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String method = request.getMethod();

        if ("GET".equalsIgnoreCase(method)) {
            // GET: Show registration page or redirect if already authenticated
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("account") != null) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }
            String captchaText = generateCaptchaText();
            if (session == null) session = request.getSession(true);
            session.setAttribute("captcha", captchaText);
            request.setAttribute("captchaText", captchaText);
            // AJAX refresh captcha
            if ("1".equals(request.getParameter("refreshCaptcha"))) {
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write(captchaText);
                return;
            }
            request.getRequestDispatcher("/view/auth/register.jsp").forward(request, response);

        } else if ("POST".equalsIgnoreCase(method)) {
            request.setCharacterEncoding("UTF-8");
            // POST: Process registration form submission
            String fullName = request.getParameter("fullName");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String confirmPassword = request.getParameter("confirmPassword");
            String phoneNumber = request.getParameter("phoneNumber");
            String captcha = request.getParameter("captcha");

            HttpSession session = request.getSession();
            String captchaExpected = (String) session.getAttribute("captcha");
            if (captchaExpected == null || captcha == null || !captchaExpected.equalsIgnoreCase(captcha.trim())) {
                Map<String, String> captchaErr = new HashMap<>();
                captchaErr.put("captcha", "Mã xác nhận không đúng.");
                setFormAttributes(request, fullName, email, phoneNumber, captchaErr, "Mã xác nhận không đúng.");
                String newCaptcha = generateCaptchaText();
                session.setAttribute("captcha", newCaptcha);
                request.setAttribute("captchaText", newCaptcha);
                request.getRequestDispatcher("/view/auth/register.jsp").forward(request, response);
                return;
            }
            session.removeAttribute("captcha");

            Map<String, String> fieldErrors = validateInput(fullName, email, password, confirmPassword, phoneNumber);
            if (!fieldErrors.isEmpty()) {
                setFormAttributes(request, fullName, email, phoneNumber, fieldErrors, null);
                String newCaptcha = generateCaptchaText();
                if (session == null) session = request.getSession(true);
                session.setAttribute("captcha", newCaptcha);
                request.setAttribute("captchaText", newCaptcha);
                request.getRequestDispatcher("/view/auth/register.jsp").forward(request, response);
                return;
            }

            if (accountDAO.isEmailExist(email.trim())) {
                Map<String, String> emailErr = new HashMap<>();
                emailErr.put("email", "Email này đã được đăng ký.");
                setFormAttributes(request, fullName, email, phoneNumber, emailErr, "Email này đã được đăng ký.");
                request.getRequestDispatcher("/view/auth/register.jsp").forward(request, response);
                return;
            }

            Account account = new Account();
            account.setFullName(fullName.trim());
            account.setEmail(email.trim());
            account.setPassword(password);
            account.setPhoneNumber(phoneNumber != null ? phoneNumber.trim() : null);
            account.setRoleId(2); // Customer Role

            int accountId;
            try {
                accountId = accountDAO.register(account);
            } catch (Exception e) {
                System.err.println("[REGISTER_ERROR] Exception during register: " + e.getMessage());
                e.printStackTrace();
                Map<String, String> sysErr = new HashMap<>();
                sysErr.put("system", "Lỗi kết nối cơ sở dữ liệu. Vui lòng thử lại sau.");
                setFormAttributes(request, fullName, email, phoneNumber,
                        sysErr, "Lỗi kết nối cơ sở dữ liệu. Vui lòng thử lại sau.");
                request.getRequestDispatcher("/view/auth/register.jsp").forward(request, response);
                return;
            }
            if (accountId > 0) {
                account.setAccountId(accountId);
                account.setPassword(null);

                session = request.getSession();
                session.setAttribute("account", account);
                session.setMaxInactiveInterval(30 * 60);

                try {
                    MailUtil.sendWelcomeEmail(account.getEmail(), account.getFullName());
                } catch (Exception ignored) {}

                response.sendRedirect(request.getContextPath() + "/");
            } else {
                System.err.println("[REGISTER_ERROR] register() returned -1 for email: " + email);
                Map<String, String> sysErr = new HashMap<>();
                sysErr.put("system", "Đăng ký thất bại. Vui lòng thử lại sau.");
                setFormAttributes(request, fullName, email, phoneNumber,
                        sysErr, "Đăng ký thất bại. Vui lòng kiểm tra kết nối database và thử lại.");
                request.getRequestDispatcher("/view/auth/register.jsp").forward(request, response);
            }
        }
    }

    // Package-private (not private) so RegisterControllerValidateInputTest,
    // in the same package under test/, can call it directly without reflection.
    Map<String, String> validateInput(String fullName, String email, String password, String confirmPassword, String phoneNumber) {
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
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(phoneNumber.trim()).matches()) {
                errors.put("phoneNumber", "Số điện thoại không hợp lệ (phải gồm 10 chữ số).");
            }
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

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Register Controller - Handles customer registration flow";
    }
    // </editor-fold>
}
