<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "profile"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Hồ sơ cá nhân — Nhân viên CGV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .profile-card {
            background: #fff;
            border: 1px solid var(--cgv-border);
            border-radius: 12px;
            padding: 32px;
            max-width: 600px;
            margin: 24px auto;
        }
        .profile-header {
            display: flex;
            align-items: center;
            gap: 20px;
            margin-bottom: 32px;
            border-bottom: 1px solid var(--cgv-border);
            padding-bottom: 24px;
        }
        .profile-avatar-large {
            width: 80px;
            height: 80px;
            background: var(--cgv-primary);
            color: #fff;
            font-size: 32px;
            font-weight: 700;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
        }
        .profile-title h2 {
            font-family: var(--font-cgv-display);
            font-size: 26px;
            color: var(--cgv-dark);
            margin: 0 0 4px 0;
        }
        .profile-title span {
            font-size: 14px;
            color: rgba(94,63,58,0.5);
            font-weight: 600;
        }
        .profile-details-grid {
            display: grid;
            grid-template-columns: 140px 1fr;
            gap: 16px 12px;
            font-size: 14px;
        }
        .profile-label {
            font-weight: 700;
            color: rgba(94,63,58,0.5);
            text-transform: uppercase;
            font-size: 11px;
            letter-spacing: 1px;
            align-self: center;
        }
        .profile-value {
            color: var(--cgv-dark);
            font-weight: 500;
        }
    </style>
</head>
<body class="cgv-body">

<%@ include file="_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Hồ sơ cá nhân</h1>
        <div class="cgv-header-right">
            <%@ include file="_notifications.jsp" %>
        </div>
    </header>

    <div class="cgv-page">
        <div class="profile-card">
            <div class="profile-header">
                <div class="profile-avatar-large">EM</div>
                <div class="profile-title">
                    <h2>${sessionScope.account.fullName}</h2>
                    <span>${sessionScope.account.roleName} — CGV Cinema Staff</span>
                </div>
            </div>

            <div class="profile-details-grid">
                <div class="profile-label">Email</div>
                <div class="profile-value">${sessionScope.account.email}</div>

                <div class="profile-label">Họ và Tên</div>
                <div class="profile-value">${sessionScope.account.fullName}</div>

                <div class="profile-label">Số điện thoại</div>
                <div class="profile-value">${not empty sessionScope.account.phoneNumber ? sessionScope.account.phoneNumber : 'Chưa cập nhật'}</div>

                <div class="profile-label">Quyền hạn</div>
                <div class="profile-value">Nhân viên hệ thống</div>

                <div class="profile-label">Trạng thái</div>
                <div class="profile-value">
                    <span class="cgv-badge active">Đang hoạt động</span>
                </div>

                <div class="profile-label">Ngày tham gia</div>
                <div class="profile-value">${sessionScope.account.createdAt}</div>
            </div>
            
            <div style="margin-top:32px; padding-top:24px; border-top:1px solid var(--cgv-border); display:flex; justify-content:flex-end;">
                <a href="${pageContext.request.contextPath}/change-password" class="btn--cgv">Đổi mật khẩu tài khoản</a>
            </div>
        </div>
    </div>
</div>
</body>
</html>
