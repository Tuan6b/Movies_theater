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
}