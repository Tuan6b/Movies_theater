<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.cinema.model.BookingCart,com.cinema.model.BookingScheduleView,com.cinema.model.Food,com.cinema.model.Promotion" %>
<%@ page import="java.util.List" %>
<%
    BookingCart cart = (BookingCart) session.getAttribute("bookingCart");
    BookingScheduleView schedule = (BookingScheduleView) session.getAttribute("bookingSchedule");
    List<Food> foodList = (List<Food>) request.getAttribute("foodList");
    List<Promotion> promotions = (List<Promotion>) request.getAttribute("promotions");
    Integer suggestedPromotionId = (Integer) request.getAttribute("suggestedPromotionId");

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
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/booking-step-navigation.css">
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

        <section class="booking-step" aria-label="Các bước đặt vé">
            <a class="step step-link"
               href="<%= request.getContextPath() %>/booking?action=seat&amp;scheduleId=<%= schedule.getScheduleId() %>"
               title="Quay lại bước chọn ghế">
                <span>1</span>
                <p>Chọn ghế</p>
            </a>
            <div class="step-line"></div>
            <a class="step step-link"
               href="<%= request.getContextPath() %>/booking?action=food"
               title="Quay lại bước chọn đồ ăn">
                <span>2</span>
                <p>Chọn đồ ăn</p>
            </a>
            <div class="step-line"></div>
            <button type="button"
                    class="step step-button active"
                    aria-current="step"
                    title="Bạn đang ở bước thanh toán">
                <span>3</span>
                <p>Thanh toán</p>
            </button>
            <div class="step-line"></div>
            <button type="submit"
                    class="step step-button"
                    form="paymentForm"
                    title="Xác nhận thanh toán để nhận vé">
                <span>4</span>
                <p>Nhận vé</p>
            </button>
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
                    <h3 class="card-title">Chọn khuyến mãi</h3>
                    <select id="promotionSelect" class="promotion-select">
                        <option value="">-- Không sử dụng khuyến mãi --</option>
                        <% if (promotions != null) {
                            double subtotal = cart.getGrandTotal();
                            for (Promotion p : promotions) {
                                boolean suggested = suggestedPromotionId != null && suggestedPromotionId == p.getPromotionId();
                                String disabled = "";
                                String note = "";
                                if (p.getMinOrderAmount() != null && subtotal < p.getMinOrderAmount().doubleValue()) {
                                    disabled = "disabled";
                                    note = " (chưa đủ " + String.format("%,.0f", p.getMinOrderAmount()) + " đ)";
                                }
                                String discountLabel = p.getDiscountType().equals("Percentage")
                                    ? p.getDiscountValue().intValue() + "%"
                                    : String.format("%,.0f", p.getDiscountValue()) + "đ";
                                if (p.getMaxDiscountAmount() != null && "Percentage".equals(p.getDiscountType())) {
                                    discountLabel += " (tối đa " + String.format("%,.0f", p.getMaxDiscountAmount()) + "đ)";
                                }
                        %>
                            <option value="<%= p.getPromotionId() %>"
                                    data-type="<%= p.getDiscountType() %>"
                                    data-value="<%= p.getDiscountValue() %>"
                                    data-min-order="<%= p.getMinOrderAmount() != null ? p.getMinOrderAmount() : 0 %>"
                                    data-max-discount="<%= p.getMaxDiscountAmount() != null ? p.getMaxDiscountAmount() : "" %>"
                                    <%= disabled %>
                                    <%= suggested ? "class=\"suggested-promo\"" : "" %>>
                                <%= p.getPromotionCode() %> - Giảm <%= discountLabel %><%= note %>
                            </option>
                        <%  }
                        } %>
                    </select>
                    <div id="suggestionBadge" class="suggestion-badge" style="<%= suggestedPromotionId != null ? "" : "display:none;" %>">
                        Gợi ý: Mã này giảm nhiều nhất cho đơn hàng của bạn!
                    </div>
                    <div id="promotionInfo" class="promotion-info"></div>
                    <input type="hidden" id="selectedPromotionId" value="<%= cart.getAppliedPromotionId() != null ? cart.getAppliedPromotionId() : "" %>">
                    <input type="hidden" id="selectedPromotionCode" value="<%= cart.getAppliedPromotionCode() != null ? cart.getAppliedPromotionCode() : "" %>">
                </div>

                <div class="checkout-card">
                    <h3 class="card-title">Phương thức thanh toán</h3>
                    <div class="payment-method-list">
                        <label class="payment-method-option">
                            <input type="radio" name="paymentMethodRadio" value="VNPay" checked>
                            <span class="payment-method-label">
                                <strong>VNPay</strong>
                                <small>Thanh toán qua cổng VNPay (môi trường sandbox demo)</small>
                            </span>
                        </label>
                        <label class="payment-method-option">
                            <input type="radio" name="paymentMethodRadio" value="Card">
                            <span class="payment-method-label">
                                <strong>Thẻ ngân hàng</strong>
                                <small>Thanh toán bằng thẻ tại quầy / demo</small>
                            </span>
                        </label>
                        <label class="payment-method-option">
                            <input type="radio" name="paymentMethodRadio" value="Cash">
                            <span class="payment-method-label">
                                <strong>Tiền mặt</strong>
                                <small>Thanh toán tiền mặt tại quầy / demo</small>
                            </span>
                        </label>
                    </div>
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
                    <a href="<%= request.getContextPath() %>/booking?action=food" class="btn btn-back btn-block" style="margin-bottom:10px;">Quay lại</a>
                    <form action="<%= request.getContextPath() %>/booking" method="post" id="paymentForm">
                        <input type="hidden" name="action" value="confirmPayment">
                        <input type="hidden" name="promotionId" id="hiddenPromotionId" value="<%= cart.getAppliedPromotionId() != null ? cart.getAppliedPromotionId() : "" %>">

                        <button type="submit" class="btn btn-next btn-block">Thanh toán qua VNPAY (Thẻ nội địa)</button>

                        <input type="hidden" name="paymentMethod" id="hiddenPaymentMethod" value="VNPay">
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

    const promoSelect = document.getElementById("promotionSelect");
    const discountRow = document.getElementById("discountRow");
    const summaryDiscount = document.getElementById("summaryDiscount");
    const summaryTotal = document.getElementById("summaryTotal");
    const summarySubtotal = document.getElementById("summarySubtotal");
    const promoInfo = document.getElementById("promotionInfo");
    const selectedPromotionId = document.getElementById("selectedPromotionId");
    const selectedPromotionCode = document.getElementById("selectedPromotionCode");
    const hiddenPromotionId = document.getElementById("hiddenPromotionId");
    const discountCodeLabel = document.getElementById("discountCodeLabel");
    const suggestionBadge = document.getElementById("suggestionBadge");
    const hiddenPaymentMethod = document.getElementById("hiddenPaymentMethod");

    function updatePaymentMethodStyle() {
        document.querySelectorAll('.payment-method-option').forEach(function(opt) {
            opt.classList.remove('checked');
        });
        var checkedRadio = document.querySelector('input[name="paymentMethodRadio"]:checked');
        if (checkedRadio) {
            checkedRadio.closest('.payment-method-option').classList.add('checked');
        }
    }

    document.querySelectorAll('input[name="paymentMethodRadio"]').forEach(function(radio) {
        radio.addEventListener("change", function() {
            hiddenPaymentMethod.value = this.value;
            updatePaymentMethodStyle();
        });
    });

    updatePaymentMethodStyle();

    function formatMoney(value) {
        return new Intl.NumberFormat("vi-VN").format(value) + " đ";
    }

    function updateTotal() {
        const option = promoSelect.options[promoSelect.selectedIndex];
        let discount = 0;
        let promoText = "";

        if (option && option.value) {
            const type = option.dataset.type;
            const value = parseFloat(option.dataset.value);
            const minOrder = parseFloat(option.dataset.minOrder) || 0;
            const maxDiscount = option.dataset.maxDiscount ? parseFloat(option.dataset.maxDiscount) : null;

            if (subtotal >= minOrder) {
                if (type === "Percentage") {
                    discount = subtotal * value / 100;
                    if (maxDiscount && discount > maxDiscount) {
                        discount = maxDiscount;
                    }
                } else {
                    discount = value;
                }
                promoText = "Áp dụng mã " + option.text.split(" - ")[0] + ": giảm "
                    + (type === "Percentage" ? value + "%" : formatMoney(value));
                if (maxDiscount) {
                    promoText += " (tối đa " + formatMoney(maxDiscount) + ")";
                }

                selectedPromotionId.value = option.value;
                selectedPromotionCode.value = option.text.split(" - ")[0];
            } else {
                promoText = "Đơn hàng tối thiểu " + formatMoney(minOrder) + " để áp dụng mã này.";
                promoSelect.value = "";
                selectedPromotionId.value = "";
                selectedPromotionCode.value = "";
            }
        } else {
            selectedPromotionId.value = "";
            selectedPromotionCode.value = "";
        }

        if (discount > 0) {
            discountRow.style.display = "flex";
            summaryDiscount.innerText = "-" + formatMoney(discount);
            discountCodeLabel.innerText = selectedPromotionCode.value;
        } else {
            discountRow.style.display = "none";
        }

        promoInfo.innerText = promoText;
        hiddenPromotionId.value = selectedPromotionId.value;

        const total = subtotal - discount;
        summaryTotal.innerText = formatMoney(total);

        if (option && option.value && option.getAttribute("class") === "suggested-promo") {
            suggestionBadge.style.display = "block";
        } else {
            suggestionBadge.style.display = "none";
        }
    }

    promoSelect.addEventListener("change", updateTotal);

    // Auto-select the suggested promotion if none is already selected
    if (!selectedPromotionId.value) {
        const suggestedOption = Array.from(promoSelect.options).find(function(opt) {
            return opt.getAttribute("class") === "suggested-promo";
        });
        if (suggestedOption) {
            promoSelect.value = suggestedOption.value;
            updateTotal();
        }
    } else {
        // Restore previously selected promotion
        promoSelect.value = selectedPromotionId.value;
        updateTotal();
    }
</script>

</body>
</html>
