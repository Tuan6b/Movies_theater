<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.cinema.model.BookingCart,com.cinema.model.BookingScheduleView,com.cinema.model.Food" %>
<%@ page import="java.util.List" %>
<%
    BookingCart cart = (BookingCart) session.getAttribute("bookingCart");
    BookingScheduleView schedule = (BookingScheduleView) session.getAttribute("bookingSchedule");
    List<Food> comboList = (List<Food>) request.getAttribute("comboList");
    List<Food> itemList = (List<Food>) request.getAttribute("itemList");

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

        <form action="<%= request.getContextPath() %>/booking" method="post" id="foodForm">
            
            <input type="hidden" name="action" value="selectFood">

            <section class="food-grid">
                
                <% if (comboList != null && !comboList.isEmpty()) { %>
                    <div class="food-section-label">COMBO</div>
                    <% for (Food food : comboList) {
                        boolean hasImage = food.getImage() != null && !food.getImage().isEmpty();
                    %>
                    <div class="food-card">
                        <div class="food-image">
                            <% if (hasImage) { %>
                                <img src="<%= food.getImage() %>" alt="<%= food.getFoodName() %>">
                            <% } else { %>
                                <div class="food-image-placeholder">
                                    <span><%= food.getFoodName().substring(0, Math.min(2, food.getFoodName().length())) %></span>
                                </div>
                            <% } %>
                        </div>
                        <div class="food-info">
                            <div>
                                <h3 class="food-name"><%= food.getFoodName() %></h3>
                            </div>
                            <div class="food-purchase">
                                <span class="food-price"><%= String.format("%,.0f", food.getPrice()) %> đ</span>
                                <div class="quantity-control">
                                    <button type="button" class="qty-btn dec-btn" data-food-id="<%= food.getFoodId() %>">-</button>
                                    <input type="text" class="qty-val" name="qty_<%= food.getFoodId() %>" id="qty_<%= food.getFoodId() %>" value="0" data-price="<%= (int) food.getPrice() %>">
                                    <button type="button" class="qty-btn inc-btn" data-food-id="<%= food.getFoodId() %>">+</button>
                                </div>
                            </div>
                        </div>
                    </div>
                <%  }
                } %>

                <% if (itemList != null && !itemList.isEmpty()) { %>
                    <div class="food-section-label">ĐỒ ĂN LẺ</div>
                    <% for (Food food : itemList) {
                        boolean hasImage = food.getImage() != null && !food.getImage().isEmpty();
                    %>
                    <div class="food-card">
                        <div class="food-image">
                            <% if (hasImage) { %>
                                <img src="<%= food.getImage() %>" alt="<%= food.getFoodName() %>">
                            <% } else { %>
                                <div class="food-image-placeholder">
                                    <span><%= food.getFoodName().substring(0, Math.min(2, food.getFoodName().length())) %></span>
                                </div>
                            <% } %>
                        </div>
                        <div class="food-info">
                            <div>
                                <h3 class="food-name"><%= food.getFoodName() %></h3>
                            </div>
                            <div class="food-purchase">
                                <span class="food-price"><%= String.format("%,.0f", food.getPrice()) %> đ</span>
                                <div class="quantity-control">
                                    <button type="button" class="qty-btn dec-btn" data-food-id="<%= food.getFoodId() %>">-</button>
                                    <input type="text" class="qty-val" name="qty_<%= food.getFoodId() %>" id="qty_<%= food.getFoodId() %>" value="0" data-price="<%= (int) food.getPrice() %>">
                                    <button type="button" class="qty-btn inc-btn" data-food-id="<%= food.getFoodId() %>">+</button>
                                </div>
                            </div>
                        </div>
                    </div>
                <%  }
                } %>

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
            let id = button.dataset.foodId;
            let input = document.getElementById("qty_" + id);
            let val = parseInt(input.value);
            input.value = val + 1;
            calculateTotal();
        });
    });

    document.querySelectorAll(".dec-btn").forEach(function(button) {
        button.addEventListener("click", function() {
            let id = button.dataset.foodId;
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
