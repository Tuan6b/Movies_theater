/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ResetPasswordController handles resetting a password when a valid token is provided.
 *
 * @author tuan6b
 */
public class ResetPasswordController extends HttpServlet {

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
            // GET: Validate reset token and display password form
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

        } else if ("POST".equalsIgnoreCase(method)) {
            request.setCharacterEncoding("UTF-8");
            // POST: Process the new password submission
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
        return "Reset Password Controller - Handles resetting password via generated token";
    }
    // </editor-fold>
}
