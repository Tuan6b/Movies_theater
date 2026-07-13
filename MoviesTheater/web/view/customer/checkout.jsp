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
                    <div class="promo-input-wrapper">
                        <input type="text"
                               id="promotionInput"
                               class="promotion-input"
                               placeholder="Nhập mã khuyến mãi..."
                               autocomplete="off"
                               value="<%= cart.getAppliedPromotionCode() != null ? cart.getAppliedPromotionCode() : "" %>">
                        <div id="promotionSuggestions" class="promo-suggestions"></div>
                    </div>
                    <div id="promotionInfo" class="promotion-info"></div>
                    <input type="hidden" id="selectedPromotionId" value="<%= cart.getAppliedPromotionId() != null ? cart.getAppliedPromotionId() : "" %>">
                    <input type="hidden" id="selectedPromotionCode" value="<%= cart.getAppliedPromotionCode() != null ? cart.getAppliedPromotionCode() : "" %>">
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
                    <div class="summary-row discount-row" id="discountRow" style="<%= cart.getDiscountAmount() > 0 ? "" : "display:none;" %>">
                        <span>Giảm giá (<span id="discountCodeLabel"><%= cart.getAppliedPromotionCode() != null ? cart.getAppliedPromotionCode() : "" %></span>)</span>
                        <span id="summaryDiscount">-<%= cart.getDiscountAmount() > 0 ? String.format("%,.0f", cart.getDiscountAmount()) : "0" %> đ</span>
                    </div>
                    <div class="summary-divider"></div>
                    <div class="summary-row summary-total">
                        <span>Tổng cộng</span>
                        <span id="summaryTotal"><%= String.format("%,.0f", cart.getFinalTotal() > 0 ? cart.getFinalTotal() : cart.getGrandTotal()) %> đ</span>
                    </div>
                    <form action="<%= request.getContextPath() %>/booking" method="post">
                        <input type="hidden" name="action" value="confirmPayment">
                        <input type="hidden" name="promotionId" id="hiddenPromotionId" value="<%= cart.getAppliedPromotionId() != null ? cart.getAppliedPromotionId() : "" %>">
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
    const subtotal = <%= cart.getGrandTotal() %>;
    const ticketTotalVal = <%= cart.getTicketTotal() %>;
    const foodTotalVal = <%= cart.getFoodTotal() %>;

    const promoInput = document.getElementById("promotionInput");
    const suggestionsContainer = document.getElementById("promotionSuggestions");
    const discountRow = document.getElementById("discountRow");
    const summaryDiscount = document.getElementById("summaryDiscount");
    const summaryTotal = document.getElementById("summaryTotal");
    const summarySubtotal = document.getElementById("summarySubtotal");
    const promoInfo = document.getElementById("promotionInfo");
    const selectedPromotionId = document.getElementById("selectedPromotionId");
    const selectedPromotionCode = document.getElementById("selectedPromotionCode");
    const hiddenPromotionId = document.getElementById("hiddenPromotionId");
    const discountCodeLabel = document.getElementById("discountCodeLabel");

    let selectedPromo = null;
    let searchTimeout = null;

    function formatMoney(value) {
        return new Intl.NumberFormat("vi-VN").format(value) + " đ";
    }

    function updateTotal() {
        let discount = 0;
        let promoText = "";

        if (selectedPromo) {
            if (subtotal >= selectedPromo.minOrder) {
                if (selectedPromo.type === "Percentage") {
                    discount = subtotal * selectedPromo.value / 100;
                    if (selectedPromo.maxDiscount && discount > selectedPromo.maxDiscount) {
                        discount = selectedPromo.maxDiscount;
                    }
                } else {
                    discount = selectedPromo.value;
                }
                promoText = "Áp dụng mã " + selectedPromo.code + ": giảm "
                    + (selectedPromo.type === "Percentage" ? selectedPromo.value + "%" : formatMoney(selectedPromo.value));
                if (selectedPromo.maxDiscount) {
                    promoText += " (tối đa " + formatMoney(selectedPromo.maxDiscount) + ")";
                }
            } else {
                promoText = "Đơn hàng tối thiểu " + formatMoney(selectedPromo.minOrder) + " để áp dụng mã này.";
                selectedPromo = null;
                promoInput.value = "";
                selectedPromotionId.value = "";
                selectedPromotionCode.value = "";
            }
        }

        if (discount > 0) {
            discountRow.style.display = "flex";
            summaryDiscount.innerText = "-" + formatMoney(discount);
            discountCodeLabel.innerText = selectedPromo.code;
        } else {
            discountRow.style.display = "none";
        }

        promoInfo.innerText = promoText;
        hiddenPromotionId.value = selectedPromotionId.value;

        const total = subtotal - discount;
        summaryTotal.innerText = formatMoney(total);
    }

    function fetchSuggestions(keyword) {
        if (!keyword || keyword.length < 1) {
            suggestionsContainer.style.display = "none";
            return;
        }

        fetch("<%= request.getContextPath() %>/booking?action=searchPromotions&q=" + encodeURIComponent(keyword))
            .then(function(resp) { return resp.json(); })
            .then(function(data) {
                suggestionsContainer.innerHTML = "";
                if (data.length === 0) {
                    suggestionsContainer.style.display = "none";
                    return;
                }
                data.forEach(function(promo) {
                    var div = document.createElement("div");
                    div.className = "promo-suggestion-item";

                    var discountText = promo.type === "Percentage"
                        ? promo.value + "%"
                        : formatMoney(promo.value);
                    if (promo.maxDiscount) {
                        discountText += " (tối đa " + formatMoney(promo.maxDiscount) + ")";
                    }

                    div.innerHTML = "<div class='promo-suggestion-code'>" + promo.code + "</div>"
                        + "<div class='promo-suggestion-detail'>Giảm " + discountText
                        + " &middot; Đơn tối thiểu " + formatMoney(promo.minOrder)
                        + " &middot; HSD: " + promo.endDate + "</div>";

                    div.addEventListener("click", function() {
                        promoInput.value = promo.code;
                        selectedPromotionId.value = promo.id;
                        selectedPromotionCode.value = promo.code;
                        selectedPromo = promo;
                        suggestionsContainer.style.display = "none";
                        updateTotal();
                    });

                    suggestionsContainer.appendChild(div);
                });
                suggestionsContainer.style.display = "block";
            })
            .catch(function() {
                suggestionsContainer.style.display = "none";
            });
    }

    promoInput.addEventListener("input", function() {
        var val = promoInput.value.trim();

        if (selectedPromo && selectedPromo.code !== val) {
            selectedPromo = null;
            selectedPromotionId.value = "";
            selectedPromotionCode.value = "";
            updateTotal();
        }

        if (searchTimeout) clearTimeout(searchTimeout);
        searchTimeout = setTimeout(function() { fetchSuggestions(val); }, 300);
    });

    promoInput.addEventListener("blur", function() {
        setTimeout(function() { suggestionsContainer.style.display = "none"; }, 200);
    });

    promoInput.addEventListener("focus", function() {
        var val = promoInput.value.trim();
        if (val.length > 0) {
            fetchSuggestions(val);
        }
    });

    // Init with any pre-selected promo from session
    if (selectedPromotionId.value) {
        // Rebuild selectedPromo from session data for display
        fetch("<%= request.getContextPath() %>/booking?action=searchPromotions&q=" + encodeURIComponent(selectedPromotionCode.value))
            .then(function(resp) { return resp.json(); })
            .then(function(data) {
                if (data.length > 0) {
                    selectedPromo = data[0];
                    updateTotal();
                }
            });
    }
</script>

</body>
</html>
