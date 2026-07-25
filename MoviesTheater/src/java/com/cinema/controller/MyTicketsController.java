package com.cinema.controller;

import com.cinema.dao.CustomerTicketHistoryDAO;
import com.cinema.dao.InvoiceDAO;
import com.cinema.dao.TicketDAO;
import com.cinema.model.Account;
import com.cinema.model.CustomerTicketHistory;
import com.cinema.model.CustomerTicketHistory.TicketItem;
import com.cinema.model.Ticket;
import com.cinema.util.BarcodeUtil;
import com.cinema.util.TicketQrUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer ticket history and ticket-detail controller.
 */
public class MyTicketsController extends HttpServlet {

    private static final int PAGE_SIZE = 6;
    private static final int MAX_SEARCH_LENGTH = 100;

    private final CustomerTicketHistoryDAO historyDAO = new CustomerTicketHistoryDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final TicketDAO ticketDAO = new TicketDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");

        Account account = getAuthenticatedCustomer(request, response);
        if (account == null) {
            return;
        }

        String action = normalize(request.getParameter("action"));
        if ("detail".equals(action)) {
            showDetail(request, response, account);
        } else {
            showHistory(request, response, account);
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");

        Account account = getAuthenticatedCustomer(request, response);
        if (account == null) {
            return;
        }

        String action = normalize(request.getParameter("action"));
        if ("save".equals(action)) {
            saveToMyTickets(request, response, account);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thao tác không hợp lệ.");
        }
    }

    private void saveToMyTickets(HttpServletRequest request, HttpServletResponse response,
            Account account) throws IOException {
        int invoiceId = parsePositiveInt(request.getParameter("invoiceId"), -1);
        if (invoiceId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã hóa đơn không hợp lệ.");
            return;
        }

        boolean saved = invoiceDAO.saveToMyTickets(invoiceId, account.getAccountId());
        if (!saved) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Không thể lưu vé. Hóa đơn không tồn tại, chưa thanh toán hoặc không thuộc tài khoản của bạn.");
            return;
        }

        response.sendRedirect(request.getContextPath()
                + "/my-tickets?action=detail&invoiceId=" + invoiceId + "&saved=1");
    }

    private void showHistory(HttpServletRequest request, HttpServletResponse response,
            Account account) throws ServletException, IOException {
        String keyword = normalize(request.getParameter("keyword"));
        if (keyword.length() > MAX_SEARCH_LENGTH) {
            keyword = keyword.substring(0, MAX_SEARCH_LENGTH);
        }

        String status = normalizeStatus(request.getParameter("status"));
        int page = parsePositiveInt(request.getParameter("page"), 1);

        int totalItems = historyDAO.countHistory(account.getAccountId(), keyword, status);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        if (page > totalPages) {
            page = totalPages;
        }

        int offset = (page - 1) * PAGE_SIZE;
        List<CustomerTicketHistory> bookings = historyDAO.findHistory(
                account.getAccountId(), keyword, status, offset, PAGE_SIZE);

        request.setAttribute("bookings", bookings);
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", status);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalItems", totalItems);
        request.setAttribute("allCount", historyDAO.countHistory(account.getAccountId(), keyword, "all"));
        request.setAttribute("validCount", historyDAO.countHistory(account.getAccountId(), keyword, "valid"));
        request.setAttribute("expiredCount", historyDAO.countHistory(account.getAccountId(), keyword, "expired"));
        request.setAttribute("viewMode", "list");
        request.getRequestDispatcher("/view/customer/my-tickets.jsp").forward(request, response);
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response,
            Account account) throws ServletException, IOException {
        int invoiceId = parsePositiveInt(request.getParameter("invoiceId"), -1);
        if (invoiceId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã hóa đơn không hợp lệ.");
            return;
        }

        CustomerTicketHistory booking = historyDAO.findOwnedDetail(account.getAccountId(), invoiceId);
        if (booking == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy vé hoặc vé không thuộc tài khoản của bạn.");
            return;
        }

        addBookingQr(booking);
        request.setAttribute("booking", booking);
        request.setAttribute("viewMode", "detail");
        request.getRequestDispatcher("/view/customer/my-tickets.jsp").forward(request, response);
    }

    private void addBookingQr(CustomerTicketHistory booking) {
        List<Ticket> tickets = ticketDAO.getByInvoiceId(booking.getInvoiceId());
        if (tickets == null || tickets.isEmpty()) {
            return;
        }

        List<String> seatNames = new ArrayList<>();
        for (TicketItem item : booking.getTickets()) {
            seatNames.add(item.getSeatName());
        }

        try {
            String qrPayload = TicketQrUtil.buildBookingPayload(tickets, seatNames);
            booking.setBookingCode(TicketQrUtil.getPrimaryTicketCode(tickets));
            booking.setQrDataUri(BarcodeUtil.generateQrCodeDataUri(qrPayload, 280, 280));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Account getAuthenticatedCustomer(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Account account = session == null ? null : (Account) session.getAttribute("account");
        if (account == null) {
            HttpSession loginSession = request.getSession(true);
            loginSession.setAttribute("redirectAfterLogin", request.getRequestURI());
            response.sendRedirect(request.getContextPath() + "/Login");
            return null;
        }
        if (account.getRoleId() != 2) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Chức năng Vé của tôi chỉ dành cho tài khoản khách hàng.");
            return null;
        }
        return account;
    }

    private String normalizeStatus(String value) {
        String status = normalize(value).toLowerCase();
        if ("valid".equals(status) || "expired".equals(status)) {
            return status;
        }
        return "all";
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private int parsePositiveInt(String value, int defaultValue) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
