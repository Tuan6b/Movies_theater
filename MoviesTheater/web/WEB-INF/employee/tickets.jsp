<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<% request.setAttribute("activeNav", "schedules"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Danh sách vé đã đặt — Nhân viên CGV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .ticket-info-card {
            background: #fff;
            border: 1px solid var(--cgv-border);
            border-radius: 12px;
            padding: 24px;
            margin-bottom: 24px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 20px;
            flex-wrap: wrap;
        }
        .info-card-left {
            display: flex;
            gap: 16px;
            align-items: center;
        }
        .info-card-poster {
            width: 70px;
            height: 100px;
            object-fit: cover;
            border-radius: 6px;
            border: 1px solid var(--cgv-border);
        }
        .info-card-detail h2 {
            font-family: var(--font-cgv-display);
            font-size: 24px;
            margin: 0 0 6px 0;
            color: var(--cgv-dark);
        }
        .info-card-meta {
            font-size: 14px;
            color: rgba(94,63,58,0.6);
            display: flex;
            gap: 16px;
        }
    </style>
</head>
<body class="cgv-body">

<%@ include file="_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Danh sách Vé đã đặt (UC47)</h1>
        <div class="cgv-header-right" style="display:flex; gap:8px; align-items:center;">
            <a href="${pageContext.request.contextPath}/employee/book?scheduleId=${schedule.scheduleId}" class="btn--cgv">+ Thêm vé</a>
            <a href="${pageContext.request.contextPath}/employee/schedules" class="btn--cgv-outline">← Quay lại Lịch chiếu</a>
        </div>
    </header>

    <div class="cgv-page">
        <div class="cgv-table-wrap" style="flex: 1;">

            <%-- Screening details --%>
            <div class="ticket-info-card">
                <div class="info-card-left">
                    <c:if test="${not empty schedule.movie.poster}">
                        <img class="info-card-poster" src="${schedule.movie.poster}" alt="Poster">
                    </c:if>
                    <div class="info-card-detail">
                        <h2>${schedule.movie.movieName}</h2>
                        <div class="info-card-meta">
                            <span>Phòng: <strong>${schedule.room.roomNumber} (${schedule.room.roomType})</strong></span>
                            <span>Thời gian: <strong style="color: var(--cgv-primary);">${schedule.startTime}</strong></span>
                            <span>Thời lượng: <strong>${schedule.movie.duration} phút</strong></span>
                        </div>
                    </div>
                </div>
                <div>
                    <span class="cgv-pill active" style="font-size:14px; padding:8px 16px;">
                        Số vé đã đặt: <strong>${not empty tickets ? tickets.size() : 0} vé</strong>
                    </span>
                </div>
            </div>

            <c:if test="${not empty requestScope.flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${requestScope.flashSuccess}</div>
            </c:if>
            <c:if test="${not empty requestScope.flashError}">
                <div class="cgv-alert cgv-alert-danger">${requestScope.flashError}</div>
            </c:if>

            <c:if test="${not empty requestScope.flashNewCodes}">
                <div class="cgv-alert cgv-alert-success" style="display:flex; flex-direction:column; gap:8px;">
                    <strong>Mã vé vừa tạo — giao cho khách hàng:</strong>
                    <div style="display:flex; flex-wrap:wrap; gap:8px; margin-top:4px;">
                        <c:forEach var="code" items="${requestScope.flashNewCodes}">
                            <code style="font-family:monospace; background:rgba(255,255,255,0.7); border:1px solid rgba(40,167,69,0.3); padding:6px 12px; border-radius:6px; font-size:13px; font-weight:700; letter-spacing:1px; color:#155724;">
                                ${code}
                            </code>
                        </c:forEach>
                    </div>
                </div>
            </c:if>

            <%-- Tickets table --%>
            <div class="cgv-data-wrap">
                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th>Mã Vé</th>
                            <th>Ghế</th>
                            <th>Giá Vé</th>
                            <th>Khách Hàng</th>
                            <th>Liên Hệ</th>
                            <th>Phương Thức TT</th>
                            <th>Thời Gian Đặt</th>
                            <th>Trạng Thái</th>
                            <th style="text-align:right;">Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty tickets}">
                                <c:forEach var="t" items="${tickets}">
                                    <tr>
                                        <td>
                                            <code style="font-family:monospace; background:#f5f5f5; padding:4px 8px; border-radius:4px; font-weight:600; font-size:12px; color:var(--cgv-dark);">
                                                ${t.code}
                                            </code>
                                        </td>
                                        <td>
                                            <span class="cgv-pill" style="font-weight:700; background:rgba(224, 20, 20, 0.05); color:var(--cgv-primary);">
                                                Hàng ${t.seat.rowChar} - Số ${t.seat.colNumber} (${t.seat.seatType})
                                            </span>
                                        </td>
                                        <td style="font-weight:600; color:var(--cgv-dark);"><fmt:formatNumber value="${t.priceAtBooking}" type="number" maxFractionDigits="0"/> VND</td>
                                        <td style="font-weight:700; color:var(--cgv-dark);">${t.invoice.account.fullName}</td>
                                        <td style="font-size:13px; color:rgba(94,63,58,0.7);">
                                            Email: ${t.invoice.account.email}<br>
                                            SĐT: ${not empty t.invoice.account.phoneNumber ? t.invoice.account.phoneNumber : '—'}
                                        </td>
                                        <td>
                                            <span class="cgv-pill" style="font-weight:600; text-transform:uppercase;">
                                                ${t.invoice.paymentMethod}
                                            </span>
                                        </td>
                                        <td style="font-size:13px; color:rgba(94,63,58,0.7);"><fmt:formatDate value="${t.invoice.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${t.checkedIn}">
                                                    <span class="cgv-badge active">Đã Check-in</span>
                                                    <div style="font-size:10px; color:rgba(94,63,58,0.5); margin-top:2px;">
                                                        Vào lúc: ${t.checkedInAt}
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="cgv-badge inactive">Chờ Check-in</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align:right;">
                                            <c:if test="${not t.checkedIn}">
                                                <form method="post" action="${pageContext.request.contextPath}/employee/checkin" style="display:inline;">
                                                    <input type="hidden" name="bookingId" value="${t.ticketId}">
                                                    <button type="submit" class="btn--cgv" style="font-size:11px; padding:6px 12px; background:#28a745;">
                                                        Check-in Nhanh
                                                    </button>
                                                </form>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="9" style="text-align:center; padding:64px; color:rgba(94,63,58,0.4);">
                                        Chưa có vé nào được đặt cho suất chiếu này.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                    <c:if test="${not empty tickets}">
                        <tfoot>
                            <tr style="background:#fef9f0; border-top:2px solid var(--cgv-border);">
                                <td colspan="8" style="text-align:right; font-weight:700; padding:14px 16px; color:var(--cgv-dark); font-size:14px;">
                                    Tổng doanh thu suất chiếu:
                                </td>
                                <td style="font-weight:700; color:var(--cgv-primary); font-size:16px; padding:14px 16px;">
                                    <fmt:formatNumber value="${totalRevenue}" type="number" maxFractionDigits="0"/> VND
                                </td>
                            </tr>
                        </tfoot>
                    </c:if>
                </table>
            </div>
        </div>
    </div>
</div>
</body>
</html>
