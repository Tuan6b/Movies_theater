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

            <%-- Account management --%>
            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.4);margin-bottom:16px;">QUẢN LÝ TÀI KHOẢN</div>
            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin-bottom:32px;">

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Tài khoản người dùng</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Xem, khoá và mở khoá tất cả tài khoản trong hệ thống</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/admin/users" class="btn--cgv-outline">Danh sách</a>
                    </div>
                </div>

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Nhân viên &amp; Phân quyền</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Tạo tài khoản nhân viên, quản lý vai trò và quyền truy cập</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/admin/staff" class="btn--cgv-outline">Danh sách</a>
                        <a href="${pageContext.request.contextPath}/admin/staff?action=add" class="btn--cgv">+ Thêm mới</a>
                    </div>
                </div>

            </div>

            <%-- System --%>
            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.4);margin-bottom:16px;">HỆ THỐNG</div>
            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;">

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Nhật ký hệ thống</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Xem nhật ký hoạt động và kiểm toán của toàn hệ thống</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/admin/logs" class="btn--cgv-outline">Xem nhật ký</a>
                    </div>
                </div>

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Cấu hình hệ thống</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Thiết lập thông số vận hành và cấu hình ứng dụng</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/admin/config" class="btn--cgv-outline">Cấu hình</a>
                    </div>
                </div>

            </div>

        </div>
    </div>
</div>
</body>
</html>
