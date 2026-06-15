package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ExportUsersServlet extends HttpServlet {

    private final AccountDAO accountDAO = new AccountDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"users_export.csv\"");

        String search = request.getParameter("q");
        String roleFilter = request.getParameter("role");

        List<Account> users = accountDAO.getAllAccounts(1, 99999, search, roleFilter);

        PrintWriter out = response.getWriter();
        out.println("ID,Email,FullName,Role,Status,Phone,DoB,Address,CreatedAt");
        for (Account u : users) {
            out.print(u.getAccountId() + ",");
            out.print(csvEscape(u.getEmail()) + ",");
            out.print(csvEscape(u.getProfile() != null ? u.getProfile().getFullName() : "") + ",");
            out.print(csvEscape(u.getRoleName()) + ",");
            out.print((u.isIsBlocked() ? "Blocked" : "Active") + ",");
            out.print(csvEscape(u.getProfile() != null ? u.getProfile().getPhoneNumber() : "") + ",");
            out.print(csvEscape(u.getProfile() != null ? u.getProfile().getDob() : "") + ",");
            out.print(csvEscape(u.getProfile() != null ? u.getProfile().getAddress() : "") + ",");
            out.println(u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
        }
        out.flush();
    }

    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
