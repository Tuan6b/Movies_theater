package com.cinema.controller;

import com.cinema.dao.BookingScheduleDAO;
import com.cinema.dao.FoodDAO;
import com.cinema.dao.InvoiceDAO;
import com.cinema.dao.InvoiceFoodDAO;
import com.cinema.dao.TicketDAO;
import com.cinema.model.Account;
import com.cinema.model.BookingCart;
import com.cinema.model.BookingScheduleView;
import com.cinema.model.Food;
import com.cinema.model.Invoice;
import com.cinema.model.Ticket;
import com.cinema.service.EmailService;
import com.cinema.util.DBUtils;
import com.cinema.util.VNPAYConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "VNPayServlet", urlPatterns = {"/vnpay"})
public class VNPayServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(VNPayServlet.class.getName());
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final TicketDAO ticketDAO = new TicketDAO();
    private final InvoiceFoodDAO invoiceFoodDAO = new InvoiceFoodDAO();
    private final FoodDAO foodDAO = new FoodDAO();
    private final BookingScheduleDAO scheduleDAO = new BookingScheduleDAO();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }
        switch (action) {
            case "create":
                handleCreate(request, response);
                break;
            case "return":
                handleReturn(request, response);
                break;
            case "ipn":
                handleIpn(request, response);
                break;
            case "cleanup":
                handleCleanup(request, response);
                break;
            case "status":
                handleStatus(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/index.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        BookingCart cart = (BookingCart) (session != null ? session.getAttribute("bookingCart") : null);
        Account account = (Account) (session != null ? session.getAttribute("account") : null);
        BookingScheduleView schedule = (BookingScheduleView) (session != null ? session.getAttribute("bookingSchedule") : null);

        if (cart == null || account == null || schedule == null) {
            response.sendRedirect(request.getContextPath() + "/showtimes");
            return;
        }

        int invoiceId;
        Connection conn = null;
        try {
            conn = DBUtils.getConnection();
            conn.setAutoCommit(false);

            invoiceId = invoiceDAO.createPending(
                    account.getAccountId(),
                    cart.getAppliedPromotionId(),
                    cart.getGrandTotal(),
                    cart.getDiscountAmount(),
                    cart.getFinalTotal()
            );

            if (invoiceId < 0) {
                throw new SQLException("Failed to create pending invoice");
            }

            ticketDAO.createPendingTickets(conn, invoiceId, cart.getScheduleId(),
                    cart.getSeatIds(), cart.getSeatPrices());

            if (cart.getFoodQuantities() != null && !cart.getFoodQuantities().isEmpty()) {
                Map<Integer, Double> foodPrices = new HashMap<>();
                for (Map.Entry<Integer, Integer> entry : cart.getFoodQuantities().entrySet()) {
                    Food food = foodDAO.getFoodById(entry.getKey());
                    if (food != null) {
                        foodPrices.put(entry.getKey(), food.getPrice());
                    }
                }
                invoiceFoodDAO.insert(invoiceId, cart.getFoodQuantities(), foodPrices);
            }

            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            LOG.log(Level.SEVERE, "Failed to create pending booking", e);
            response.sendRedirect(request.getContextPath() + "/booking?action=checkout&error=system");
            return;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }

        String baseUrl = request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort() + request.getContextPath();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expire = now.plusMinutes(15);
        long amount = (long) (cart.getFinalTotal() * 100);

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", VNPAYConfig.TMN_CODE);
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_BankCode", "VNBANK");
        params.put("vnp_CreateDate", VNPAYConfig.formatDate(now));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_IpAddr", request.getRemoteAddr());
        params.put("vnp_Locale", "vn");
        params.put("vnp_OrderInfo", "CGV" + invoiceId);
        params.put("vnp_OrderType", "other");
        params.put("vnp_ReturnUrl", baseUrl + VNPAYConfig.RETURN_URL);
        params.put("vnp_ExpireDate", VNPAYConfig.formatDate(expire));
        params.put("vnp_TxnRef", "CGV" + invoiceId);

        String paymentUrl = VNPAYConfig.buildSignedUrl(params, VNPAYConfig.PAY_URL);
        response.sendRedirect(paymentUrl);
    }

    private void handleReturn(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, String> params = extractParams(request);
        if (!VNPAYConfig.verifySignature(params)) {
            request.setAttribute("error", "Chữ ký không hợp lệ.");
            request.getRequestDispatcher("/view/customer/payment_failed.jsp").forward(request, response);
            return;
        }

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        if (txnRef == null || !txnRef.startsWith("CGV")) {
            request.setAttribute("error", "Mã giao dịch không hợp lệ.");
            request.getRequestDispatcher("/view/customer/payment_failed.jsp").forward(request, response);
            return;
        }

        int invoiceId;
        try {
            invoiceId = Integer.parseInt(txnRef.substring(3));
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Mã giao dịch không hợp lệ.");
            request.getRequestDispatcher("/view/customer/payment_failed.jsp").forward(request, response);
            return;
        }

        cleanupExpired();

        Invoice invoice = invoiceDAO.findByInvoiceId(invoiceId);
        if (invoice == null) {
            request.setAttribute("error", "Không tìm thấy hóa đơn.");
            request.getRequestDispatcher("/view/customer/payment_failed.jsp").forward(request, response);
            return;
        }

        String status = invoice.getPaymentStatus();
        if ("Paid".equals(status)) {
            LOG.info("Return URL: invoice " + invoiceId + " already paid, loading data");
            loadPaymentData(request, invoiceId, invoice);
            request.getRequestDispatcher("/view/customer/payment_success.jsp").forward(request, response);
        } else if ("Pending".equals(status)) {
            if ("00".equals(responseCode)) {
                String vnpTxnNo = params.get("vnp_TransactionNo");
                String bankCode = params.get("vnp_BankCode");
                String payDate = params.get("vnp_PayDate");
                boolean confirmed = confirmPayment(invoiceId, vnpTxnNo, bankCode, payDate);
                if (confirmed) {
                    invoice = invoiceDAO.findByInvoiceId(invoiceId);
                    loadPaymentData(request, invoiceId, invoice);
                    trySendEmail(invoiceId);
                    request.getRequestDispatcher("/view/customer/payment_success.jsp").forward(request, response);
                } else {
                    request.setAttribute("error", "Xử lý thanh toán thất bại. Vui lòng liên hệ CSKH.");
                    request.getRequestDispatcher("/view/customer/payment_failed.jsp").forward(request, response);
                }
            } else {
                markFailedAndCleanup(invoiceId);
                String errorMsg = getResponseMessage(responseCode);
                request.setAttribute("error", "Thanh toán thất bại: " + errorMsg);
                request.getRequestDispatcher("/view/customer/payment_failed.jsp").forward(request, response);
            }
        } else {
            request.setAttribute("error", "Hóa đơn đã bị hủy hoặc hết hạn.");
            request.getRequestDispatcher("/view/customer/payment_failed.jsp").forward(request, response);
        }
    }

    private void handleIpn(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, String> params = extractParams(request);

        if (!VNPAYConfig.verifySignature(params)) {
            writeIpnResponse(response, "97", "Invalid signature");
            return;
        }

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");

        if (txnRef == null || !txnRef.startsWith("CGV")) {
            writeIpnResponse(response, "01", "Invalid TxnRef");
            return;
        }

        int invoiceId;
        try {
            invoiceId = Integer.parseInt(txnRef.substring(3));
        } catch (NumberFormatException e) {
            writeIpnResponse(response, "01", "Invalid TxnRef");
            return;
        }

        cleanupExpired();

        String currentStatus = invoiceDAO.getPaymentStatus(invoiceId);
        if (currentStatus == null) {
            writeIpnResponse(response, "01", "Invoice not found");
            return;
        }

        if ("Paid".equals(currentStatus)) {
            LOG.info("IPN: invoice " + invoiceId + " already paid, idempotent ack");
            writeIpnResponse(response, "00", "Success");
            return;
        }

        if (!"Pending".equals(currentStatus)) {
            writeIpnResponse(response, "02", "Invoice already processed");
            return;
        }

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            String vnpTxnNo = params.get("vnp_TransactionNo");
            String bankCode = params.get("vnp_BankCode");
            String payDate = params.get("vnp_PayDate");

            boolean confirmed = confirmPayment(invoiceId, vnpTxnNo, bankCode, payDate);
            if (confirmed) {
                trySendEmail(invoiceId);
                writeIpnResponse(response, "00", "Success");
            } else {
                writeIpnResponse(response, "99", "Internal error");
            }
        } else {
            markFailedAndCleanup(invoiceId);
            writeIpnResponse(response, "00", "Fail");
        }
    }

    private void handleCleanup(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        cleanupExpired();
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":\"ok\"}");
    }

    private void cleanupExpired() {
        List<Invoice> expired = invoiceDAO.findExpiredPending(15);
        for (Invoice inv : expired) {
            int id = inv.getInvoiceId();
            int updated = invoiceDAO.updateStatusAtomic(id, "Failed", "Pending");
            if (updated == 1) {
                ticketDAO.deleteByInvoiceId(id);
                invoiceFoodDAO.deleteByInvoiceId(id);
                LOG.info("Cleaned up expired pending invoice " + id);
            }
        }
    }

    private void markFailedAndCleanup(int invoiceId) {
        int updated = invoiceDAO.updateStatusAtomic(invoiceId, "Failed", "Pending");
        if (updated == 1) {
            ticketDAO.deleteByInvoiceId(invoiceId);
            invoiceFoodDAO.deleteByInvoiceId(invoiceId);
        }
    }

    private void loadPaymentData(HttpServletRequest request, int invoiceId, Invoice invoice) {
        List<Ticket> tickets = ticketDAO.getByInvoiceId(invoiceId);
        int scheduleId = invoiceDAO.getScheduleIdByInvoice(invoiceId);
        BookingScheduleView schedule = scheduleDAO.getScheduleById(scheduleId);

        Map<Integer, String> barcodeUris = new HashMap<>();
        for (Ticket t : tickets) {
            try {
                String uri = com.cinema.util.BarcodeUtil.generateBarcodeDataUri(t.getCode(), 300, 80);
                barcodeUris.put(t.getTicketId(), uri);
                LOG.fine("Barcode generated for ticket " + t.getTicketId() + " code=" + t.getCode());
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Barcode generation failed for ticket " + t.getTicketId(), e);
            }
        }

        Map<Integer, Integer> foodQuantities = invoiceFoodDAO.getByInvoiceId(invoiceId);
        Map<Integer, Food> foodMap = new HashMap<>();
        if (foodQuantities != null && !foodQuantities.isEmpty()) {
            for (Integer foodId : foodQuantities.keySet()) {
                Food f = foodDAO.getFoodById(foodId);
                if (f != null) foodMap.put(foodId, f);
            }
        }

        LOG.info("Payment data loaded for invoice " + invoiceId + ": " + tickets.size() + " tickets, "
                + foodMap.size() + " food items");

        request.setAttribute("invoice", invoice);
        request.setAttribute("tickets", tickets);
        request.setAttribute("schedule", schedule);
        request.setAttribute("barcodeUris", barcodeUris);
        request.setAttribute("foodQuantities", foodQuantities);
        request.setAttribute("foodMap", foodMap);
    }

    private boolean confirmPayment(int invoiceId, String vnpTxnNo, String bankCode, String payDate) {
        Connection conn = null;
        try {
            conn = DBUtils.getConnection();
            conn.setAutoCommit(false);

            int updated = invoiceDAO.updateStatusAtomic(conn, invoiceId, "Paid", "Pending");
            if (updated != 1) {
                conn.rollback();
                LOG.warning("confirmPayment: invoice " + invoiceId + " not in Pending state, skipping");
                return false;
            }

            int scheduleId = invoiceDAO.getScheduleIdByInvoice(conn, invoiceId);
            ticketDAO.finalizeTickets(conn, invoiceId, scheduleId);
            invoiceDAO.updateTxnDetails(conn, invoiceId, vnpTxnNo, bankCode, payDate);
            invoiceDAO.incrementPromotionUsage(conn, invoiceId);

            conn.commit();
            LOG.info("Payment confirmed for invoice " + invoiceId + " (txn=" + vnpTxnNo + ")");
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            LOG.log(Level.SEVERE, "Payment confirmation failed for invoice " + invoiceId, e);
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { /* ignore */ }
            }
        }
    }

    private void trySendEmail(int invoiceId) {
        try {
            Invoice invoice = invoiceDAO.findByInvoiceId(invoiceId);
            if (invoice == null) {
                LOG.warning("Cannot send email: invoice " + invoiceId + " not found");
                return;
            }

            int scheduleId = invoiceDAO.getScheduleIdByInvoice(invoiceId);
            BookingScheduleView schedule = scheduleDAO.getScheduleById(scheduleId);
            if (schedule == null) {
                LOG.warning("Cannot send email: schedule not found for invoice " + invoiceId);
                return;
            }

            List<Ticket> tickets = ticketDAO.getByInvoiceId(invoiceId);
            List<String> ticketCodes = new ArrayList<>();
            List<String> seatNames = new ArrayList<>();
            for (Ticket t : tickets) {
                ticketCodes.add(t.getCode());
                seatNames.add(t.getSeat() != null
                        ? t.getSeat().getRowChar() + t.getSeat().getColNumber()
                        : "N/A");
            }

            Account account = new com.cinema.dao.AccountDAO().getAccountById(invoice.getAccountId());
            if (account == null) {
                LOG.warning("Cannot send email: account not found for invoice " + invoiceId);
                return;
            }

            java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
            String totalStr = df.format(invoice.getTotalAmount()) + " đ";

            StringBuilder foodSummary = new StringBuilder();
            Map<Integer, Integer> foodQtys = invoiceFoodDAO.getByInvoiceId(invoiceId);
            if (foodQtys != null && !foodQtys.isEmpty()) {
                for (Map.Entry<Integer, Integer> entry : foodQtys.entrySet()) {
                    Food f = foodDAO.getFoodById(entry.getKey());
                    if (f != null) {
                        if (foodSummary.length() > 0) foodSummary.append(", ");
                        foodSummary.append(entry.getValue()).append("x ").append(f.getFoodName());
                    }
                }
            }

            emailService.sendTicketConfirmation(
                    account.getEmail(),
                    account.getFullName() != null ? account.getFullName() : account.getEmail(),
                    schedule.getMovieName(),
                    schedule.getShowDate(),
                    schedule.getStartTime(),
                    schedule.getRoomNumber() + " (" + schedule.getRoomType() + ")",
                    seatNames,
                    ticketCodes,
                    foodSummary.toString(),
                    totalStr,
                    "CGV" + invoiceId
            );
            LOG.info("Confirmation email sent to " + account.getEmail() + " for invoice " + invoiceId);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Email sending failed for invoice " + invoiceId, e);
        }
    }

    private void writeIpnResponse(HttpServletResponse response, String code, String message)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"RspCode\":\"" + code + "\",\"Message\":\"" + message + "\"}");
    }

    private void handleStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String invoiceIdRaw = request.getParameter("invoiceId");
        response.setContentType("application/json;charset=UTF-8");
        if (invoiceIdRaw == null) {
            response.getWriter().write("{\"status\":\"invalid\"}");
            return;
        }
        try {
            int invoiceId = Integer.parseInt(invoiceIdRaw);
            String status = invoiceDAO.getPaymentStatus(invoiceId);
            if (status == null) status = "not_found";
            response.getWriter().write("{\"status\":\"" + status + "\"}");
        } catch (NumberFormatException e) {
            response.getWriter().write("{\"status\":\"invalid\"}");
        }
    }

    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                params.put(entry.getKey(), values[0]);
            }
        }
        return params;
    }

    private String getResponseMessage(String code) {
        switch (code) {
            case "00": return "Giao dịch thành công";
            case "07": return "Trừ tiền thành công - Giao dịch bị nghi ngờ";
            case "09": return "Thẻ/Tài khoản chưa đăng ký Internet Banking";
            case "10": return "Xác thực thông tin thẻ/tài khoản không đúng";
            case "11": return "Đã hết hạn chờ thanh toán";
            case "12": return "Thẻ/Tài khoản bị khóa";
            case "13": return "Sai mật khẩu xác thực (OTP)";
            case "24": return "Khách hàng hủy giao dịch";
            case "51": return "Tài khoản không đủ số dư";
            case "65": return "Tài khoản vượt quá hạn mức giao dịch";
            case "75": return "Ngân hàng đang bảo trì";
            case "79": return "Sai mật khẩu thanh toán";
            case "99": return "Lỗi không xác định";
            default: return "Mã lỗi: " + code;
        }
    }
}
