<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.cinema.model.BookingScheduleView,com.cinema.model.Food,com.cinema.model.Ticket" %>
<%@ page import="java.util.List,java.util.Map" %>
<%
    List<Ticket> tickets = (List<Ticket>) request.getAttribute("tickets");
    BookingScheduleView schedule = (BookingScheduleView) request.getAttribute("schedule");
    List<Integer> seatIds = (List<Integer>) request.getAttribute("seatIds");
    List<String> seatNames = (List<String>) request.getAttribute("seatNames");
    Map<Integer, Integer> foodQuantities = (Map<Integer, Integer>) request.getAttribute("foodQuantities");
    Map<Integer, Food> foodMap = (Map<Integer, Food>) request.getAttribute("foodMap");
    Double total = (Double) request.getAttribute("total");
    String paymentMethod = (String) request.getAttribute("paymentMethod");
    Boolean emailSent = (Boolean) request.getAttribute("emailSent");
    String email = (String) request.getAttribute("email");

    if (tickets == null || schedule == null) {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>CGV CINEMA - Vé của bạn</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/checkout.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/ticket_confirmation.css">
</head>
<body>

<div class="checkout-page">

    <div class="top-dark">
        <div class="header-inner">
            <a href="<%= request.getContextPath() %>/" class="logo">CGV CINEMA</a>
            <div class="nav">
                <a href="<%= request.getContextPath() %>/index.jsp">Trang chủ</a>
                <a href="<%= request.getContextPath() %>/showtimes">Lịch chiếu</a>
                <a href="#">Ưu đãi</a>
            </div>
        </div>
    </div>

    <main class="checkout-container">

        <section class="booking-step">
            <div class="step">
                <span>1</span>
                <p>Chọn ghế</p>
            </div>
            <div class="step-line"></div>
            <div class="step">
                <span>2</span>
                <p>Chọn đồ ăn</p>
            </div>
            <div class="step-line"></div>
            <div class="step">
                <span>3</span>
                <p>Thanh toán</p>
            </div>
            <div class="step-line"></div>
            <div class="step active">
                <span>4</span>
                <p>Nhận vé</p>
            </div>
        </section>

        <section class="section-title">
            <div class="title-icon">&#9989;</div>
            <h1>THANH TOÁN THÀNH CÔNG</h1>
        </section>

        <div class="reminder-banner">
            <strong>Vui lòng lưu lại vé!</strong>
            Hãy chụp màn hình hoặc lưu mã vé / QR code bên dưới để xuất trình tại quầy vé khi đến rạp.
            <% if (Boolean.TRUE.equals(emailSent)) { %>
                Vé cũng đã được gửi tới email <strong><%= email %></strong>.
            <% } else { %>
                Không thể gửi email vé lúc này — vui lòng lưu lại thông tin bên dưới để đảm bảo an toàn.
            <% } %>
        </div>

        <div class="checkout-layout">

            <div class="checkout-main">

                <div class="checkout-card">
                    <h3 class="card-title">Thông tin suất chiếu</h3>
                    <div class="info-grid">
                        <div><span>Phim</span><strong><%= schedule.getMovieName() %></strong></div>
                        <div><span>Suất chiếu</span><strong><%= schedule.getStartTime() %> - <%= schedule.getShowDate() %></strong></div>
                        <div><span>Phòng</span><strong><%= schedule.getRoomNumber() %> (<%= schedule.getRoomType() %>)</strong></div>
                    </div>
                </div>

                <div class="checkout-card">
                    <h3 class="card-title">Vé của bạn (<%= tickets.size() %> vé)</h3>
                    <div class="ticket-grid">
                        <% for (Ticket ticket : tickets) {
                            String seatLabel = "";
                            if (seatIds != null && seatNames != null) {
                                for (int i = 0; i < seatIds.size(); i++) {
                                    if (seatIds.get(i) == ticket.getSeatId()) {
                                        seatLabel = seatNames.get(i);
                                        break;
                                    }
                                }
                            }
                        %>
                        <div class="ticket-card">
                            <div class="ticket-qr" data-code="<%= ticket.getCode() %>"></div>
                            <div class="ticket-seat">Ghế <%= seatLabel %></div>
                            <div class="ticket-code"><%= ticket.getCode() %></div>
                            <div class="ticket-price"><%= String.format("%,.0f", ticket.getPriceAtBooking()) %> đ</div>
                        </div>
                        <% } %>
                    </div>
                </div>

                <% if (foodQuantities != null && !foodQuantities.isEmpty() && foodMap != null) { %>
                <div class="checkout-card">
                    <h3 class="card-title">Bắp nước đã đặt</h3>
                    <% for (Map.Entry<Integer, Integer> entry : foodQuantities.entrySet()) {
                        Food food = foodMap.get(entry.getKey());
                        if (food == null) continue;
                    %>
                    <div class="item-row">
                        <span><%= entry.getValue() %> x <%= food.getFoodName() %></span>
                        <span><%= String.format("%,.0f", food.getPrice() * entry.getValue()) %> đ</span>
                    </div>
                    <% } %>
                </div>
                <% } %>

            </div>

            <div class="checkout-sidebar">
                <div class="summary-card">
                    <h3>Hóa đơn</h3>
                    <div class="summary-row">
                        <span>Phương thức thanh toán</span>
                        <span><%= paymentMethod %></span>
                    </div>
                    <div class="summary-divider"></div>
                    <div class="summary-row summary-total">
                        <span>Tổng đã thanh toán</span>
                        <span><%= String.format("%,.0f", total != null ? total : 0) %> đ</span>
                    </div>
                    <button type="button" class="btn btn-next btn-block" onclick="window.print()">In / Lưu vé</button>
                    <a href="<%= request.getContextPath() %>/index.jsp" class="btn btn-back btn-block" style="margin-top:10px;">Về trang chủ</a>
                </div>
            </div>

        </div>

    </main>

    <footer class="footer-dark">
        <div class="footer-inner">
            <strong>CGV CINEMA</strong>
            <span>© 2026 CGV Cinema. Hệ thống quản lý rạp chiếu phim</span>
        </div>
    </footer>

</div>

<%-- qrcodejs 1.0.0, served from web/js instead of a CDN: without a network the
     customer would get no QR at all, and the counter has nothing to scan. The
     vendored file is byte-identical to the CDN copy (same sha384 as the integrity
     attribute this tag used to carry). --%>
<script src="${pageContext.request.contextPath}/js/qrcode.min.js"></script>
<script>
    document.querySelectorAll(".ticket-qr").forEach(function (el) {
        new QRCode(el, {
            text: el.dataset.code,
            width: 140,
            height: 140,
            correctLevel: QRCode.CorrectLevel.M
        });
    });
</script>

</body>
</html>
