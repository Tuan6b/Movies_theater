/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.cinema.controller;

import com.cinema.dao.EmployeeDAO;
import com.cinema.model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tuan6b
 */
public class EmployeeServlet extends HttpServlet {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    private static final int PAGE_SIZE = 10;
    private static final String LIST_JSP = "/view/manager/employees/list.jsp";
    private static final String FORM_JSP = "/view/manager/employees/form.jsp";
    private static final String LIST_URL = "/manager/employees";

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
        String action = request.getParameter("action");
        if (action == null) action = "";

        if ("GET".equalsIgnoreCase(method)) {
            switch (action) {
                case "add":
                    showAddForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                default:
                    showList(request, response);
                    break;
            }
        } else if ("POST".equalsIgnoreCase(method)) {
            request.setCharacterEncoding("UTF-8");
            switch (action) {
                case "create":
                    handleCreate(request, response);
                    break;
                case "update":
                    handleUpdate(request, response);
                    break;
                case "toggle":
                    handleToggle(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + LIST_URL);
                    break;
            }
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword  = request.getParameter("keyword");
        String sortField = request.getParameter("sort");
        String sortDir   = request.getParameter("dir");
        int page = parseIntParam(request.getParameter("page"), 1);
        if (page < 1) page = 1;

        HttpSession session = request.getSession(false);
        if (session != null) {
            transferFlash(session, request, "flashSuccess");
            transferFlash(session, request, "flashError");
        }

        List<Account> employees = employeeDAO.getAll(keyword, page, PAGE_SIZE, sortField, sortDir);
        int totalItems  = employeeDAO.countAll(keyword);
        int totalPages  = totalItems == 0 ? 1 : (int) Math.ceil((double) totalItems / PAGE_SIZE);

        request.setAttribute("employees", employees);
        request.setAttribute("totalItems", totalItems);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("currentPage", page);
        request.setAttribute("keyword",   keyword   != null ? keyword   : "");
        request.setAttribute("sortField", sortField != null ? sortField : "");
        request.setAttribute("sortDir",   sortDir   != null ? sortDir   : "");
        request.getRequestDispatcher(LIST_JSP).forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("formAction", "create");
        request.setAttribute("pageTitle", "Add New Employee");
        request.getRequestDispatcher(FORM_JSP).forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request.getParameter("id"), 0);
        if (id <= 0) {
            response.sendRedirect(request.getContextPath() + LIST_URL);
            return;
        }
        Account employee = employeeDAO.getById(id);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + LIST_URL);
            return;
        }
        request.setAttribute("employee", employee);
        request.setAttribute("formAction", "update");
        request.setAttribute("pageTitle", "Edit Employee");
        request.getRequestDispatcher(FORM_JSP).forward(request, response);
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Account account = buildAccountFromRequest(request);
        Map<String, String> errors = validateForCreate(account);

        if (!errors.isEmpty()) {
            request.setAttribute("employee", account);
            request.setAttribute("errors", errors);
            request.setAttribute("formAction", "create");
            request.setAttribute("pageTitle", "Add New Employee");
            request.getRequestDispatcher(FORM_JSP).forward(request, response);
            return;
        }

        int newId = employeeDAO.add(account);
        if (newId > 0) {
            // account.getPassword() now holds the auto-generated plaintext password
            request.getSession().setAttribute("flashSuccess",
                    "Tài khoản nhân viên đã được tạo. Mật khẩu tạm thời: " + account.getPassword()
                    + " (Hãy thông báo cho nhân viên)");
        } else {
            request.getSession().setAttribute("flashError", "System error. Please try again.");
        }
        response.sendRedirect(request.getContextPath() + LIST_URL);
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request.getParameter("accountId"), 0);
        if (id <= 0) {
            response.sendRedirect(request.getContextPath() + LIST_URL);
            return;
        }

        // BR-14: accountId arrives from a hidden form field, so confirm it still
        // resolves to an employee before writing. getById filters on RoleID, which
        // makes a retargeted id (a Manager or Admin account) fail here rather than
        // relying only on the same guard inside the DAO.
        if (employeeDAO.getById(id) == null) {
            request.getSession().setAttribute("flashError", "Employee not found.");
            response.sendRedirect(request.getContextPath() + LIST_URL);
            return;
        }

        Account account = buildAccountFromRequest(request);
        account.setAccountId(id);
        Map<String, String> errors = validateForUpdate(account, id);

        if (!errors.isEmpty()) {
            request.setAttribute("employee", account);
            request.setAttribute("errors", errors);
            request.setAttribute("formAction", "update");
            request.setAttribute("pageTitle", "Edit Employee");
            request.getRequestDispatcher(FORM_JSP).forward(request, response);
            return;
        }

        boolean ok = employeeDAO.update(account);
        if (ok) {
            request.getSession().setAttribute("flashSuccess", "Employee updated successfully.");
        } else {
            request.getSession().setAttribute("flashError", "System error. Please try again.");
        }
        response.sendRedirect(request.getContextPath() + LIST_URL);
    }

    private void handleToggle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request.getParameter("accountId"), 0);
        String blockedParam = request.getParameter("blocked");
        if (id > 0) {
            boolean block = "true".equals(blockedParam);
            boolean ok = employeeDAO.setBlocked(id, block);
            if (ok) {
                String msg = block ? "Employee deactivated." : "Employee activated.";
                request.getSession().setAttribute("flashSuccess", msg);
            } else {
                request.getSession().setAttribute("flashError", "System error. Please try again.");
            }
        }
        response.sendRedirect(request.getContextPath() + LIST_URL);
    }

    private Map<String, String> validateForCreate(Account account) {
        Map<String, String> errors = new LinkedHashMap<>();
        validateEmail(account.getEmail(), 0, errors);
        // fullName and password are optional on create; employee fills them on first login
        return errors;
    }

    private Map<String, String> validateForUpdate(Account account, int id) {
        Map<String, String> errors = new LinkedHashMap<>();
        validateEmail(account.getEmail(), id, errors);
        validateFullName(account.getFullName(), errors);
        return errors;
    }

    private void validateEmail(String email, int excludeId, Map<String, String> errors) {
        if (email == null || email.trim().isEmpty()) {
            errors.put("email", "Email is required");
            return;
        }
        if (!isValidEmailFormat(email)) {
            errors.put("email", "Invalid email format");
            return;
        }
        if (employeeDAO.isEmailExist(email.trim(), excludeId)) {
            errors.put("email", "Email already in use");
        }
    }

    // Package-private (not private) so EmployeeServletValidateEmailTest, in the
    // same package under test/, can call it directly without reflection.
    static boolean isValidEmailFormat(String email) {
        return email != null && email.trim().matches("^[^@]+@[^@]+\\.[^@]+$");
    }

    private void validateFullName(String fullName, Map<String, String> errors) {
        if (fullName == null || fullName.trim().isEmpty()) {
            errors.put("fullName", "Full name is required");
        }
    }

    private Account buildAccountFromRequest(HttpServletRequest request) {
        Account account = new Account();
        account.setEmail(request.getParameter("email"));
        account.setFullName(request.getParameter("fullName"));
        account.setPhoneNumber(request.getParameter("phoneNumber"));
        account.setAddress(request.getParameter("address"));
        account.setDateOfBirth(request.getParameter("dateOfBirth"));
        String pwd = request.getParameter("password");
        account.setPassword(pwd != null ? pwd.trim() : null);
        return account;
    }

    private void transferFlash(HttpSession session, HttpServletRequest request, String key) {
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(key, value);
            session.removeAttribute(key);
        }
    }

    private int parseIntParam(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
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
        return "Employee Manager Servlet - UC44 Manage Employee + UC45 View Employee List";
    }// </editor-fold>

}
