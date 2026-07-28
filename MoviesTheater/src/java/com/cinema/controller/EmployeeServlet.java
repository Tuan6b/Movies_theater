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

    /**
     * Renders the employee form. maxDateOfBirth feeds the date input's max
     * attribute so the picker cannot even offer an under-age date; the server
     * still re-checks, since that attribute is trivial to edit away.
     */
    private void forwardToForm(HttpServletRequest request, HttpServletResponse response,
            String formAction, String pageTitle) throws ServletException, IOException {
        request.setAttribute("formAction", formAction);
        request.setAttribute("pageTitle", pageTitle);
        request.setAttribute("maxDateOfBirth",
                java.time.LocalDate.now().minusYears(MIN_AGE).toString());
        request.getRequestDispatcher(FORM_JSP).forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forwardToForm(request, response, "create", "Add New Employee");
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
        forwardToForm(request, response, "update", "Edit Employee");
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Account account = buildAccountFromRequest(request);
        Map<String, String> errors = validateForCreate(account);

        if (!errors.isEmpty()) {
            request.setAttribute("employee", account);
            request.setAttribute("errors", errors);
            forwardToForm(request, response, "create", "Add New Employee");
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
            forwardToForm(request, response, "update", "Edit Employee");
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

    /** UserProfile.FullName is NVARCHAR(100); Address is NVARCHAR(255). */
    private static final int MAX_FULL_NAME = 100;
    private static final int MAX_ADDRESS   = 255;

    /** Bộ luật Lao động 2019, Điều 3: a worker must be at least 15, and the roles
     *  this cinema hires for are restricted to adults, so 18 is the floor here. */
    static final int MIN_AGE = 18;

    /** Nobody plausibly starts a counter job at this age — catches typo'd years. */
    private static final int MAX_AGE = 70;

    private Map<String, String> validateForCreate(Account account) {
        // Same rules as update: this form is the only place an employee profile is
        // entered now that first-login setup does nothing but replace the password,
        // so a field left loose on create can never be corrected by the employee.
        // The password stays out of it — EmployeeDAO.add() generates a temporary one.
        return validateProfile(account, 0);
    }

    private Map<String, String> validateForUpdate(Account account, int id) {
        return validateProfile(account, id);
    }

    /**
     * @param excludeId the account being edited, so its own email and phone do not
     *                  count as duplicates; 0 when creating.
     */
    private Map<String, String> validateProfile(Account account, int excludeId) {
        Map<String, String> errors = new LinkedHashMap<>();
        validateFullName(account.getFullName(), errors);
        validateEmail(account.getEmail(), excludeId, errors);
        validatePhone(account.getPhoneNumber(), excludeId, errors);
        validateDateOfBirth(account.getDateOfBirth(), errors);
        validateAddress(account.getAddress(), errors);
        return errors;
    }

    private void validateEmail(String email, int excludeId, Map<String, String> errors) {
        if (email == null || email.trim().isEmpty()) {
            errors.put("email", "Vui lòng nhập email");
            return;
        }
        if (!isValidEmailFormat(email)) {
            errors.put("email", "Email không hợp lệ (ví dụ: nhanvien@cinema.vn)");
            return;
        }
        // Account.Email also carries a UNIQUE constraint, so this check is about
        // showing a field error instead of letting the insert blow up on it.
        if (employeeDAO.isEmailExist(email.trim(), excludeId)) {
            errors.put("email", "Email này đã được dùng cho tài khoản khác");
        }
    }

    // Package-private (not private) so EmployeeServletValidateEmailTest, in the
    // same package under test/, can call it directly without reflection.
    static boolean isValidEmailFormat(String email) {
        return email != null && email.trim().matches("^[^@]+@[^@]+\\.[^@]+$");
    }

    private void validateFullName(String fullName, Map<String, String> errors) {
        if (fullName == null || fullName.trim().isEmpty()) {
            errors.put("fullName", "Vui lòng nhập họ và tên");
        } else if (fullName.trim().length() > MAX_FULL_NAME) {
            errors.put("fullName", "Họ và tên tối đa " + MAX_FULL_NAME + " ký tự");
        }
    }

    private void validatePhone(String phone, int excludeId, Map<String, String> errors) {
        String normalized = normalizePhone(phone);
        if (normalized.isEmpty()) {
            errors.put("phoneNumber", "Vui lòng nhập số điện thoại");
            return;
        }
        if (!isValidPhoneFormat(normalized)) {
            errors.put("phoneNumber", "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0");
            return;
        }
        if (employeeDAO.isPhoneExist(normalized, excludeId)) {
            errors.put("phoneNumber", "Số điện thoại này đã thuộc về một nhân viên khác");
        }
    }

    private void validateDateOfBirth(String dateOfBirth, Map<String, String> errors) {
        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
            // Required, otherwise the age rule below could be skipped just by
            // leaving the field empty.
            errors.put("dateOfBirth", "Vui lòng nhập ngày sinh");
            return;
        }
        java.time.LocalDate dob;
        try {
            dob = java.time.LocalDate.parse(dateOfBirth.trim());
        } catch (java.time.format.DateTimeParseException e) {
            errors.put("dateOfBirth", "Ngày sinh không hợp lệ");
            return;
        }
        java.time.LocalDate today = java.time.LocalDate.now();
        if (!dob.isBefore(today)) {
            errors.put("dateOfBirth", "Ngày sinh phải là ngày trong quá khứ");
            return;
        }
        if (!isOldEnough(dob, today)) {
            errors.put("dateOfBirth",
                    "Nhân viên phải đủ " + MIN_AGE + " tuổi theo Bộ luật Lao động");
            return;
        }
        if (java.time.Period.between(dob, today).getYears() > MAX_AGE) {
            errors.put("dateOfBirth", "Ngày sinh không hợp lý (trên " + MAX_AGE + " tuổi)");
        }
    }

    private void validateAddress(String address, Map<String, String> errors) {
        if (address != null && address.trim().length() > MAX_ADDRESS) {
            errors.put("address", "Địa chỉ tối đa " + MAX_ADDRESS + " ký tự");
        }
    }

    /**
     * Strips the spaces, dots, hyphens and brackets people type into a phone field
     * and rewrites a +84/84 prefix as a leading 0. Without this, "090 000 0003",
     * "+84900000003" and "0900000003" are three different strings in the database
     * and the duplicate check never fires.
     *
     * Package-private so EmployeeServletValidateProfileTest can call it directly.
     *
     * @return the digits-only form, or "" when there is nothing to normalise
     */
    static String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        String digits = phone.replaceAll("[\\s.\\-()]", "");
        if (digits.startsWith("+84")) {
            digits = "0" + digits.substring(3);
        } else if (digits.startsWith("84") && digits.length() > 10) {
            digits = "0" + digits.substring(2);
        }
        return digits;
    }

    /** Vietnamese subscriber numbers have been a flat 10 digits starting 0 since 2018. */
    static boolean isValidPhoneFormat(String normalizedPhone) {
        return normalizedPhone != null && normalizedPhone.matches("0\\d{9}");
    }

    /**
     * Whether someone born on {@code dob} has reached MIN_AGE by {@code today}.
     * Takes the reference date rather than reading the clock so the test does not
     * depend on when it runs, and so the birthday itself counts as old enough.
     */
    static boolean isOldEnough(java.time.LocalDate dob, java.time.LocalDate today) {
        return dob != null && !dob.plusYears(MIN_AGE).isAfter(today);
    }

    private Account buildAccountFromRequest(HttpServletRequest request) {
        Account account = new Account();
        // Trimmed here rather than only inside the validators: EmployeeDAO writes
        // whatever this object holds, so an untrimmed value would be validated in
        // one form and stored in another — enough for " a@b.vn" to sit alongside
        // "a@b.vn" and defeat both duplicate checks.
        account.setEmail(trimToNull(request.getParameter("email")));
        account.setFullName(trimToNull(request.getParameter("fullName")));
        account.setPhoneNumber(normalizePhone(request.getParameter("phoneNumber")));
        account.setAddress(trimToNull(request.getParameter("address")));
        account.setDateOfBirth(trimToNull(request.getParameter("dateOfBirth")));
        String pwd = request.getParameter("password");
        account.setPassword(pwd != null ? pwd.trim() : null);
        return account;
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
