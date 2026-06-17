/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.cinema.controller;

import com.cinema.dao.EmployeeDAO;
import com.cinema.dao.WorkShiftDAO;
import com.cinema.model.Account;
import com.cinema.model.WorkShift;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tuan6b
 */
public class WorkShiftServlet extends HttpServlet {

    private final WorkShiftDAO shiftDAO    = new WorkShiftDAO();
    private final EmployeeDAO  employeeDAO = new EmployeeDAO();

    private static final String LIST_JSP = "/WEB-INF/manager/shifts/list.jsp";
    private static final String FORM_JSP = "/WEB-INF/manager/shifts/form.jsp";
    private static final String LIST_URL = "/manager/shifts";

    // Fixed CGV shift types: key -> [startTime HH:MM, endTime HH:MM]
    private static final Map<String, String[]> SHIFT_TIMES = new LinkedHashMap<>();
    static {
        SHIFT_TIMES.put("6H_SANG",  new String[]{"08:00", "14:00"});
        SHIFT_TIMES.put("6H_CHIEU", new String[]{"14:00", "20:00"});
        SHIFT_TIMES.put("6H_TOI",   new String[]{"20:00", "23:59"});
        SHIFT_TIMES.put("8H_SANG",  new String[]{"08:00", "17:30"});
        SHIFT_TIMES.put("8H_CHIEU", new String[]{"13:00", "22:30"});
    }

    private static final String[] MONTH_VI = {
        "", "Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
        "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"
    };

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
                case "edit":
                    showEditForm(request, response);
                    break;
                default:
                    showCalendar(request, response);
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
                case "delete":
                    handleDelete(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + LIST_URL);
                    break;
            }
        }
    }

    private void showCalendar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int empId = parseIntParam(request.getParameter("empId"), 0);
        LocalDate today = LocalDate.now();
        int year  = parseIntParam(request.getParameter("year"),  today.getYear());
        int month = parseIntParam(request.getParameter("month"), today.getMonthValue());
        if (month < 1) month = 1;
        if (month > 12) month = 12;

        HttpSession session = request.getSession(false);
        if (session != null) {
            transferFlash(session, request, "flashSuccess");
            transferFlash(session, request, "flashError");
        }

        List<Account> employees = employeeDAO.getAll(null, 1, 200, "name", "ASC");
        List<WorkShift> shifts  = empId > 0
                ? shiftDAO.getByEmployeeAndMonth(empId, year, month)
                : java.util.Collections.emptyList();

        int prevMonth = month == 1 ? 12 : month - 1;
        int prevYear  = month == 1 ? year - 1 : year;
        int nextMonth = month == 12 ? 1 : month + 1;
        int nextYear  = month == 12 ? year + 1 : year;

        LocalTime now = LocalTime.now();
        String currentTime = String.format("%02d:%02d", now.getHour(), now.getMinute());

        request.setAttribute("employees",     employees);
        request.setAttribute("shifts",        shifts);
        request.setAttribute("selectedEmpId", empId);
        request.setAttribute("selectedYear",  year);
        request.setAttribute("selectedMonth", month);
        request.setAttribute("monthName",     MONTH_VI[month] + " " + year);
        request.setAttribute("prevMonth",     prevMonth);
        request.setAttribute("prevYear",      prevYear);
        request.setAttribute("nextMonth",     nextMonth);
        request.setAttribute("nextYear",      nextYear);
        request.setAttribute("serverToday",   today.toString());
        request.setAttribute("serverTime",    currentTime);
        request.getRequestDispatcher(LIST_JSP).forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request.getParameter("id"), 0);
        if (id <= 0) { response.sendRedirect(request.getContextPath() + LIST_URL); return; }
        WorkShift shift = shiftDAO.getById(id);
        if (shift == null) { response.sendRedirect(request.getContextPath() + LIST_URL); return; }
        request.setAttribute("shift", shift);
        request.getRequestDispatcher(FORM_JSP).forward(request, response);
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int    empId     = parseIntParam(request.getParameter("employeeId"), 0);
        String shiftType = request.getParameter("shiftType");
        String dateStr   = request.getParameter("shiftDate");
        String notes     = request.getParameter("notes");
        String yearStr   = request.getParameter("year");
        String monthStr  = request.getParameter("month");

        String backUrl = buildCalendarUrl(request.getContextPath(), empId, yearStr, monthStr);
        String[] times = (shiftType != null) ? SHIFT_TIMES.get(shiftType) : null;

        if (empId <= 0 || times == null || dateStr == null || dateStr.isEmpty()) {
            request.getSession().setAttribute("flashError", "Thiếu thông tin ca làm việc.");
            response.sendRedirect(backUrl);
            return;
        }

        try {
            LocalDate date  = LocalDate.parse(dateStr);
            LocalTime start = LocalTime.parse(times[0]);

            if (shiftDAO.existsShift(empId, date, start)) {
                request.getSession().setAttribute("flashError", "Nhân viên đã có ca này vào ngày " + dateStr + ".");
                response.sendRedirect(backUrl);
                return;
            }

            WorkShift shift = new WorkShift();
            shift.setEmployeeId(empId);
            shift.setShiftDate(date);
            shift.setStartTime(start);
            shift.setEndTime(LocalTime.parse(times[1]));
            shift.setStatus("Scheduled");
            shift.setNotes(notes);
            shiftDAO.add(shift);
        } catch (Exception e) {
            request.getSession().setAttribute("flashError", "Lỗi hệ thống. Vui lòng thử lại.");
        }
        response.sendRedirect(backUrl);
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request.getParameter("shiftId"), 0);
        if (id <= 0) { response.sendRedirect(request.getContextPath() + LIST_URL); return; }

        WorkShift shift = shiftDAO.getById(id);
        if (shift == null) { response.sendRedirect(request.getContextPath() + LIST_URL); return; }

        String status = request.getParameter("status");
        String notes  = request.getParameter("notes");
        if (status != null && !status.isEmpty()) shift.setStatus(status);
        shift.setNotes(notes);
        shiftDAO.update(shift);

        String year  = String.valueOf(shift.getShiftDate().getYear());
        String month = String.valueOf(shift.getShiftDate().getMonthValue());
        response.sendRedirect(buildCalendarUrl(request.getContextPath(), shift.getEmployeeId(), year, month));
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request.getParameter("shiftId"), 0);
        String empIdStr = request.getParameter("empId");
        String yearStr  = request.getParameter("year");
        String monthStr = request.getParameter("month");
        int empId = parseIntParam(empIdStr, 0);

        if (id > 0) shiftDAO.delete(id);
        response.sendRedirect(buildCalendarUrl(request.getContextPath(), empId, yearStr, monthStr));
    }

    private String buildCalendarUrl(String contextPath, int empId, String year, String month) {
        StringBuilder url = new StringBuilder(contextPath + LIST_URL);
        url.append("?empId=").append(empId);
        if (year  != null && !year.isEmpty())  url.append("&year=").append(year);
        if (month != null && !month.isEmpty()) url.append("&month=").append(month);
        return url.toString();
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
        try { return Integer.parseInt(value.trim()); }
        catch (NumberFormatException e) { return defaultValue; }
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
        return "Work Shift Manager Servlet - Calendar-based scheduling";
    }// </editor-fold>

}
