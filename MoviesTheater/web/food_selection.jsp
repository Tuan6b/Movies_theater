<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.cinema.model.BookingCart,com.cinema.model.BookingScheduleView" %>
<%
    BookingCart cart = (BookingCart) session.getAttribute("bookingCart");
    BookingScheduleView schedule = (BookingScheduleView) session.getAttribute("bookingSchedule");

    if (cart == null || schedule == null) {
        response.sendRedirect(request.getContextPath() + "/showtimes");
        return;
    }

    // Convert seat names list to comma separated string for displaying
    StringBuilder seatNamesStr = new StringBuilder();
    if (cart.getSeatNames() != null) {
        for (int i = 0; i < cart.getSeatNames().size(); i++) {
            seatNamesStr.append(cart.getSeatNames().get(i));
            if (i < cart.getSeatNames().size() - 1) {
                seatNamesStr.append(", ");
            }
        }
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>CGV CINEMA - Chọn bắp nước</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/food-selection.css">
</head>
<body>

<div class="food-page">

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

    <main class="food-container">

        <section class="booking-step">
            <div class="step">
                <span>1</span>
                <p>Chọn ghế</p>
            </div>

            <div class="step-line"></div>

            <div class="step active">
                <span>2</span>
                <p>Chọn đồ ăn</p>
            </div>

            <div class="step-line"></div>

            <div class="step">
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
            <div class="title-icon">🍿</div>
            <h1>BẮP NƯỚC & COMBOS</h1>
        </section>

        <form action="<%= request.getContextPath() %>/checkout" method="post" id="foodForm">
            
            <input type="hidden" name="action" value="selectFood">

            <section class="food-grid">
                
                <!-- Combo 1 -->
                <div class="food-card">
                    <div class="food-image">
                        <img src="<%= request.getContextPath() %>/Image/my_combo.png" alt="My Combo" onerror="this.src='https://img.vietnamfinance.vn/upload/news/hoangnam/2018/1/3/cgv-1.jpg'">
                    </div>
                    <div class="food-info">
                        <div>
                            <h3 class="food-name">My Combo</h3>
                            <p class="food-desc">1 Nước ngọt cỡ vừa (Medium Soda) + 1 Bắp ngọt cỡ vừa (Medium Popcorn)</p>
                        </div>
                        <div class="food-purchase">
                            <span class="food-price">85,000 đ</span>
                            <div class="quantity-control">
                                <button type="button" class="qty-btn dec-btn" data-id="my_combo">-</button>
                                <input type="text" class="qty-val" name="qty_my_combo" id="qty_my_combo" value="0" data-price="85000">
                                <button type="button" class="qty-btn inc-btn" data-id="my_combo">+</button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Combo 2 -->
                <div class="food-card">
                    <div class="food-image">
                        <img src="<%= request.getContextPath() %>/Image/cgv_combo.png" alt="CGV Combo" onerror="this.src='https://img.vietnamfinance.vn/upload/news/hoangnam/2018/1/3/cgv-1.jpg'">
                    </div>
                    <div class="food-info">
                        <div>
                            <h3 class="food-name">CGV Combo</h3>
                            <p class="food-desc">2 Nước ngọt cỡ vừa (Medium Soda) + 1 Bắp ngọt cỡ lớn (Large Popcorn)</p>
                        </div>
                        <div class="food-purchase">
                            <span class="food-price">115,000 đ</span>
                            <div class="quantity-control">
                                <button type="button" class="qty-btn dec-btn" data-id="cgv_combo">-</button>
                                <input type="text" class="qty-val" name="qty_cgv_combo" id="qty_cgv_combo" value="0" data-price="115000">
                                <button type="button" class="qty-btn inc-btn" data-id="cgv_combo">+</button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Combo 3 -->
                <div class="food-card">
                    <div class="food-image">
                        <img src="<%= request.getContextPath() %>/Image/twin_combo.png" alt="Twin Combo" onerror="this.src='https://img.vietnamfinance.vn/upload/news/hoangnam/2018/1/3/cgv-1.jpg'">
                    </div>
                    <div class="food-info">
                        <div>
                            <h3 class="food-name">Twin Combo</h3>
                            <p class="food-desc">2 Nước ngọt cỡ vừa (Medium Soda) + 2 Bắp ngọt cỡ lớn (Large Popcorn)</p>
                        </div>
                        <div class="food-purchase">
                            <span class="food-price">165,000 đ</span>
                            <div class="quantity-control">
                                <button type="button" class="qty-btn dec-btn" data-id="twin_combo">-</button>
                                <input type="text" class="qty-val" name="qty_twin_combo" id="qty_twin_combo" value="0" data-price="165000">
                                <button type="button" class="qty-btn inc-btn" data-id="twin_combo">+</button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Combo 4 -->
                <div class="food-card">
                    <div class="food-image">
                        <img src="<%= request.getContextPath() %>/Image/family_combo.png" alt="Family Combo" onerror="this.src='https://img.vietnamfinance.vn/upload/news/hoangnam/2018/1/3/cgv-1.jpg'">
                    </div>
                    <div class="food-info">
                        <div>
                            <h3 class="food-name">Family Combo</h3>
                            <p class="food-desc">4 Nước ngọt cỡ vừa (Medium Soda) + 2 Bắp ngọt cỡ lớn + 2 Snack khoai tây</p>
                        </div>
                        <div class="food-purchase">
                            <span class="food-price">245,000 đ</span>
                            <div class="quantity-control">
                                <button type="button" class="qty-btn dec-btn" data-id="family_combo">-</button>
                                <input type="text" class="qty-val" name="qty_family_combo" id="qty_family_combo" value="0" data-price="245000">
                                <button type="button" class="qty-btn inc-btn" data-id="family_combo">+</button>
                            </div>
                        </div>
                    </div>
                </div>

            </section>

            <section class="checkout-bar">

                <div class="selected-info">
                    <p>Vé & Bắp nước đã chọn</p>
                    <strong>
                        Phim: <%= schedule.getMovieName() %><br>
                        Ghế: <%= seatNamesStr.toString() %> (<%= cart.getSeatIds().size() %> vé)<br>
                        Bắp nước: <span id="foodSummaryText">Chưa chọn bắp nước</span>
                    </strong>
                </div>

                <div class="price-info">
                    <p>Tổng cộng</p>
                    <strong id="totalPriceText"><%= String.format("%,.0f", cart.getTicketTotal()) %> đ</strong>
                </div>

                <div class="action-group">
                    <a href="javascript:history.back()" class="btn btn-back">
                        Quay lại
                    </a>

                    <button type="submit" class="btn btn-next">
                        Thanh toán
                    </button>
                </div>

            </section>

        </form>

    </main>

    <footer class="footer-dark">
        <div class="footer-inner">
            <strong>CGV CINEMA</strong>
            <span>© 2026 CGV Cinema. Hệ thống quản lý rạp chiếu phim</span>
        </div>
    </footer>

</div>

<script>
    const ticketTotal = <%= cart.getTicketTotal() %>;
    const foodSummaryText = document.getElementById("foodSummaryText");
    const totalPriceText = document.getElementById("totalPriceText");

    function formatMoney(value) {
        return new Intl.NumberFormat("vi-VN").format(value) + " đ";
    }

    function calculateTotal() {
        let total = ticketTotal;
        let selectedCombos = [];

        document.querySelectorAll(".qty-val").forEach(function(input) {
            let qty = parseInt(input.value);
            if (qty > 0) {
                let price = parseInt(input.dataset.price);
                total += (qty * price);
                
                // Get combo name
                let card = input.closest(".food-card");
                let name = card.querySelector(".food-name").innerText;
                selectedCombos.push(qty + " x " + name);
            }
        });

        if (selectedCombos.length === 0) {
            foodSummaryText.innerText = "Chưa chọn bắp nước";
        } else {
            foodSummaryText.innerText = selectedCombos.join(", ");
        }

        totalPriceText.innerText = formatMoney(total);
    }

    // Inc / Dec click handlers
    document.querySelectorAll(".inc-btn").forEach(function(button) {
        button.addEventListener("click", function() {
            let id = button.dataset.id;
            let input = document.getElementById("qty_" + id);
            let val = parseInt(input.value);
            input.value = val + 1;
            calculateTotal();
        });
    });

    document.querySelectorAll(".dec-btn").forEach(function(button) {
        button.addEventListener("click", function() {
            let id = button.dataset.id;
            let input = document.getElementById("qty_" + id);
            let val = parseInt(input.value);
            if (val > 0) {
                input.value = val - 1;
                calculateTotal();
            }
        });
    });
</script>

</body>
</html>
