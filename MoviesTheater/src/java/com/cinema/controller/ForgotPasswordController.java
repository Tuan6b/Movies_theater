/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ForgotPasswordController handles requests for generating password reset tokens.
 *
 * @author tuan6b
 */
public class ForgotPasswordController extends HttpServlet {

    private final AccountDAO accountDAO = new AccountDAO();

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
            // GET: Show forgot password form
            request.getRequestDispatcher("/view/auth/forgot-password.jsp").forward(request, response);
        } else if ("POST".equalsIgnoreCase(method)) {
            request.setCharacterEncoding("UTF-8");
            // POST: Process form submission
            String email = request.getParameter("email");
            if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
                request.setAttribute("error", "Email không hợp lệ.");
                request.getRequestDispatcher("/view/auth/forgot-password.jsp").forward(request, response);
                return;
            }

            // Always display success message even if email doesn't exist to prevent enumeration vulnerability
            if (!accountDAO.isEmailExist(email.trim())) {
                request.setAttribute("message", "Nếu email tồn tại trong hệ thống, link đặt lại mật khẩu đã được gửi.");
                request.getRequestDispatcher("/view/auth/forgot-password.jsp").forward(request, response);
                return;
            }

            String token = UUID.randomUUID().toString();
            LocalDateTime expiry = LocalDateTime.now().plusHours(1);
            boolean updated = accountDAO.updateResetToken(email.trim(), token, expiry);

            if (!updated) {
                request.setAttribute("error", "Có lỗi xảy ra khi tạo yêu cầu. Vui lòng thử lại sau.");
                request.getRequestDispatcher("/view/auth/forgot-password.jsp").forward(request, response);
                return;
            }

            // Robust reset link construction using request details
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();
            String resetLink = scheme + "://" + serverName + ":" + serverPort + contextPath + "/new-password?token=" + token;

            long expirySeconds = java.time.Duration.between(LocalDateTime.now(), expiry).getSeconds();
            request.setAttribute("expirySeconds", expirySeconds);
            request.setAttribute("message", "Link đặt lại mật khẩu đã được tạo.<br>"
                    + "<a href=\"" + resetLink + "\" style=\"color:#bd0000;\">Nhấp vào đây để đặt lại mật khẩu</a>");
            request.getRequestDispatcher("/view/auth/forgot-password.jsp").forward(request, response);
        }
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
        return "Forgot Password Controller - Handles reset password token generation";
    }
    // </editor-fold>
}
