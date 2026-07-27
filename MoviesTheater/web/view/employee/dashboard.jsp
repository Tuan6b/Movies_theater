<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "dashboard"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Dashboard — CGV Employee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .emp-kpi-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 16px;
            margin-bottom: 16px;
        }
        .emp-card {
            background: #fff;
            border: 1px solid var(--cgv-border);
            border-radius: 12px;
            padding: 24px;
        }
        .emp-key {
            font-family: var(--font-cgv-ui);
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 2px;
            text-transform: uppercase;
            color: rgba(94,63,58,0.5);
            margin-bottom: 8px;
        }
        .emp-val { font-size: 28px; }
        .emp-val.sm { font-size: 22px; }
        .emp-sub {
            font-size: 12px;
            color: rgba(94,63,58,0.55);
            margin-top: 6px;
        }
        .emp-strip {
            display: flex;
            flex-wrap: wrap;
            gap: 28px;
            background: #fff;
            border: 1px solid var(--cgv-border);
            border-radius: 12px;
            padding: 16px 24px;
            margin-bottom: 32px;
        }
        .emp-strip-item { font-size: 13px; color: rgba(94,63,58,0.7); }
        .emp-strip-item b { font-family: var(--font-cgv-ui); font-size: 15px; color: var(--cgv-dark); }
        .section-heading {
            font-family: var(--font-cgv-ui);
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 2px;
            text-transform: uppercase;
            color: rgba(94,63,58,0.5);
            margin: 0 0 12px 0;
        }
        .two-col-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 24px;
            margin-bottom: 32px;
        }
        .bar-wrap {
            height: 8px;
            background: rgba(94,63,58,0.08);
            border-radius: 4px;
            overflow: hidden;
            margin-top: 4px;
        }
        .bar-fill { height: 100%; background: var(--cgv-red); border-radius: 4px; }
        .bar-fill.amber { background: var(--cgv-amber); }
        .emp-act {
            display: inline-block;
            font-size: 12px;
            font-weight: 600;
            text-decoration: none;
            padding: 4px 10px;
            border-radius: 6px;
            border: 1px solid var(--cgv-border);
            color: var(--cgv-dark);
        }
        .emp-act:hover { border-color: var(--cgv-red); color: var(--cgv-red); }
        .emp-act.muted {
            color: rgba(94,63,58,0.35);
            border-color: rgba(94,63,58,0.12);
            cursor: not-allowed;
        }
        .emp-badge {
            display: inline-block;
            min-width: 20px;
            padding: 1px 7px;
            border-radius: 10px;
            background: var(--cgv-red);
            color: #fff;
            font-size: 11px;
            font-weight: 700;
            text-align: center;
        }
        @media (max-width: 1100px) { .emp-kpi-grid { grid-template-columns: repeat(2, 1fr); } }
        @media (max-width: 900px)  { .two-col-grid { grid-template-columns: 1fr; } }
    </style>
</head>
<body class="cgv-body">

