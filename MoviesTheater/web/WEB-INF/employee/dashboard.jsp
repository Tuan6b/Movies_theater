<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Employee Dashboard — CGV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .employee-badge { font-size:10px; background:rgba(59,130,246,0.15); color:#3b82f6; padding:2px 10px; border-radius:999px; font-weight:600; margin-left:8px; }
    </style>
</head>
<body class="cgv-body">

<%@ include file="_sidebar.jsp" %>

<div class="cgv-main">
    <header class="cgv-header">
        <h1 class="cgv-header-title">Employee Dashboard</h1>
        <div class="cgv-header-right">
            <div class="cgv-user-wrap">
                <div class="cgv-avatar">EM</div>
                <span class="cgv-user-name">
                    ${sessionScope.account.profile.fullName}
                    <span class="employee-badge">Employee</span>
                </span>
            </div>
        </div>
    </header>

    <div class="cgv-page">
        <div class="cgv-list-wrap">

            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin-bottom:32px;">

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Phim</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Xem danh sách phim đang chiếu</div>
                    <a href="${pageContext.request.contextPath}/manager/movies" class="btn--cgv-outline">Danh sách phim</a>
                </div>

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Phòng chiếu</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Xem danh sách phòng chiếu</div>
                    <a href="${pageContext.request.contextPath}/RoomServlet" class="btn--cgv-outline">Danh sách phòng</a>
                </div>

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Lịch chiếu</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Xem lịch chiếu phim</div>
                    <a href="${pageContext.request.contextPath}/manager/schedules" class="btn--cgv-outline">Xem lịch chiếu</a>
                </div>

            </div>

            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;">
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Check-in vé</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Xác nhận vé cho khách hàng</div>
                    <a href="${pageContext.request.contextPath}/manager/checkin" class="btn--cgv-outline">Check-in</a>
                </div>
            </div>

        </div>
    </div>
</div>
</body>
</html>
