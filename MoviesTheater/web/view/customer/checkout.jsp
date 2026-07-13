<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.cinema.model.BookingCart,com.cinema.model.BookingScheduleView,com.cinema.model.Food,com.cinema.model.Promotion" %>
<%@ page import="java.util.List" %>
<%
    BookingCart cart = (BookingCart) session.getAttribute("bookingCart");
    BookingScheduleView schedule = (BookingScheduleView) session.getAttribute("bookingSchedule");
    List<Food> foodList = (List<Food>) request.getAttribute("foodList");
    List<Promotion> promotions = (List<Promotion>) request.getAttribute("promotions");

    if (cart == null || schedule == null) {
        response.sendRedirect(request.getContextPath() + "/showtimes");
        return;
    }

    StringBuilder seatNamesStr = new StringBuilder();
    if (cart.getSeatNames() != null) {
        for (int i = 0; i < cart.getSeatNames().size(); i++) {
            seatNamesStr.append(cart.getSeatNames().get(i));
            if (i < cart.getSeatNames().size() - 1) seatNamesStr.append(", ");
        }
    }

    // Build promotions JSON for JS calculation
    String promotionsJson = "[]";
    if (promotions != null && !promotions.isEmpty()) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < promotions.size(); i++) {
            Promotion p = promotions.get(i);
            json.append("{");
            json.append("\"id\":").append(p.getPromotionId()).append(",");
            json.append("\"code\":\"").append(p.getPromotionCode().replace("\"", "\\\"")).append("\",");
            json.append("\"type\":\"").append(p.getDiscountType()).append("\",");
            json.append("\"value\":").append(p.getDiscountValue()).append(",");
            json.append("\"minOrder\":").append(p.getMinOrderAmount() != null ? p.getMinOrderAmount() : 0).append(",");
            json.append("\"maxDiscount\":").append(p.getMaxDiscountAmount() != null ? p.getMaxDiscountAmount() : "null");
            json.append("}");
            if (i < promotions.size() - 1) json.append(",");
        }
        json.append("]");
        promotionsJson = json.toString();
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>CGV CINEMA - Thanh toán</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/main.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/checkout.css">
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
            <div class="step active">
                <span>3</span>
                <p>Thanh toán</p>
            </div>
            <div class="step-line"></div>
            <div class="step">
                <span>4</span>
                <p>Nhận vé</p>
            </div>
        </section>

        <section class="section-title">
            <div class="title-icon">&#9746;</div>
            <h1>XÁC NHẬN THANH TOÁN</h1>
        </section>

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
                    <h3 class="card-title">Vé đã chọn</h3>
                    <div class="seat-summary">
                        <span>Ghế: <strong><%= seatNamesStr.toString() %></strong></span>
                        <span class="seat-summary-count"><%= cart.getSeatIds().size() %> vé</span>
                    </div>
                    <div class="item-row">
                        <span>Tiền vé</span>
                        <span><%= String.format("%,.0f", cart.getTicketTotal()) %> đ</span>
                    </div>
                </div>

                <% if (foodList != null && cart.getFoodQuantities() != null && !cart.getFoodQuantities().isEmpty()) { %>
                <div class="checkout-card">
                    <h3 class="card-title">Bắp nước đã chọn</h3>
                    <% for (Food food : foodList) {
                        Integer qty = cart.getFoodQuantities().get(food.getFoodId());
                        if (qty != null && qty > 0) {
                    %>
                    <div class="item-row">
                        <span><%= qty %> x <%= food.getFoodName() %></span>
                        <span><%= String.format("%,.0f", food.getPrice() * qty) %> đ</span>
                    </div>
                    <%  }
                    } %>
                </div>
                <% } %>

                <div class="checkout-card">
                    <h3 class="card-title">Mã khuyến mãi</h3>
                    <select id="promotionSelect" class="promotion-select">
                        <option value="">-- Chọn khuyến mãi --</option>
                        <% if (promotions != null) {
                            for (Promotion p : promotions) {
                                String desc = p.getDescription() != null ? p.getDescription() : p.getPromotionCode();
                        %>
                            <option value="<%= p.getPromotionId() %>"
                                    data-type="<%= p.getDiscountType() %>"
                                    data-value="<%= p.getDiscountValue() %>"
                                    data-min-order="<%= p.getMinOrderAmount() != null ? p.getMinOrderAmount() : 0 %>"
                                    data-max-discount="<%= p.getMaxDiscountAmount() != null ? p.getMaxDiscountAmount() : "" %>">
                                <%= p.getPromotionCode() %> - <%= desc %>
                                (<%= p.getDiscountType().equals("Percentage") ? p.getDiscountValue().intValue() + "%" : String.format("%,.0f", p.getDiscountValue()) + "đ" %>)
                            </option>
                        <%  }
                        } %>
                    </select>
                    <div id="promotionInfo" class="promotion-info"></div>
                </div>

            </div>

            <div class="checkout-sidebar">
                <div class="summary-card">
                    <h3>Hóa đơn tạm tính</h3>
                    <div class="summary-row">
                        <span>Tiền vé</span>
                        <span id="summaryTicketTotal"><%= String.format("%,.0f", cart.getTicketTotal()) %> đ</span>
                    </div>
                    <div class="summary-row">
                        <span>Bắp nước</span>
                        <span id="summaryFoodTotal"><%= String.format("%,.0f", cart.getFoodTotal()) %> đ</span>
                    </div>
                    <div class="summary-divider"></div>
                    <div class="summary-row">
                        <span>Tạm tính</span>
                        <span id="summarySubtotal"><%= String.format("%,.0f", cart.getGrandTotal()) %> đ</span>
                    </div>
                    <div class="summary-row discount-row" id="discountRow" style="display:none;">
                        <span>Giảm giá</span>
                        <span id="summaryDiscount">-0 đ</span>
                    </div>
                    <div class="summary-divider"></div>
                    <div class="summary-row summary-total">
                        <span>Tổng cộng</span>
                        <span id="summaryTotal"><%= String.format("%,.0f", cart.getGrandTotal()) %> đ</span>
                    </div>
                    <form action="<%= request.getContextPath() %>/booking" method="post">
                        <input type="hidden" name="action" value="confirmPayment">
                        <input type="hidden" name="promotionId" id="selectedPromotionId" value="">
                        <button type="submit" class="btn btn-next btn-block">Xác nhận thanh toán</button>
                    </form>
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