<%@ include file="_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Dashboard</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <%@ include file="_notifications.jsp" %>
                <div class="cgv-header-divider"></div>
                <div class="cgv-user-wrap">
                    <div class="cgv-avatar">EM</div>
                    <span class="cgv-user-name">
                        <c:choose>
                            <c:when test="${not empty sessionScope.account}">${sessionScope.account.fullName}</c:when>
                            <c:otherwise>Employee</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>
        </div>
    </header>

    <div class="cgv-page">
        <div class="cgv-list-wrap">

            <c:if test="${not empty flashError}">
                <div style="background:#fee2e2;border:1px solid #ef4444;border-radius:8px;padding:12px 16px;margin-bottom:20px;font-size:13px;font-weight:600;color:#991b1b;">
                    ${flashError}
                </div>
            </c:if>

            <%-- No-shift warning --%>
            <c:if test="${noShift}">
                <div style="background:#fef3c7;border:1px solid #f59e0b;border-radius:8px;padding:12px 16px;margin-bottom:20px;display:flex;align-items:center;gap:10px;">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#d97706" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0;">
                        <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                        <line x1="12" y1="9" x2="12" y2="13"/>
                        <line x1="12" y1="17" x2="12.01" y2="17"/>
                    </svg>
                    <span style="font-size:13px;font-weight:600;color:#92400e;">Bạn không có ca làm việc vào lúc này. Chức năng check-in vé và xuất vé bị tạm khoá cho đến khi ca bắt đầu.</span>
                </div>
            </c:if>

            <%-- Today at a glance --%>
            <div class="emp-kpi-grid">
                <div class="emp-card">
                    <div class="emp-key">Doanh thu hôm nay (VND)</div>
                    <div class="cgv-stat-num emp-val">${not empty empRevenueToday ? empRevenueToday : '0'}</div>
                    <div class="emp-sub">Toàn rạp · ${not empty empInvoicesToday ? empInvoicesToday : 0} hoá đơn</div>
                </div>
                <div class="emp-card">
                    <div class="emp-key">Vé bán hôm nay</div>
                    <div class="cgv-stat-num amber emp-val">${not empty empTicketsToday ? empTicketsToday : 0}</div>
                    <div class="emp-sub">${not empty empCheckinsToday ? empCheckinsToday : 0} vé đã check-in</div>
                </div>
                <div class="emp-card">
                    <div class="emp-key">Chờ check-in (${empWindowHours}h tới)</div>
                    <div class="cgv-stat-num red emp-val">${not empty empPendingSoon ? empPendingSoon : 0}</div>
                    <div class="emp-sub">
                        <c:choose>
                            <c:when test="${noShift}">Cần có ca làm việc để quét vé</c:when>
                            <c:otherwise>
                                <a href="${pageContext.request.contextPath}/employee/checkin"
                                   style="color:var(--cgv-red);font-weight:600;text-decoration:none;">Mở màn hình quét vé →</a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div class="emp-card">
                    <div class="emp-key">Ca hôm nay</div>
                    <div class="cgv-stat-num emp-val sm">${not empty empShiftToday ? empShiftToday : 'Không có ca'}</div>
                    <div class="emp-sub">${empShiftStatus}</div>
                </div>
            </div>

            <div class="emp-strip">
                <span class="emp-strip-item">Suất chiếu hôm nay: <b>${not empty empScreeningsToday ? empScreeningsToday : 0}</b></span>
                <span class="emp-strip-item">Suất còn lại: <b>${not empty empUpcomingCount ? empUpcomingCount : 0}</b></span>
                <span class="emp-strip-item">Hoá đơn đã thanh toán: <b>${not empty empInvoicesToday ? empInvoicesToday : 0}</b></span>
                <c:if test="${empPendingExchanges gt 0}">
                    <span class="emp-strip-item">
                        Yêu cầu đổi ca chờ bạn duyệt: <span class="emp-badge">${empPendingExchanges}</span>
                        <a href="${pageContext.request.contextPath}/employee/my-shifts"
                           style="color:var(--cgv-red);font-weight:600;text-decoration:none;margin-left:6px;">Xem →</a>
                    </span>
                </c:if>
            </div>

            <%-- Shows still to run today --%>
            <div class="section-heading">SUẤT CHIẾU CÒN LẠI HÔM NAY</div>
            <div class="cgv-data-wrap" style="margin-bottom:32px;">
                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th>Giờ</th>
                            <th>Phim</th>
                            <th>Phòng</th>
                            <th>Đã bán</th>
                            <th>Chờ check-in</th>
                            <th style="text-align:right;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty empUpcoming}">
                                <c:forEach var="sh" items="${empUpcoming}">
                                    <tr>
                                        <td style="font-weight:700;white-space:nowrap;">
                                            ${sh.startTime}
                                            <c:if test="${sh.ongoing}">
                                                <div style="font-size:10px;font-weight:700;color:var(--cgv-red);letter-spacing:1px;">ĐANG CHIẾU</div>
                                            </c:if>
                                        </td>
                                        <td style="font-weight:600;max-width:240px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">
                                            ${sh.movieName}
                                        </td>
                                        <td>${sh.roomNumber}</td>
                                        <td style="min-width:110px;">
                                            <span style="font-weight:500;">${sh.sold}/${sh.capacity}</span>
                                            <c:if test="${sh.capacity gt 0}">
                                                <div class="bar-wrap">
                                                    <div class="bar-fill" style="width:${sh.soldPercent}%;"></div>
                                                </div>
                                            </c:if>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${sh.pending gt 0}">
                                                    <span class="emp-badge">${sh.pending}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color:rgba(94,63,58,0.35);">—</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align:right;white-space:nowrap;">
                                            <c:choose>
                                                <c:when test="${noShift}">
                                                    <span class="emp-act muted">Bán vé</span>
                                                    <span class="emp-act muted">Check-in</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <a class="emp-act"
                                                       href="${pageContext.request.contextPath}/employee/book?scheduleId=${sh.scheduleId}">Bán vé</a>
                                                    <a class="emp-act"
                                                       href="${pageContext.request.contextPath}/employee/checkin">Check-in</a>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" style="text-align:center;padding:32px;color:rgba(94,63,58,0.4);">
                                        Không còn suất chiếu nào trong hôm nay.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <%-- Revenue trend + how today's money came in --%>
            <div class="two-col-grid">

                <div class="cgv-data-wrap">
                    <div class="cgv-data-toolbar">
                        <span class="section-heading" style="margin:0;">DOANH THU 7 NGÀY GẦN NHẤT</span>
                    </div>
                    <div style="padding:20px;">
                        <div style="height:260px;">
                            <canvas id="revenueChart"></canvas>
                        </div>
                    </div>
                </div>

                <div class="cgv-data-wrap">
                    <div class="cgv-data-toolbar">
                        <span class="section-heading" style="margin:0;">DOANH THU HÔM NAY THEO PHƯƠNG THỨC</span>
                    </div>
                    <table class="cgv-dt">
                        <thead>
                            <tr>
                                <th>Phương thức</th>
                                <th>Hoá đơn</th>
                                <th>Doanh thu (VND)</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty empPaymentToday}">
                                    <c:forEach var="ps" items="${empPaymentToday}">
                                        <tr>
                                            <td>
                                                <span class="cgv-pill" style="font-weight:600;text-transform:uppercase;">${ps.method}</span>
                                                <div style="font-size:11px;color:rgba(94,63,58,0.45);margin-top:4px;">
                                                    ${ps.counter ? 'Tại quầy' : 'Trực tuyến'}
                                                </div>
                                            </td>
                                            <td>${ps.count}</td>
                                            <td>
                                                <span style="font-weight:500;">${ps.formattedAmount}</span>
                                                <div class="bar-wrap">
                                                    <div class="bar-fill ${ps.counter ? '' : 'amber'}" style="width:${ps.percent}%;"></div>
                                                </div>
                                                <div style="font-size:11px;color:rgba(94,63,58,0.45);margin-top:2px;">${ps.percent}%</div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="3" style="text-align:center;padding:32px;color:rgba(94,63,58,0.4);">
                                            Hôm nay chưa có hoá đơn nào được thanh toán.
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>

            </div>

            <%-- Shortcuts --%>
            <div class="section-heading">LỐI TẮT</div>
            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;">

                <div class="emp-card">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">
                        Ca làm việc của tôi
                        <c:if test="${empPendingExchanges gt 0}">
                            <span class="emp-badge" style="margin-left:4px;">${empPendingExchanges}</span>
                        </c:if>
                    </div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">
                        <c:choose>
                            <c:when test="${empPendingExchanges gt 0}">Có yêu cầu đổi ca đang chờ bạn phản hồi</c:when>
                            <c:otherwise>Xem lịch ca tháng này và gửi yêu cầu đổi ca</c:otherwise>
                        </c:choose>
                    </div>
                    <a href="${pageContext.request.contextPath}/employee/my-shifts" class="btn--cgv-outline">Mở lịch ca</a>
                </div>

                <div class="emp-card">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Lịch chiếu</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Tra cứu suất chiếu theo ngày, phim hoặc phòng</div>
                    <a href="${pageContext.request.contextPath}/employee/schedules" class="btn--cgv-outline">Xem lịch chiếu</a>
                </div>

                <div class="emp-card">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Đổi mật khẩu</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Cập nhật mật khẩu đăng nhập của bạn</div>
                    <a href="${pageContext.request.contextPath}/change-password" class="btn--cgv-outline">Đổi mật khẩu</a>
                </div>

            </div>

        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"
        integrity="sha384-NrKB+u6Ts6AtkIhwPixiKTzgSKNblyhlk0Sohlgar9UHUBzai/sgnNNWWd291xqt"
        crossorigin="anonymous"></script>
<script>
    var revenueChartData = ${not empty revenueChartJson ? revenueChartJson : '{"labels":[],"values":[]}'};
    new Chart(document.getElementById('revenueChart').getContext('2d'), {
        type: 'bar',
        data: {
            labels: revenueChartData.labels,
            datasets: [{
                label: 'Doanh thu (VND)',
                data: revenueChartData.values,
                backgroundColor: 'rgba(189,0,0,0.7)'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true } }
        }
    });
</script>
</body>
</html>
