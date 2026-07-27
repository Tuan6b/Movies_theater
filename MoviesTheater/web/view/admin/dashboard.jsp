<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "dashboard"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Trang chính — CGV System Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .sa-card {
            background: #fff;
            border: 1px solid var(--cgv-border);
            border-radius: 12px;
            padding: 24px;
            display: flex;
            flex-direction: column;
        }
        .sa-card-title {
            font-weight: 700;
            font-family: var(--font-cgv-ui);
            margin-bottom: 4px;
        }
        .sa-card-desc {
            font-size: 13px;
            color: rgba(94,63,58,0.6);
            margin-bottom: 16px;
            flex: 1;
        }
        .sa-heading {
            font-family: var(--font-cgv-ui);
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 2px;
            text-transform: uppercase;
            color: rgba(94,63,58,0.5);
            margin: 0 0 12px 0;
        }
        .sa-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 16px;
        }
        @media (max-width: 900px) { .sa-grid { grid-template-columns: 1fr; } }
    </style>
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

            <div class="sa-heading">CHỨC NĂNG</div>
            <div class="sa-grid">

                <div class="sa-card">
                    <div class="sa-card-title">Nhật ký hệ thống</div>
                    <div class="sa-card-desc">Tra cứu toàn bộ thao tác đã thực hiện trên hệ thống, lọc theo loại thao tác hoặc từ khoá</div>
                    <a href="${pageContext.request.contextPath}/admin/logs" class="btn--cgv-outline">Xem nhật ký</a>
                </div>

                <div class="sa-card">
                    <div class="sa-card-title">Đổi mật khẩu</div>
                    <div class="sa-card-desc">Cập nhật mật khẩu đăng nhập của bạn</div>
                    <a href="${pageContext.request.contextPath}/change-password" class="btn--cgv-outline">Đổi mật khẩu</a>
                </div>

            </div>

        </div>
    </div>
</div>
</body>
</html>
