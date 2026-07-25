package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.model.Account;
import com.cinema.util.SystemLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class ProfileController extends HttpServlet {

    private final AccountDAO accountDAO = new AccountDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Account account = (Account) session.getAttribute("account");
        if (account == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }

        Account fresh = accountDAO.getAccountById(account.getAccountId());
        if (fresh != null) {
            session.setAttribute("account", fresh);
        }

        String roleView = getRoleView(account.getRoleId());
        request.getRequestDispatcher(roleView).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Account account = (Account) session.getAttribute("account");
        if (account == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String dob = request.getParameter("dob");

        String sql = "UPDATE UserProfile SET FullName = ?, PhoneNumber = ?, Address = ?, DoB = ? WHERE AccountID = ?";
        try (java.sql.Connection conn = com.cinema.util.DBUtils.getConnection();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, fullName != null ? fullName.trim() : account.getFullName());
            ps.setString(2, phone != null ? phone.trim() : account.getPhoneNumber());
            ps.setNString(3, address != null ? address.trim() : null);
            if (dob != null && !dob.trim().isEmpty()) {
                ps.setDate(4, java.sql.Date.valueOf(dob));
            } else {
                ps.setNull(4, java.sql.Types.DATE);
            }
            ps.setInt(5, account.getAccountId());
            ps.executeUpdate();

            SystemLogService.log(account.getAccountId(), "PROFILE_UPDATE",
                    "User updated profile", request.getRemoteAddr());

            Account fresh = accountDAO.getAccountById(account.getAccountId());
            if (fresh != null) session.setAttribute("account", fresh);

            session.setAttribute("flashSuccess", "Cập nhật hồ sơ thành công.");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("flashError", "Lỗi khi cập nhật hồ sơ: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private String getRoleView(int roleId) {
        switch (roleId) {
            case 3: return "/view/employee/profile.jsp";
            case 4: return "/view/manager/profile.jsp";
            case 5: return "/view/admin/profile.jsp";
            default: return "/view/customer/profile.jsp";
        }
    }
}
