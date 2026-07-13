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

            <%-- Stat cards --%>
            <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:16px;margin-bottom:32px;">
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:8px;">Suất chiếu hôm nay</div>
                    <div class="cgv-stat-num" style="font-size:28px;">${not empty empScreeningsToday ? empScreeningsToday : '—'}</div>
                </div>
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:8px;">Check-in hôm nay</div>
                    <div class="cgv-stat-num amber" style="font-size:28px;">${not empty empCheckinsToday ? empCheckinsToday : '—'}</div>
                </div>
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:8px;">Phim đang chiếu</div>
                    <div class="cgv-stat-num" style="font-size:28px;">${not empty empActiveMovies ? empActiveMovies : '—'}</div>
                </div>
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:8px;">Trạng thái ca</div>
                    <div class="cgv-stat-num red" style="font-size:28px;">${not empty empShiftStatus ? empShiftStatus : '—'}</div>
                </div>
            </div>

            <%-- Shift breakdown chart --%>
            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.4);margin-bottom:16px;">CA LÀM VIỆC THÁNG NÀY</div>
            <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;margin-bottom:32px;">
                <div style="height:300px;">
                    <canvas id="shiftChart"></canvas>
                </div>
            </div>

            <%-- Account --%>
            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.4);margin-bottom:16px;">TÀI KHOẢN</div>
            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;">

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Đổi mật khẩu</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Cập nhật mật khẩu đăng nhập của bạn</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/change-password" class="btn--cgv-outline">Đổi mật khẩu</a>
                    </div>
                </div>

            </div>

        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"
        integrity="sha384-NrKB+u6Ts6AtkIhwPixiKTzgSKNblyhlk0Sohlgar9UHUBzai/sgnNNWWd291xqt"
        crossorigin="anonymous"></script>
<script>
    var shiftChartData = ${shiftChartJson};
    new Chart(document.getElementById('shiftChart').getContext('2d'), {
        type: 'doughnut',
        data: {
            labels: shiftChartData.labels,
            datasets: [{
                data: shiftChartData.values,
                backgroundColor: ['#865300', '#bd0000', '#5e3f3a']
            }]
        },
        options: { responsive: true, maintainAspectRatio: false }
    });
</script>
</body>
</html>