<script>
    const promotions = <%= promotionsJson %>;
    const subtotal = <%= cart.getGrandTotal() %>;
    const ticketTotalVal = <%= cart.getTicketTotal() %>;
    const foodTotalVal = <%= cart.getFoodTotal() %>;

    const promoSelect = document.getElementById("promotionSelect");
    const discountRow = document.getElementById("discountRow");
    const summaryDiscount = document.getElementById("summaryDiscount");
    const summaryTotal = document.getElementById("summaryTotal");
    const summarySubtotal = document.getElementById("summarySubtotal");
    const promoInfo = document.getElementById("promotionInfo");
    const selectedPromotionId = document.getElementById("selectedPromotionId");

    function formatMoney(value) {
        return new Intl.NumberFormat("vi-VN").format(value) + " đ";
    }

    function updateTotal() {
        const selectedId = promoSelect.value;
        let discount = 0;
        let promoText = "";

        if (selectedId) {
            const promo = promotions.find(function(p) { return p.id == selectedId; });
            if (promo) {
                if (subtotal >= promo.minOrder) {
                    if (promo.type === "Percentage") {
                        discount = subtotal * promo.value / 100;
                        if (promo.maxDiscount && discount > promo.maxDiscount) {
                            discount = promo.maxDiscount;
                        }
                    } else {
                        discount = promo.value;
                    }
                    promoText = "Áp dụng mã " + promo.code + ": giảm " + (promo.type === "Percentage" ? promo.value + "%" : formatMoney(promo.value));
                    if (promo.maxDiscount) {
                        promoText += " (tối đa " + formatMoney(promo.maxDiscount) + ")";
                    }
                } else {
                    promoText = "Đơn hàng tối thiểu " + formatMoney(promo.minOrder) + " để áp dụng mã này.";
                    promoSelect.value = "";
                }
            }
        }

        if (discount > 0) {
            discountRow.style.display = "flex";
            summaryDiscount.innerText = "-" + formatMoney(discount);
        } else {
            discountRow.style.display = "none";
        }

        promoInfo.innerText = promoText;
        selectedPromotionId.value = selectedId || "";

        const total = subtotal - discount;
        summaryTotal.innerText = formatMoney(total);
    }

    promoSelect.addEventListener("change", updateTotal);
</script>

</body>
</html>
