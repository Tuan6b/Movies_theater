<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "schedules"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Bán vé tại quầy — Nhân viên CGV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .booking-layout {
            display: grid;
            grid-template-columns: 1fr 360px;
            gap: 24px;
            margin-top: 20px;
        }
        .seat-map-card {
            background: #fff;
            border: 1px solid var(--cgv-border);
            border-radius: 12px;
            padding: 32px;
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        .checkout-card {
            background: #fff;
            border: 1px solid var(--cgv-border);
            border-radius: 12px;
            padding: 24px;
            height: fit-content;
        }
        .screen-banner {
            width: 80%;
            height: 30px;
            background: #e2e8f0;
            border-radius: 4px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 12px;
            font-weight: 700;
            color: #64748b;
            letter-spacing: 6px;
            margin-bottom: 48px;
            border-top: 4px solid #94a3b8;
        }
        .seat-map-grid {
            display: flex;
            flex-direction: column;
            gap: 8px;
            width: 100%;
            align-items: center;
        }
        .seat-row {
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .row-name {
            width: 24px;
            font-size: 13px;
            font-weight: 700;
            color: rgba(94,63,58,0.5);
            text-align: center;
        }
        .seat-container {
            display: inline-block;
            position: relative;
        }
        .seat-input {
            position: absolute;
            opacity: 0;
            width: 0;
            height: 0;
        }
        .seat-btn {
            display: inline-flex;
            width: 38px;
            height: 38px;
            justify-content: center;
            align-items: center;
            border-radius: 6px;
            font-size: 11px;
            font-weight: 700;
            color: #fff;
            cursor: pointer;
            transition: all 0.15s ease-in-out;
            user-select: none;
            border: 2px solid transparent;
        }
        .seat-btn.Normal { background: #3b82f6; }
        .seat-btn.VIP { background: #f59e0b; }
        .seat-btn.Couple { background: #ec4899; }
        
        /* Booked / Sold */
        .seat-btn.booked {
            background: #ef4444 !important;
            color: rgba(255,255,255,0.7);
            cursor: not-allowed;
            opacity: 0.6;
        }
        
        /* Selected Style */
        .seat-input:checked + .seat-btn {
            background: #10b981 !important;
            border-color: #047857;
            transform: scale(1.08);
            box-shadow: 0 4px 8px rgba(16, 185, 129, 0.3);
        }
        
        .legend-list {
            display: flex;
            gap: 16px;
            margin-top: 32px;
            font-size: 12px;
            font-weight: 600;
            color: rgba(94,63,58,0.7);
        }
        .legend-item {
            display: flex;
            align-items: center;
            gap: 6px;
        }
        .legend-color {
            width: 16px;
            height: 16px;
            border-radius: 4px;
        }
        .checkout-title {
            font-family: var(--font-cgv-display);
            font-size: 20px;
            color: var(--cgv-dark);
            margin: 0 0 16px 0;
            border-bottom: 2px solid var(--cgv-border);
            padding-bottom: 12px;
        }
        .checkout-field {
            margin-bottom: 16px;
        }
        .checkout-field label {
            display: block;
            font-size: 11px;
            font-weight: 700;
            color: rgba(94,63,58,0.6);
            margin-bottom: 6px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        .summary-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 8px;
            font-size: 14px;
            color: rgba(94,63,58,0.7);
        }
    </style>
</head>
<body class="cgv-body">

<%@ include file="_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Bán vé tại quầy (UC48)</h1>
        <div class="cgv-header-right">
            <a href="${pageContext.request.contextPath}/employee/schedules" class="btn--cgv-outline">← Quay lại Lịch chiếu</a>
        </div>
    </header>

    <div class="cgv-page">
        <c:if test="${not empty requestScope.flashSuccess}">
            <div class="cgv-alert cgv-alert-success">${requestScope.flashSuccess}</div>
        </c:if>
        <c:if test="${not empty requestScope.flashError}">
            <div class="cgv-alert cgv-alert-danger">${requestScope.flashError}</div>
        </c:if>

        <form id="bookingForm" method="post" action="${pageContext.request.contextPath}/employee/book">
            <input type="hidden" name="scheduleId" value="${schedule.scheduleId}">

            <div class="booking-layout">
                
                <%-- Seat selection map --%>
                <div class="seat-map-card">
                    <div class="screen-banner">MÀN HÌNH</div>

                    <div class="seat-map-grid">
                        <%-- We need to group seats by RowChar. For clean JSP rendering, we can build a loop. --%>
                        <%-- Since row alphabetical characters go from A onwards, we can group them --%>
                        <c:set var="currentRow" value="" />
                        <c:forEach var="seat" items="${seats}" varStatus="loop">
                            <c:if test="${currentRow ne seat.rowChar}">
                                <c:if test="${not loop.first}">
                                    </div> <%-- Close previous row --%>
                                </c:if>
                                <c:set var="currentRow" value="${seat.rowChar}" />
                                <div class="seat-row">
                                    <div class="row-name">${currentRow}</div>
                            </c:if>
                            
                            <%-- Check if seat is booked --%>
                            <c:set var="isBooked" value="false" />
                            <c:forEach var="bookedId" items="${bookedSeatIds}">
                                <c:if test="${bookedId eq seat.seatId}">
                                    <c:set var="isBooked" value="true" />
                                </c:if>
                            </c:forEach>
                            
                            <div class="seat-container">
                                <c:choose>
                                    <c:when test="${isBooked}">
                                        <div class="seat-btn booked" title="Đã bán: ${seat.rowChar}${seat.colNumber}">
                                            ${seat.rowChar}${seat.colNumber}
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <input class="seat-input" type="checkbox" name="seatIds" id="seat-${seat.seatId}" value="${seat.seatId}"
                                               data-name="${seat.rowChar}${seat.colNumber}" data-type="${seat.seatType}">
                                        <label class="seat-btn ${seat.seatType}" for="seat-${seat.seatId}" title="${seat.rowChar}${seat.colNumber} (${seat.seatType})">
                                            ${seat.rowChar}${seat.colNumber}
                                        </label>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            
                            <c:if test="${loop.last}">
                                </div> <%-- Close last row --%>
                            </c:if>
                        </c:forEach>
                    </div>

                    <div class="legend-list">
                        <div class="legend-item"><div class="legend-color Normal" style="background:#3b82f6;"></div> Ghế thường</div>
                        <div class="legend-item"><div class="legend-color VIP" style="background:#f59e0b;"></div> Ghế VIP</div>
                        <div class="legend-item"><div class="legend-color Couple" style="background:#ec4899;"></div> Ghế đôi</div>
                        <div class="legend-item"><div class="legend-color booked" style="background:#ef4444;"></div> Ghế đã bán</div>
                        <div class="legend-item"><div class="legend-color selected" style="background:#10b981;"></div> Ghế đang chọn</div>
                    </div>
                </div>

                <%-- Checkout section --%>
                <div class="checkout-card">
                    <h3 class="checkout-title">Thông tin hoá đơn</h3>
                    
                    <div style="font-size:13px; color:rgba(94,63,58,0.7); margin-bottom:20px; line-height:1.6;">
                        Phim: <strong style="color:var(--cgv-dark);">${schedule.movie.movieName}</strong><br>
                        Phòng: <strong>${schedule.room.roomNumber} (${schedule.room.roomType})</strong><br>
                        Giờ chiếu: <strong style="color:var(--cgv-primary);">${schedule.startTime}</strong><br>
                        Đơn giá cơ bản: <strong>${schedule.baseTicketPrice} VND</strong>
                    </div>

                    <%-- Customer details --%>
                    <div style="border-top: 1px solid var(--cgv-border); padding-top:16px; margin-bottom:16px;">
                        <h4 style="font-size:12px; font-weight:700; margin:0 0 12px 0; color:rgba(94,63,58,0.5); text-transform:uppercase;">KHÁCH HÀNG (TÙY CHỌN)</h4>
                        
                        <div class="checkout-field">
                            <label for="customerEmail">Email</label>
                            <input class="cgv-input" type="email" id="customerEmail" name="customerEmail" placeholder="khachhang@gmail.com">
                        </div>

                        <div class="checkout-field">
                            <label for="customerName">Họ và Tên</label>
                            <input class="cgv-input" type="text" id="customerName" name="customerName" placeholder="Tên khách hàng">
                        </div>

                        <div class="checkout-field">
                            <label for="customerPhone">Số điện thoại</label>
                            <input class="cgv-input" type="tel" id="customerPhone" name="customerPhone" placeholder="SĐT khách hàng">
                        </div>
                    </div>

                    <%-- Summary --%>
                    <div style="border-top: 1px solid var(--cgv-border); padding-top:16px; margin-bottom:24px;">
                        <div class="summary-row">
                            <span>Ghế đã chọn:</span>
                            <span id="selected-seats-display" style="font-weight:700; color:var(--cgv-dark);">Chưa chọn</span>
                        </div>
                        <div class="summary-row" style="font-size:16px; font-weight:700; color:var(--cgv-dark); margin-top:12px; padding-top:12px; border-top:1px dashed var(--cgv-border);">
                            <span>TỔNG TIỀN:</span>
                            <span id="total-price-display" style="color:var(--cgv-primary);">0 VND</span>
                        </div>
                    </div>

                    <button type="submit" class="btn--cgv" style="width:100%; height:46px; font-size:14px; background:var(--cgv-primary); border-radius:8px;">
                        XÁC NHẬN XUẤT VÉ
                    </button>
                </div>

            </div>
        </form>
    </div>
</div>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const basePrice = ${schedule.baseTicketPrice};
        const checkboxes = document.querySelectorAll(".seat-input");
        const selectedSeatsDisplay = document.getElementById("selected-seats-display");
        const totalPriceDisplay = document.getElementById("total-price-display");
        const bookingForm = document.getElementById("bookingForm");

        function updateSummary() {
            let selectedSeats = [];
            let totalPrice = 0;

            checkboxes.forEach(cb => {
                if (cb.checked) {
                    selectedSeats.push(cb.getAttribute("data-name"));
                    const type = cb.getAttribute("data-type");
                    let seatPrice = basePrice;
                    if (type === "VIP") {
                        seatPrice = basePrice * 1.5;
                    } else if (type === "Couple") {
                        seatPrice = basePrice * 2; // Couple seat double base price
                    }
                    totalPrice += seatPrice;
                }
            });

            if (selectedSeats.length > 0) {
                selectedSeatsDisplay.textContent = selectedSeats.join(", ");
                totalPriceDisplay.textContent = totalPrice.toLocaleString("vi-VN") + " VND";
            } else {
                selectedSeatsDisplay.textContent = "Chưa chọn";
                totalPriceDisplay.textContent = "0 VND";
            }
        }

        checkboxes.forEach(cb => {
            cb.addEventListener("change", updateSummary);
        });

        bookingForm.addEventListener("submit", function (e) {
            let checkedCount = 0;
            checkboxes.forEach(cb => {
                if (cb.checked) checkedCount++;
            });

            if (checkedCount === 0) {
                e.preventDefault();
                alert("Vui lòng chọn ít nhất một ghế để xuất vé.");
            }
        });
    });
</script>

</body>
</html>
