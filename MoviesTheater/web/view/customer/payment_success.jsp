<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.cinema.model.Invoice, com.cinema.model.Ticket, com.cinema.model.BookingScheduleView, com.cinema.model.Food" %>
<%@ page import="java.util.List, java.util.Map" %>
<%
    Invoice invoice = (Invoice) request.getAttribute("invoice");
    List<Ticket> tickets = (List<Ticket>) request.getAttribute("tickets");
    BookingScheduleView schedule = (BookingScheduleView) request.getAttribute("schedule");
    Map<Integer, String> barcodeUris = (Map<Integer, String>) request.getAttribute("barcodeUris");
    Map<Integer, Integer> foodQuantities = (Map<Integer, Integer>) request.getAttribute("foodQuantities");
    Map<Integer, Food> foodMap = (Map<Integer, Food>) request.getAttribute("foodMap");

    if (invoice == null || tickets == null || schedule == null) {
        response.sendRedirect(request.getContextPath() + "/showtimes");
        return;
    }

    java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>CGV CINEMA - Đặt vé thành công</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/main.css">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f5f5f5; color: #333; }
        .success-page { max-width: 800px; margin: 0 auto; padding: 20px; }
        .header { background: #d71f2b; color: #fff; padding: 30px 20px; text-align: center; border-radius: 12px 12px 0 0; }
        .header h1 { font-size: 28px; margin-bottom: 5px; }
        .header p { font-size: 14px; opacity: 0.9; }
        .content { background: #fff; padding: 30px; border: 1px solid #ddd; border-top: none; border-radius: 0 0 12px 12px; }
        .section-title { font-size: 16px; font-weight: bold; color: #d71f2b; margin: 20px 0 10px; padding-bottom: 5px; border-bottom: 2px solid #d71f2b; }
        .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 20px; }
        .info-grid div { padding: 8px 0; }
        .info-grid span { color: #888; font-size: 12px; display: block; }
        .info-grid strong { font-size: 14px; }
        .ticket-card { border: 1px solid #ddd; border-radius: 8px; padding: 20px; margin: 15px 0; text-align: center; background: #fafafa; }
        .ticket-card img { display: block; margin: 0 auto 10px; max-width: 100%; }
        .ticket-code { font-size: 20px; font-weight: bold; letter-spacing: 2px; color: #d71f2b; margin: 10px 0; }
        .ticket-seat { font-size: 14px; color: #666; }
        .copy-btn { background: #d71f2b; color: #fff; border: none; padding: 8px 20px; border-radius: 4px; cursor: pointer; font-size: 13px; margin-top: 8px; }
        .copy-btn:hover { background: #b81a24; }
        .copy-btn.copied { background: #28a745; }
        .email-note { background: #e8f5e9; padding: 15px; border-radius: 8px; margin: 20px 0; text-align: center; color: #2e7d32; font-size: 14px; }
        .item-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #eee; font-size: 14px; }
        .total-row { display: flex; justify-content: space-between; padding: 10px 0; font-size: 16px; border-top: 2px solid #d71f2b; margin-top: 15px; }
        .total-row strong { color: #d71f2b; }
        .btn-home { display: block; text-align: center; background: #d71f2b; color: #fff; text-decoration: none; padding: 12px; border-radius: 6px; font-size: 16px; margin-top: 20px; }
        .btn-home:hover { background: #b81a24; }
        .toast { position: fixed; top: 20px; right: 20px; background: #333; color: #fff; padding: 12px 20px; border-radius: 6px; font-size: 14px; opacity: 0; transition: opacity 0.3s; z-index: 9999; }
        .toast.show { opacity: 1; }
        .txn-ref { font-size: 12px; color: #999; text-align: center; margin-top: 15px; }
    </style>
</head>
<body>
<div class="success-page">
    <div class="header">
        <h1>&#10003; ĐẶT VÉ THÀNH CÔNG</h1>
        <p>Cảm ơn bạn đã đặt vé tại CGV Cinema</p>
    </div>
    <div class="content">
        <div class="section-title">THÔNG TIN SUẤT CHIẾU</div>
        <div class="info-grid">
            <div><span>Phim</span><strong><%= schedule.getMovieName() %></strong></div>
            <div><span>Suất chiếu</span><strong><%= schedule.getShowDate() %> - <%= schedule.getStartTime() %></strong></div>
            <div><span>Phòng</span><strong><%= schedule.getRoomNumber() %> (<%= schedule.getRoomType() %>)</strong></div>
            <div><span>Mã giao dịch</span><strong>CGV<%= invoice.getInvoiceId() %></strong></div>
            <% if (invoice.getTransactionRef() != null && !invoice.getTransactionRef().isEmpty()) { %>
            <div><span>Mã VNPAY</span><strong><%= invoice.getTransactionRef() %></strong></div>
            <% } %>
        </div>

        <% if (foodQuantities != null && !foodQuantities.isEmpty() && foodMap != null) { %>
        <div class="section-title">BẮP NƯỚC</div>
        <% for (Map.Entry<Integer, Integer> entry : foodQuantities.entrySet()) {
            Food f = foodMap.get(entry.getKey());
            if (f == null) continue;
            int qty = entry.getValue();
        %>
        <div class="item-row">
            <span><%= qty %> x <%= f.getFoodName() %></span>
            <span><%= df.format(f.getPrice() * qty) %> đ</span>
        </div>
        <% } %>
        <% } %>

        <div class="email-note">&#9993; Vé đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.</div>

        <div style="background:#fff3cd;border:1px solid #ffc107;border-radius:8px;padding:15px;margin:15px 0;text-align:center;">
            <strong style="color:#856404;">&#9888; LƯU MÃ VÉ CỦA BẠN</strong>
            <p style="color:#856404;font-size:13px;margin-top:5px;">Vui lòng sao chép hoặc chụp ảnh mã vạch bên dưới để sử dụng khi soát vé tại rạp. Mỗi vé có một mã riêng.</p>
        </div>

        <div class="section-title">VÉ CỦA BẠN</div>
        <% for (Ticket ticket : tickets) {
            String code = ticket.getCode();
            String seatName = ticket.getSeat() != null
                ? ticket.getSeat().getRowChar() + ticket.getSeat().getColNumber() : "N/A";
            String seatType = ticket.getSeat() != null ? ticket.getSeat().getSeatType() : "";
            String barcodeUri = barcodeUris != null ? barcodeUris.get(ticket.getTicketId()) : null;
        %>
        <div class="ticket-card">
            <% if (barcodeUri != null && !barcodeUri.isEmpty()) { %>
                <img src="<%= barcodeUri %>" alt="<%= code %>" />
            <% } %>
            <div class="ticket-code" id="code-<%= ticket.getTicketId() %>"><%= code %></div>
            <div class="ticket-seat">Ghế: <strong><%= seatName %></strong> <%= seatType.isEmpty() ? "" : "(" + seatType + ")" %> - Giá: <%= df.format(ticket.getPriceAtBooking()) %> đ</div>
            <button class="copy-btn" onclick="copyCode('<%= code %>', this)">Sao chép mã vé</button>
        </div>
        <% } %>

        <div class="total-row">
            <span>Tổng cộng</span>
            <strong><%= df.format(invoice.getTotalAmount()) %> đ</strong>
        </div>

        <div class="txn-ref">Mã giao dịch VNPAY: <%= invoice.getTransactionRef() != null ? invoice.getTransactionRef() : "N/A" %></div>

        <a href="<%= request.getContextPath() %>/" class="btn-home">Về trang chủ</a>
    </div>
</div>

<div class="toast" id="toast"></div>

<script>
function copyCode(code, btn) {
    navigator.clipboard.writeText(code).then(function() {
        btn.textContent = "Đã sao chép!";
        btn.classList.add("copied");
        showToast("Đã sao chép mã: " + code);
        setTimeout(function() {
            btn.textContent = "Sao chép mã vé";
            btn.classList.remove("copied");
        }, 2000);
    }).catch(function() {
        showToast("Không thể sao chép. Vui lòng thử lại.");
    });
}

function showToast(msg) {
    var toast = document.getElementById("toast");
    toast.textContent = msg;
    toast.classList.add("show");
    setTimeout(function() { toast.classList.remove("show"); }, 2500);
}
</script>
</body>
</html>
