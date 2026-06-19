<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "checkin"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Check-in Vé — Nhân viên CGV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .checkin-lookup {
            background: #fff;
            border: 1px solid var(--cgv-border);
            border-radius: 12px;
            padding: 32px;
            margin-bottom: 32px;
        }
        .checkin-lookup-title {
            font-family: var(--font-cgv-ui);
            font-weight: 700;
            font-size: 10px;
            letter-spacing: 2px;
            text-transform: uppercase;
            color: rgba(94,63,58,0.5);
            margin-bottom: 20px;
        }
        .checkin-search-row {
            display: flex;
            gap: 12px;
            align-items: center;
            flex-wrap: wrap;
        }
        .checkin-result {
            margin-top: 24px;
            background: #faf6f5;
            border: 1px solid var(--cgv-border);
            border-radius: 10px;
            padding: 20px 24px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
            flex-wrap: wrap;
        }
        .checkin-result-info { display: flex; flex-direction: column; gap: 4px; }
        .checkin-result-name { font-family: var(--font-cgv-display); font-size: 20px; color: var(--cgv-dark); }
        .checkin-result-meta { font-size: 13px; color: rgba(94,63,58,0.6); }
    </style>
</head>
<body class="cgv-body">

<%@ include file="_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Soát vé khách hàng (Check-in)</h1>
        <div class="cgv-header-right">
            <div class="cgv-user-wrap">
                <div class="cgv-avatar">EM</div>
                <span class="cgv-user-name">${sessionScope.account.fullName}</span>
            </div>
        </div>
    </header>

    <div class="cgv-page">
        <div class="cgv-table-wrap" style="flex: 1;">

            <c:if test="${not empty requestScope.flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${requestScope.flashSuccess}</div>
            </c:if>
            <c:if test="${not empty requestScope.flashError}">
                <div class="cgv-alert cgv-alert-danger">${requestScope.flashError}</div>
            </c:if>

            <div class="checkin-lookup">
                <div class="checkin-lookup-title">TRA CỨU MÃ VÉ CHECK-IN</div>
                <form method="get" action="${pageContext.request.contextPath}/employee/checkin" class="checkin-search-row">
                    <input class="cgv-input" style="flex:1;max-width:320px;" type="text"
                           name="code" placeholder="Nhập mã vé hoặc mã QR..."
                           value="${param.code}">
                    <button type="submit" class="btn--cgv">Xác thực vé</button>
                </form>

                <c:if test="${not empty booking}">
                    <div class="checkin-result">
                        <div class="checkin-result-info">
                            <div class="checkin-result-name">${booking.movieTitle}</div>
                            <div class="checkin-result-meta">
                                Suất chiếu: ${booking.showDate} · ${booking.startTime}
                            </div>
                            <div class="checkin-result-meta" style="margin-top:4px;">
                                Khách hàng: <strong>${booking.customerName}</strong> ·
                                Ghế: <strong style="color:var(--cgv-primary);">${booking.seats}</strong>
                            </div>
                        </div>
                        <div style="display:flex;gap:12px;align-items:center;">
                            <span class="cgv-badge ${booking.checkedIn ? 'active' : 'inactive'}">
                                ${booking.checkedIn ? 'ĐÃ CHECK-IN' : 'CHƯA CHECK-IN'}
                            </span>
                            <c:if test="${not booking.checkedIn}">
                                <form method="post" action="${pageContext.request.contextPath}/employee/checkin">
                                    <input type="hidden" name="bookingId" value="${booking.bookingId}">
                                    <button type="submit" class="btn--cgv" style="background:#28a745;">Xác nhận Check-in</button>
                                </form>
                            </c:if>
                        </div>
                    </div>
                </c:if>
            </div>

            <div class="cgv-toolbar">
                <div class="cgv-pills">
                    <a href="?filter=today"   class="cgv-pill ${empty param.filter || param.filter eq 'today'   ? 'active' : ''}">Vé Hôm Nay</a>
                    <a href="?filter=pending" class="cgv-pill ${param.filter eq 'pending' ? 'active' : ''}">Chờ Check-in</a>
                    <a href="?filter=checked" class="cgv-pill ${param.filter eq 'checked' ? 'active' : ''}">Đã Check-in</a>
                </div>
            </div>

            <div class="cgv-data-wrap">
                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th>Mã Vé</th>
                            <th>Khách Hàng</th>
                            <th>Phim</th>
                            <th>Suất Chiếu</th>
                            <th>Ghế</th>
                            <th>Giá Vé</th>
                            <th>Trạng Thái</th>
                            <th>Hành Động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty bookingList}">
                                <c:forEach var="b" items="${bookingList}">
                                    <tr>
                                        <td>
                                            <code style="font-family:monospace;background:#f3f3f3;padding:2px 8px;border-radius:4px;font-size:12px;letter-spacing:1px;font-weight:600;">
                                                ${b.code}
                                            </code>
                                        </td>
                                        <td style="font-weight:700; color:var(--cgv-dark);">${b.customerName}</td>
                                        <td>${b.movieTitle}</td>
                                        <td style="font-size:13px;white-space:nowrap;">${b.showDate} ${b.startTime}</td>
                                        <td><span class="cgv-pill">${b.seats}</span></td>
                                        <td style="font-weight:600; color:var(--cgv-dark);">${b.totalAmount} VND</td>
                                        <td>
                                            <span class="cgv-badge ${b.checkedIn ? 'active' : 'inactive'}">
                                                ${b.checkedIn ? 'Đã Check In' : 'Chờ Check In'}
                                            </span>
                                        </td>
                                        <td>
                                            <c:if test="${not b.checkedIn}">
                                                <form method="post" action="${pageContext.request.contextPath}/employee/checkin">
                                                    <input type="hidden" name="bookingId" value="${b.bookingId}">
                                                    <button type="submit" class="btn--cgv-outline" style="font-size:11px; padding:6px 12px; border-color:#28a745; color:#28a745;">Check In</button>
                                                </form>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="8" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">Không tìm thấy thông tin vé nào.</td></tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
</body>
</html>
