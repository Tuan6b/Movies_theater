<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.cinema.model.BookingScheduleView" %>
<%@ page import="com.cinema.model.BookingCart" %>
<%@ page import="com.cinema.model.SeatView" %>
<%@ page import="java.util.List" %>

<%
    BookingScheduleView schedule =
            (BookingScheduleView) request.getAttribute("schedule");

    List<SeatView> seats =
            (List<SeatView>) request.getAttribute("seats");

    if (schedule == null || seats == null) {
        response.sendRedirect(request.getContextPath() + "/showtimes");
        return;
    }

    BookingCart currentCart = (BookingCart) session.getAttribute("bookingCart");
    boolean sameScheduleCart = currentCart != null
            && currentCart.getScheduleId() == schedule.getScheduleId()
            && currentCart.getSeatIds() != null;

    int totalSeats = seats.size();
    int bookedSeats = 0;

    for (SeatView item : seats) {
        if (item.isBooked()) {
            bookedSeats++;
        }
    }

    int availableSeats = totalSeats - bookedSeats;

    String movieName = schedule.getMovieName();

    if (movieName == null || movieName.trim().isEmpty()) {
        movieName = "Đang cập nhật";
    }

    String posterUrl = schedule.getImageUrl();

    if (posterUrl == null || posterUrl.trim().isEmpty()) {
        posterUrl = request.getContextPath() + "/images/no-poster.jpg";
    }

    // Format date from YYYY-MM-DD to DD/MM/YYYY
    String showDateRaw = schedule.getShowDate();
    String formattedDate = showDateRaw;
    if (showDateRaw != null && showDateRaw.length() == 10) {
        String[] parts = showDateRaw.split("-");
        formattedDate = parts[2] + "/" + parts[1] + "/" + parts[0];
    }

    // Estimate End Time by adding 115 minutes (1h55m) to Start Time (HH:MM)
    String startTime = schedule.getStartTime();
    String endHourMin = "";
    if (startTime != null && startTime.contains(":")) {
        try {
            String[] timeParts = startTime.split(":");
            int hours = Integer.parseInt(timeParts[0].trim());
            int minutes = Integer.parseInt(timeParts[1].trim());
            int totalMinutes = hours * 60 + minutes + 115;
            int endHours = (totalMinutes / 60) % 24;
            int endMins = totalMinutes % 60;
            endHourMin = String.format("%02d:%02d", endHours, endMins);
        } catch (Exception e) {
            endHourMin = startTime;
        }
    }

    // Group and sort seats descending by column number
    java.util.Map<String, java.util.List<com.cinema.model.SeatView>> seatsByRow = new java.util.LinkedHashMap<>();
    int maxCol = 0;
    for (com.cinema.model.SeatView seat : seats) {
        seatsByRow.computeIfAbsent(seat.getRowChar(), k -> new java.util.ArrayList<>()).add(seat);
        if (seat.getColNumber() > maxCol) {
            maxCol = seat.getColNumber();
        }
    }
    for (java.util.List<com.cinema.model.SeatView> rowSeats : seatsByRow.values()) {
        rowSeats.sort((s1, s2) -> Integer.compare(s2.getColNumber(), s1.getColNumber()));
    }
%>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>CGV CINEMA - Chọn ghế</title>

    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/css/seat-selection.css">
    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/css/booking-step-navigation.css">
</head>

<body>

