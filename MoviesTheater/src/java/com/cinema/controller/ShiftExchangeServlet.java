/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.cinema.controller;

import com.cinema.dao.ShiftExchangeDAO;
import com.cinema.model.ShiftExchangeRequest;
import com.cinema.service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Manager review queue for shift hand-off requests.
 *
 * The recipient used to accept or decline a hand-off; approval belongs to the
 * Manager now, so this screen is the only place a request can be settled. The
 * employee side keeps just the two ends it owns: raising a request and cancelling
 * one it has not answered yet.
 *
 * @author tuan6b
 */
public class ShiftExchangeServlet extends HttpServlet {

    private final ShiftExchangeDAO exchangeDAO = new ShiftExchangeDAO();
    private final NotificationService notificationService = new NotificationService();

    private static final String LIST_JSP = "/view/manager/shifts/exchanges.jsp";
    private static final String LIST_URL = "/manager/shift-exchanges";

    /** Statuses the filter accepts, matching CHK_SER_Status in the schema. */
    private static final List<String> FILTER_STATUSES =
            Arrays.asList("Pending", "Accepted", "Rejected", "Cancelled");

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String method = request.getMethod();
        String action = request.getParameter("action");
        if (action == null) action = "";

        if ("POST".equalsIgnoreCase(method)) {
            request.setCharacterEncoding("UTF-8");
            switch (action) {
                case "approve":
                    handleDecision(request, response, true);
                    break;
                case "reject":
                    handleDecision(request, response, false);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + LIST_URL);
                    break;
            }
        } else {
            showList(request, response);
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            transferFlash(session, request, "flashSuccess");
            transferFlash(session, request, "flashError");
        }

        // Anything outside the whitelist (including "all") drops the WHERE clause
        // rather than reaching the DAO, so a hand-edited ?status= cannot filter on
        // a value the Status column could never hold.
        String status = request.getParameter("status");
        if (status == null || status.trim().isEmpty()) {
            status = "Pending";
        }
        String filter = FILTER_STATUSES.contains(status) ? status : "all";

        List<ShiftExchangeRequest> requests =
                exchangeDAO.getAllForManager("all".equals(filter) ? null : filter);

        request.setAttribute("requests", requests);
        request.setAttribute("selectedStatus", filter);
        request.setAttribute("pendingCount", exchangeDAO.countPending());
        request.setAttribute("serverToday", java.time.LocalDate.now());
        request.getRequestDispatcher(LIST_JSP).forward(request, response);
    }

    /**
     * Settles one pending request. The reload afterwards is what makes a request
     * disappear from the Pending list, so the redirect keeps the current filter.
     */
    private void handleDecision(HttpServletRequest request, HttpServletResponse response,
            boolean approve) throws ServletException, IOException {
        HttpSession session = request.getSession();
        int requestId = parseIntParam(request.getParameter("requestId"), 0);
        String status = request.getParameter("status");

        if (requestId <= 0) {
            session.setAttribute("flashError", "Yêu cầu không hợp lệ.");
            response.sendRedirect(buildListUrl(request.getContextPath(), status));
            return;
        }

        String outcome = approve
                ? exchangeDAO.approve(requestId)
                : (exchangeDAO.reject(requestId)
                        ? ShiftExchangeDAO.APPROVE_OK : ShiftExchangeDAO.APPROVE_NOT_PENDING);

        if (ShiftExchangeDAO.APPROVE_OK.equals(outcome)) {
            // Re-read after the write so the notification quotes the shift and the
            // names as they were actually recorded, not as the form claimed.
            ShiftExchangeRequest settled = exchangeDAO.getById(requestId);
            if (settled != null) {
                if (approve) {
                    notificationService.notifyShiftExchangeAccepted(settled);
                } else {
                    notificationService.notifyShiftExchangeRejected(settled);
                }
            }
            session.setAttribute("flashSuccess", approve
                    ? "Đã duyệt yêu cầu đổi ca. Ca làm việc đã được chuyển."
                    : "Đã từ chối yêu cầu đổi ca.");
        } else {
            session.setAttribute("flashError", failureMessage(outcome, approve));
        }
        response.sendRedirect(buildListUrl(request.getContextPath(), status));
    }

    /** Turns an approve() outcome into what the Manager needs to do about it. */
    private String failureMessage(String outcome, boolean approve) {
        if (!approve) {
            return "Không thể từ chối. Yêu cầu có thể đã được xử lý.";
        }
        switch (outcome) {
            case ShiftExchangeDAO.APPROVE_NOT_PENDING:
                return "Yêu cầu này đã được xử lý hoặc đã bị hủy.";
            case ShiftExchangeDAO.APPROVE_SHIFT_MOVED:
                return "Không thể duyệt: ca làm việc không còn thuộc về người gửi yêu cầu.";
            case ShiftExchangeDAO.APPROVE_TARGET_BUSY:
                return "Không thể duyệt: người nhận đã có ca làm việc cùng khung giờ trong ngày đó.";
            default:
                return "Không thể duyệt yêu cầu do lỗi hệ thống. Vui lòng thử lại.";
        }
    }

    private String buildListUrl(String contextPath, String status) {
        String safe = FILTER_STATUSES.contains(status) ? status : "Pending";
        return contextPath + LIST_URL + "?status=" + safe;
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Shift Exchange Servlet - Manager approval queue for shift hand-offs";
    }
}
