/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.cinema.model.BookingScheduleView,com.cinema.model.SeatView" %>
<%@ page import="java.util.List" %>
<%
    BookingScheduleView schedule = (BookingScheduleView) request.getAttribute("schedule");
    List<SeatView> seats = (List<SeatView>) request.getAttribute("seats");

    // Prevent direct JSP access without controller attributes
    if (schedule == null || seats == null) {
        response.sendRedirect(request.getContextPath() + "/showtimes");
        return;
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>CGV CINEMA - Chọn ghế</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/seat-selection.css">
</head>

<body>

<div class="seat-page">

    <div class="top-dark">
        <div class="header-inner">
            <a href="<%= request.getContextPath() %>/" class="logo">CGV CINEMA</a>

            <div class="nav">
                <a href="<%= request.getContextPath() %>/index.jsp">Trang chủ</a>
                <a href="#">Ưu đãi</a>
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
                <strong><%= schedule.getMovieName() %></strong>
            </div>

            <div>
                <span>Suất chiếu</span>
                <strong><%= schedule.getStartTime() %> - <%= schedule.getShowDate() %></strong>
            </div>

            <div>
                <span>Phòng</span>
                <strong><%= schedule.getRoomNumber() %> (<%= schedule.getRoomType() %>)</strong>
            </div>

            <div>
                <span>Giá vé</span>
                <strong><%= String.format("%,.0f", schedule.getBaseTicketPrice()) %> đ</strong>
            </div>
        </section>

        <form action="<%= request.getContextPath() %>/booking" method="post" id="seatForm">

            <input type="hidden" name="action" value="selectSeat">
            <input type="hidden" name="scheduleId" value="<%= schedule.getScheduleId() %>">

            <section class="seat-map-card">

                <div class="screen-wrapper">
                    <div class="screen-light"></div>
                    <div class="screen">MÀN HÌNH</div>
                    <div class="screen-text">SCREEN</div>
                </div>

                <div class="seat-map">
                    <%
                        String currentRow = "";
                        boolean firstRow = true;
                        for (SeatView seat : seats) {
                            String seatRow = seat.getRowChar();
                            if (!seatRow.equals(currentRow)) {
                                if (!firstRow) {
                                    // Close previous row's seat list and row label container
                                    out.println("</div></div>");
                                }
                                firstRow = false;
                                currentRow = seatRow;
                                // Start new row
                                out.println("<div class='seat-row'>");
                                out.println("<div class='row-label'>" + currentRow + "</div>");
                                out.println("<div class='seat-list'>");
                            }

                            String seatName = seat.getSeatName();
                            String seatClass = seat.isBooked() ? "booked" : (seat.getSeatType().equalsIgnoreCase("VIP") ? "vip" : "normal");
                    %>
                            <label class="seat <%= seatClass %>"
                                   data-seat-name="<%= seatName %>"
                                   data-price="<%= schedule.getBaseTicketPrice() %>">

                                <input type="checkbox"
                                       name="seatIds"
                                       value="<%= seat.getSeatId() %>"
                                       data-seat-name="<%= seatName %>"
                                       data-price="<%= schedule.getBaseTicketPrice() %>"
                                       <%= seat.isBooked() ? "disabled" : "" %>>

                                <span><%= seat.getColNumber() %></span>
                            </label>
                    <%
                        }
                        if (!firstRow) {
                            out.println("</div></div>");
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