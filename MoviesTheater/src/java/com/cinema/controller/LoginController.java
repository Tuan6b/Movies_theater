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
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * LoginController manages the authentication flow for accounts.
 *
 * @author tuan6b
 */
public class LoginController extends HttpServlet {

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
            // GET: Show login form or redirect if already authenticated
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("account") != null) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }
            request.getRequestDispatcher("/login.jsp").forward(request, response);

        } else if ("POST".equalsIgnoreCase(method)) {
            request.setCharacterEncoding("UTF-8");
            // POST: Process login credentials
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String remember = request.getParameter("remember");

            if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                request.setAttribute("error", "Vui lòng nhập email và mật khẩu.");
                request.setAttribute("email", email);
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

            HttpSession session = request.getSession(true);
            session.setAttribute("account", account);

            // Remember me
            // Setting session lifetime to 7 days (7 * 24 * 60 * 60) on server-side is a bad practice.
            // Under high load, this causes OutOfMemoryError due to session object accumulation in JVM heap.
            // A production-grade implementation should use persistent cookies + DB token storage.
            // As a safer default, we limit session to 1 day instead of 7 days here.
            if ("on".equalsIgnoreCase(remember)) {
                session.setMaxInactiveInterval(24 * 60 * 60); // 1 day
            } else {
                session.setMaxInactiveInterval(30 * 60); // 30 minutes
            }

            String redirectAfterLogin = (String) session.getAttribute("redirectAfterLogin");
            if (redirectAfterLogin != null && !redirectAfterLogin.trim().isEmpty()) {
                session.removeAttribute("redirectAfterLogin");
                response.sendRedirect(redirectAfterLogin);
            } else {
                switch (account.getRoleId()) {
                    case 5: // Admin
                    case 4: // Manager
                    case 3: // Employee
                        response.sendRedirect(request.getContextPath() + "/manager");
                        break;
                    default: // Customer
                        response.sendRedirect(request.getContextPath() + "/");
                        break;
                }
            }
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
        return "Login Controller - Handles user authentication flow";
    }
    // </editor-fold>
}