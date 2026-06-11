<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "dashboard"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Admin Dashboard — CGV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="_sidebar.jsp" %>

<div class="cgv-main">
    <header class="cgv-header" style="background: linear-gradient(135deg, #1a0a0a 0%, #0d0505 100%); border-bottom: 1px solid rgba(220, 38, 38, 0.2);">
        <h1 class="cgv-header-title" style="color:#fff;">Admin Dashboard</h1>
        <div class="cgv-header-right">
            <div class="cgv-user-wrap">
                <div class="cgv-avatar" style="background:#dc2626;color:#fff;">AD</div>
                <span class="cgv-user-name" style="color:rgba(255,255,255,0.8);">${sessionScope.account.profile.fullName}</span>
                <span style="font-size:10px;background:rgba(220,38,38,0.3);color:#fca5a5;padding:2px 10px;border-radius:999px;font-weight:600;margin-left:8px;">Admin</span>
            </div>
        </div>
    </header>

    <div class="cgv-page">
        <div class="cgv-list-wrap">

            <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:16px;margin-bottom:32px;">
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;border-left:3px solid #dc2626;">
                    <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:8px;">Total Accounts</div>
                    <div class="cgv-stat-num" style="font-size:28px;">${not empty totalAccounts ? totalAccounts : '—'}</div>
                </div>
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;border-left:3px solid #f59e0b;">
                    <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:8px;">Staff</div>
                    <div class="cgv-stat-num amber" style="font-size:28px;">${not empty staffCount ? staffCount : '—'}</div>
                </div>
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;border-left:3px solid #10b981;">
                    <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:8px;">Active</div>
                    <div class="cgv-stat-num" style="font-size:28px;">${not empty activeCount ? activeCount : '—'}</div>
                </div>
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;border-left:3px solid #b91c1c;">
                    <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:8px;">Locked</div>
                    <div class="cgv-stat-num red" style="font-size:28px;">${not empty lockedCount ? lockedCount : '—'}</div>
                </div>
            </div>

            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.4);margin-bottom:16px;">QUẢN LÝ HỆ THỐNG</div>
            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin-bottom:32px;">

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Người dùng &amp; Phân quyền</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Tạo, chỉnh sửa, khóa/mở khóa tài khoản, phân quyền</div>
                    <a href="${pageContext.request.contextPath}/manager/users" class="btn--cgv">Quản lý Users</a>
                </div>

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Phim &amp; Thể loại</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Quản lý phim, thể loại, lịch chiếu</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/manager/movies" class="btn--cgv-outline">Phim</a>
                        <a href="${pageContext.request.contextPath}/admin/genre" class="btn--cgv-outline">Thể loại</a>
                        <a href="${pageContext.request.contextPath}/manager/schedules" class="btn--cgv-outline">Lịch chiếu</a>
                    </div>
                </div>

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Kinh doanh</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Phòng chiếu, khuyến mãi, đồ ăn, thống kê</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/RoomServlet" class="btn--cgv-outline">Phòng</a>
                        <a href="${pageContext.request.contextPath}/manager/promotions" class="btn--cgv-outline">Khuyến mãi</a>
                        <a href="${pageContext.request.contextPath}/manager/food" class="btn--cgv-outline">Đồ ăn</a>
                        <a href="${pageContext.request.contextPath}/manager/analytics" class="btn--cgv-outline">Thống kê</a>
                    </div>
                </div>

            </div>

            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.4);margin-bottom:16px;">TIỆN ÍCH</div>
            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;">

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Check-in</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Xác nhận vé của khách hàng</div>
                    <a href="${pageContext.request.contextPath}/manager/checkin" class="btn--cgv-outline">Check-in</a>
                </div>

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Cấu hình hệ thống</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Cài đặt chung</div>
                    <a href="${pageContext.request.contextPath}/manager/settings" class="btn--cgv-outline">Settings</a>
                </div>

            </div>
        </div>
    </div>
</div>
</body>
</html>