<div class="seat-page">

    <header class="top-dark">

        <div class="header-inner">

            <a href="<%= request.getContextPath() %>/"
               class="logo">
                CGV CINEMA
            </a>

            <nav class="nav">

                <a href="<%= request.getContextPath() %>/index.jsp">
                    Trang chủ
                </a>

                <a href="#">
                    Ưu đãi
                </a>

            </nav>

        </div>

    </header>

    <main class="seat-container">

        <section class="booking-step" aria-label="Các bước đặt vé">

            <button type="button"
                    class="step step-button active"
                    aria-current="step"
                    title="Bạn đang ở bước chọn ghế">
                <span>1</span>
                <p>Chọn ghế</p>
            </button>

            <div class="step-line"></div>

            <button type="submit"
                    class="step step-button"
                    form="seatForm"
                    name="targetStep"
                    value="food"
                    title="Lưu ghế đã chọn và chuyển sang chọn đồ ăn">
                <span>2</span>
                <p>Chọn đồ ăn</p>
            </button>

            <div class="step-line"></div>

            <button type="submit"
                    class="step step-button"
                    form="seatForm"
                    name="targetStep"
                    value="checkout"
                    title="Lưu ghế đã chọn và chuyển sang thanh toán">
                <span>3</span>
                <p>Thanh toán</p>
            </button>

            <div class="step-line"></div>

            <div class="step step-disabled"
                 aria-disabled="true"
                 title="Bạn cần hoàn tất thanh toán trước khi nhận vé">
                <span>4</span>
                <p>Nhận vé</p>
            </div>

        </section>

        <!-- CGV style header and info panel -->
        <div class="cgv-booking-header">
            BOOKING ONLINE
        </div>

        <div class="cgv-info-box">
            <div class="cgv-cinema-room">
                CGV CINEMA | <%= schedule.getRoomNumber() %> | Số ghế (<%= availableSeats %>/<%= totalSeats %>)
            </div>
            <div class="cgv-datetime">
                <%= formattedDate %> <%= schedule.getStartTime() %> ~ <%= formattedDate %> <%= endHourMin %>
            </div>
        </div>

        <div class="cgv-grey-bar">
            1 Người/Ghế
        </div>

        <form action="<%= request.getContextPath() %>/booking"
              method="post"
              id="seatForm">

            <input type="hidden"
                   name="action"
                   value="selectSeat">

            <input type="hidden"
                   name="scheduleId"
                   value="<%= schedule.getScheduleId() %>">

            <section class="seat-map-card">

                <div class="screen-wrapper">
                    <div class="screen-curve"></div>
                    <div class="screen-text">SCREEN</div>
                </div>

                <div class="seat-map">

                    <%
                        for (java.util.Map.Entry<String, java.util.List<com.cinema.model.SeatView>> entry : seatsByRow.entrySet()) {
                            String rowChar = entry.getKey();
                            java.util.List<com.cinema.model.SeatView> rowSeats = entry.getValue();

                            java.util.Map<Integer, com.cinema.model.SeatView> seatMapByCol = new java.util.HashMap<>();
                            for (com.cinema.model.SeatView s : rowSeats) {
                                seatMapByCol.put(s.getColNumber(), s);
                            }
                    %>
                    <div class="seat-row">
                        <div class="seat-list">
                            <%
                                for (int col = maxCol; col >= 1; col--) {
                                    com.cinema.model.SeatView seat = seatMapByCol.get(col);
                                    if (seat != null) {
                                        String seatName = seat.getSeatName();
                                        String seatType = seat.getSeatType() == null ? "NORMAL" : seat.getSeatType();
                                        boolean isVip = seatType.equalsIgnoreCase("VIP");
                                        boolean isCouple = seatType.equalsIgnoreCase("COUPLE") || seatType.equalsIgnoreCase("SWEETBOX");

                                        String seatClass = "";
                                        if (seat.isBooked()) {
                                            seatClass = "booked";
                                        } else if (isCouple) {
                                            seatClass = "couple";
                                        } else if (isVip) {
                                            seatClass = "vip";
                                        } else {
                                            seatClass = "normal";
                                        }

                                        double seatPrice = isVip ? schedule.getBaseTicketPrice() + 10000 : schedule.getBaseTicketPrice();
                                        if (isCouple) {
                                            seatPrice = schedule.getBaseTicketPrice() * 2;
                                        }
                            %>
                            <label class="seat <%= seatClass %>"
                                   data-seat-name="<%= seatName %>"
                                   data-price="<%= (int) seatPrice %>"
                                   data-seat-type="<%= seatType %>"
                                   title="<%= seat.isBooked() ? "Ghế " + seatName + " đã được đặt" : "Chọn ghế " + seatName %>">

                                <input type="checkbox"
                                       name="seatIds"
                                       value="<%= seat.getSeatId() %>"
                                       data-seat-name="<%= seatName %>"
                                       data-price="<%= (int) seatPrice %>"
                                       data-seat-type="<%= seatType %>"
                                       <%= sameScheduleCart && currentCart.getSeatIds().contains(seat.getSeatId()) && !seat.isBooked() ? "checked" : "" %>
                                       <%= seat.isBooked() ? "disabled" : "" %>>

                                <span><%= seatName %></span>
                            </label>
                            <%
                                    } else {
                            %>
                            <div class="seat-placeholder"></div>
                            <%
                                    }

                                    // Walkway gap after column 9 (except for Row L)
                                    if (col == 9 && !"L".equalsIgnoreCase(rowChar)) {
                            %>
                            <div class="seat-walkway"></div>
                            <%
                                    }
                                }
                            %>
                        </div>
                    </div>
                    <%
                        }
                    %>

                </div>

                <div class="seat-note">

                    <div>
                        <span class="note-box normal"></span>
                        Ghế thường
                    </div>

                    <div>
                        <span class="note-box vip"></span>
                        Ghế VIP
                    </div>

                    <div>
                        <span class="note-box couple"></span>
                        Sweetbox (Couple)
                    </div>

                    <div>
                        <span class="note-box selected"></span>
                        Ghế đang chọn
                    </div>

                    <div>
                        <span class="note-box booked"></span>
                        Ghế đã đặt
                    </div>

                </div>

            </section>

            <section class="booking-detail-card">

                <div class="movie-mini-card">

                    <div class="movie-poster-frame">

                        <img class="movie-poster"
                             src="<%= posterUrl %>"
                             alt="Poster phim <%= movieName %>"
                             onerror="this.onerror=null; this.src='<%= request.getContextPath() %>/images/no-poster.jpg';">

                    </div>

                </div>

                <div class="booking-detail-column">

                    <div class="detail-label">
                        Tên phim
                    </div>

                    <div class="detail-value movie-name">
                        <%= movieName %>
                    </div>

                    <div class="detail-label">
                        Rạp
                    </div>

                    <div class="detail-value">
                        CGV CINEMA
                    </div>

                    <div class="detail-label">
                        Định dạng
                    </div>

                    <div class="detail-value">
                        <%= schedule.getRoomType() %>
                    </div>

                </div>

                <div class="booking-detail-column">

                    <div class="detail-label">
                        Suất chiếu
                    </div>

                    <div class="detail-value">
                        <%= schedule.getStartTime() %>
                        _
                        <%= schedule.getShowDate() %>
                    </div>

                    <div class="detail-label">
                        Phòng chiếu
                    </div>

                    <div class="detail-value">
                        <%= schedule.getRoomNumber() %>
                    </div>

                    <div class="detail-label">
                        Ghế
                    </div>

                    <div class="detail-value selected-seat-value"
                         id="detailSelectedSeats">
                        Chưa chọn ghế
                    </div>

                    <div id="seatDetailsList" class="seat-details-list"></div>

                </div>

                <div class="booking-detail-column payment-column">

                    <div class="money-row">

                        <span>Tiền vé</span>

                        <strong id="detailTicketPrice">
                            0 đ
                        </strong>

                    </div>

                    <div class="money-row">

                        <span>Combo</span>

                        <strong>
                            0 đ
                        </strong>

                    </div>

                    <div class="money-row">

                        <span>Khuyến mãi</span>

                        <strong>
                            0 đ
                        </strong>

                    </div>

                    <div class="money-row total">

                        <span>Tổng cộng</span>

                        <strong id="detailTotalPrice">
                            0 đ
                        </strong>

                    </div>

                </div>

            </section>

            <section class="checkout-bar">

                <div class="selected-info">

                    <p>Đã chọn</p>

                    <strong id="selectedSeatsText">
                        Chưa chọn ghế
                    </strong>

                    <span id="selectedCount"
                          class="seat-count-badge">
                        0
                        <p>Ghế</p> 
                    </span>
                    

                </div>

                <div class="price-info">

                    <p>Tạm tính</p>

                    <strong id="totalPriceText">
                        0 đ
                    </strong>

                </div>

                <div class="action-group">

                    <a href="javascript:history.back()"
                       class="btn btn-back">
                        Quay lại
                    </a>

                    <button type="submit"
                            class="btn btn-next"
                            id="nextButton"
                            disabled>
                        Tiếp tục
                    </button>

                </div>

            </section>

        </form>

    </main>

    <footer class="footer-dark">

        <div class="footer-inner">

            <strong>
                CGV CINEMA
            </strong>

            <span>
                © 2026 CGV Cinema.
                Hệ thống quản lý rạp chiếu phim
            </span>

        </div>

    </footer>

