<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "dashboard"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Dashboard — CGV System Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">System Admin</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <div class="cgv-header-divider"></div>
                <div class="cgv-user-wrap">
                    <div class="cgv-avatar">SA</div>
                    <span class="cgv-user-name">
                        <c:choose>
                            <c:when test="${not empty sessionScope.account}">${sessionScope.account.fullName}</c:when>
                            <c:otherwise>System Admin</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>
        </div>
    </header>

    <div class="cgv-page">
        <div class="cgv-list-wrap">

            <%-- Stat cards --%>
            <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:16px;margin-bottom:32px;">
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:8px;">Tổng người dùng</div>
                    <div class="cgv-stat-num" style="font-size:28px;">${not empty adminTotalUsers ? adminTotalUsers : '—'}</div>
                </div>
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:8px;">Tổng nhân viên</div>
                    <div class="cgv-stat-num amber" style="font-size:28px;">${not empty adminTotalStaff ? adminTotalStaff : '—'}</div>
                </div>
            </div>

            <%-- System activity chart --%>
            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.4);margin-bottom:16px;">HOẠT ĐỘNG HỆ THỐNG (7 NGÀY GẦN NHẤT)</div>
            <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                <div style="height:300px;">
                    <canvas id="activityChart"></canvas>
                </div>
            </div>

        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"
        integrity="sha384-NrKB+u6Ts6AtkIhwPixiKTzgSKNblyhlk0Sohlgar9UHUBzai/sgnNNWWd291xqt"
        crossorigin="anonymous"></script>
<script>
    var activityChartData = ${activityChartJson};
    new Chart(document.getElementById('activityChart').getContext('2d'), {
        type: 'bar',
        data: {
            labels: activityChartData.labels,
            datasets: [{
                label: 'Số lượt hoạt động',
                data: activityChartData.values,
                backgroundColor: 'rgba(94,63,58,0.7)'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
        }
    });
</script>
</body>
</html>
