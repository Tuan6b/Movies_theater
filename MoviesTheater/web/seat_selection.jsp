<%-- 
    Document   : seat_selection
    Created on : Jun 11, 2026, 3:00:35 PM
    Author     : ADMIN
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    String scheduleId = request.getParameter("scheduleId");

    if (scheduleId == null || scheduleId.trim().isEmpty()) {
        scheduleId = "1";
    }

    double ticketPrice = 90000;
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>CGV - Select Seat</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/seat-selection.css">
</head>

<body>

<div class="seat-page">

    <div class="top-dark">
        <div class="header-inner">
            <div class="logo">CGV CINEMA</div>

            <div class="nav">
                <a href="<%= request.getContextPath() %>/index.jsp">Trang chủ</a>
                <a href="<%= request.getContextPath() %>/showtime.jsp">Lịch chiếu</a>
                <a href="#">Vé của tôi</a>
            </div>
        </div>
    </div>

    <main class="seat-container">

        <section class="booking-step">
            <div class="step active">
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

            <div class="step">
                <span>4</span>
                <p>Nhận vé</p>
            </div>
        </section>

        <section class="section-title">
            <div class="title-icon">▣</div>
            <h1>CHỌN GHẾ</h1>
        </section>

        <section class="movie-info-card">
            <div>
                <span>Phim</span>
                <strong>CGV Movie Demo</strong>
            </div>

            <div>
                <span>Suất chiếu</span>
                <strong>09:00 - 01/07</strong>
            </div>

            <div>
                <span>Phòng</span>
                <strong>Phòng P01</strong>
            </div>

            <div>
                <span>Giá vé</span>
                <strong><%= String.format("%,.0f", ticketPrice) %> đ</strong>
            </div>
        </section>

        <form action="<%= request.getContextPath() %>/booking" method="post" id="seatForm">

            <input type="hidden" name="action" value="selectSeat">
            <input type="hidden" name="scheduleId" value="<%= scheduleId %>">

            <section class="seat-map-card">

                <div class="screen-wrapper">
                    <div class="screen-light"></div>
                    <div class="screen">MÀN HÌNH</div>
                </div>

                <div class="seat-map">

                    <%
                        String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H"};
                        int seatId = 1;

                        for (String row : rows) {
                    %>

                    <div class="seat-row">
                        <div class="row-label"><%= row %></div>

                        <div class="seat-list">
                            <%
                                for (int col = 1; col <= 10; col++) {
                                    String seatName = row + col;

                                    boolean isVip = row.equals("E") || row.equals("F") || row.equals("G") || row.equals("H");

                                    boolean isBooked =
                                            seatName.equals("A4") ||
                                            seatName.equals("A5") ||
                                            seatName.equals("C6") ||
                                            seatName.equals("D7") ||
                                            seatName.equals("F3") ||
                                            seatName.equals("F4");

                                    String seatClass = isBooked ? "booked" : (isVip ? "vip" : "normal");
                            %>

                            <label class="seat <%= seatClass %>"
                                   data-seat-name="<%= seatName %>"
                                   data-price="<%= ticketPrice %>">

                                <input type="checkbox"
                                       name="seatIds"
                                       value="<%= seatId %>"
                                       data-seat-name="<%= seatName %>"
                                       data-price="<%= ticketPrice %>"
                                       <%= isBooked ? "disabled" : "" %>>

                                <span><%= seatName %></span>
                            </label>

                            <%
                                    seatId++;
                                }
                            %>
                        </div>
                    </div>

                    <%
                        }
                    %>

                </div>

                <div class="seat-note">
                    <div><span class="note-box normal"></span> Ghế thường</div>
                    <div><span class="note-box vip"></span> Ghế VIP</div>
                    <div><span class="note-box selected"></span> Ghế đang chọn</div>
                    <div><span class="note-box booked"></span> Ghế đã đặt</div>
                </div>

            </section>

            <section class="checkout-bar">

                <div class="selected-info">
                    <p>Ghế đã chọn</p>
                    <strong id="selectedSeatsText">Chưa chọn ghế</strong>
                </div>

                <div class="price-info">
                    <p>Tạm tính</p>
                    <strong id="totalPriceText">0 đ</strong>
                </div>

                <div class="action-group">
                    <a href="javascript:history.back()" class="btn btn-back">
                        Quay lại
                    </a>

                    <button type="submit" class="btn btn-next">
                        Tiếp tục
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
    const seatForm = document.getElementById("seatForm");
    const checkboxes = document.querySelectorAll("input[name='seatIds']");
    const selectedSeatsText = document.getElementById("selectedSeatsText");
    const totalPriceText = document.getElementById("totalPriceText");

    function formatMoney(value) {
        return new Intl.NumberFormat("vi-VN").format(value) + " đ";
    }

    function updateSelectedSeats() {
        let selectedSeats = [];
        let total = 0;

        checkboxes.forEach(function (checkbox) {
            if (checkbox.checked) {
                selectedSeats.push(checkbox.dataset.seatName);
                total += Number(checkbox.dataset.price);
            }
        });

        if (selectedSeats.length === 0) {
            selectedSeatsText.innerText = "Chưa chọn ghế";
        } else {
            selectedSeatsText.innerText = selectedSeats.join(", ");
        }

        totalPriceText.innerText = formatMoney(total);
    }

    checkboxes.forEach(function (checkbox) {
        checkbox.addEventListener("change", updateSelectedSeats);
    });

    seatForm.addEventListener("submit", function (event) {
        let hasSelected = false;

        checkboxes.forEach(function (checkbox) {
            if (checkbox.checked) {
                hasSelected = true;
            }
        });

        if (!hasSelected) {
            event.preventDefault();
            alert("Vui lòng chọn ít nhất một ghế.");
        }
    });
</script>

</body>
</html>