</div>

<script>
    const seatForm =
        document.getElementById("seatForm");

    const checkboxes =
        document.querySelectorAll(
            "input[name='seatIds']"
        );

    const selectedSeatsText =
        document.getElementById(
            "selectedSeatsText"
        );

    const selectedCount =
        document.getElementById(
            "selectedCount"
        );

    const totalPriceText =
        document.getElementById(
            "totalPriceText"
        );

    const detailSelectedSeats =
        document.getElementById(
            "detailSelectedSeats"
        );

    const detailTicketPrice =
        document.getElementById(
            "detailTicketPrice"
        );

    const detailTotalPrice =
        document.getElementById(
            "detailTotalPrice"
        );

    const seatDetailsList =
        document.getElementById(
            "seatDetailsList"
        );

    const nextButton =
        document.getElementById(
            "nextButton"
        );

    const MAX_SEATS = 8;

    function formatMoney(value) {
        return new Intl.NumberFormat(
            "vi-VN"
        ).format(value) + " đ";
    }

    function updateSelectedSeats() {

        const selectedSeats = [];
        let total = 0;

        checkboxes.forEach(function (checkbox) {

            if (checkbox.checked) {

                selectedSeats.push(
                    checkbox.dataset.seatName
                );

                total += Number(
                    checkbox.dataset.price
                );
            }

        });

        if (selectedSeats.length === 0) {

            selectedSeatsText.textContent =
                "Chưa chọn ghế";

            detailSelectedSeats.textContent =
                "Chưa chọn ghế";

            selectedCount.textContent = "0";

            seatDetailsList.innerHTML = "";

            nextButton.disabled = true;

        } else {

            const selectedSeatNames =
                selectedSeats.join(", ");

            selectedSeatsText.textContent =
                selectedSeatNames;

            detailSelectedSeats.textContent =
                selectedSeatNames;

            selectedCount.textContent =
                selectedSeats.length;

            let detailsHtml = "";
            const seen = new Set();
            checkboxes.forEach(function (cb) {
                if (cb.checked && !seen.has(cb.dataset.seatName)) {
                    seen.add(cb.dataset.seatName);
                    const typeLabel = cb.dataset.seatType === "VIP" ? "VIP" : cb.dataset.seatType === "COUPLE" || cb.dataset.seatType === "SWEETBOX" ? "Sweetbox" : "Thường";
                    detailsHtml += "<div class='seat-detail-item'><span>" + cb.dataset.seatName + " (" + typeLabel + ")</span><span>" + formatMoney(Number(cb.dataset.price)) + "</span></div>";
                }
            });
            seatDetailsList.innerHTML = detailsHtml;

            nextButton.disabled = false;
        }

        const formattedTotal =
            formatMoney(total);

        totalPriceText.textContent =
            formattedTotal;

        detailTicketPrice.textContent =
            formattedTotal;

        detailTotalPrice.textContent =
            formattedTotal;
    }

    checkboxes.forEach(function (checkbox) {

        checkbox.addEventListener(
            "change",
            function () {

                if (checkbox.checked) {

                    const checkedCount =
                        document.querySelectorAll(
                            "input[name='seatIds']:checked"
                        ).length;

                    if (checkedCount > MAX_SEATS) {

                        checkbox.checked = false;

                        alert(
                            "Bạn chỉ được chọn tối đa "
                            + MAX_SEATS
                            + " ghế cho mỗi lần đặt vé."
                        );
                    }
                }

                updateSelectedSeats();
            }
        );

    });

    seatForm.addEventListener(
        "submit",
        function (event) {

            const selectedCheckboxes =
                document.querySelectorAll(
                    "input[name='seatIds']:checked"
                );

            if (selectedCheckboxes.length === 0) {

                event.preventDefault();

                alert(
                    "Vui lòng chọn ít nhất một ghế."
                );

                return;
            }

            nextButton.disabled = true;
            nextButton.textContent = "Thanh Toán";
        }
    );

    updateSelectedSeats();
</script>

</body>
</html>