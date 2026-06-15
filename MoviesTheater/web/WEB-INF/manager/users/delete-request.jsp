<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Yêu cầu xóa tài khoản — CGV Cinema</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/profile.css">
    <style>
        .del-req-page { padding: 60px 0; min-height: calc(100vh - 300px); }
        .del-req-page .section-inner { max-width: 520px; margin: 0 auto; }
        .del-warning { background:#fef2f2; border:1px solid #fecaca; border-radius:8px; padding:16px; margin-bottom:20px; font-size:13px; color:#b91c1c; }
        .del-warning strong { display:block; margin-bottom:4px; font-size:14px; }
        textarea.del-reason { width:100%; min-height:120px; padding:12px; border:1px solid var(--cgv-border); border-radius:8px; font-size:14px; resize:vertical; font-family:inherit; }
    </style>
</head>
<body>

<header class="site-header">
    <div class="site-header-inner">
        <a href="${pageContext.request.contextPath}/" class="site-logo">
            <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
            <span class="site-logo-text">CGV CINEMA</span>
        </a>
        <nav class="site-nav">
            <a href="${pageContext.request.contextPath}/">Trang chủ</a>
        </nav>
        <div class="site-header-actions">
            <a href="${pageContext.request.contextPath}/profile" style="font-size:13px;color:var(--cgv-text-muted);font-weight:500;text-decoration:none;">
                Xin chào, <strong>${sessionScope.account.profile.fullName}</strong>
            </a>
            <a href="${pageContext.request.contextPath}/Logout" class="btn btn-ghost">Đăng xuất</a>
        </div>
    </div>
</header>

<section class="del-req-page">
    <div class="section-inner">
        <div class="profile-card">
            <h2 class="auth-title">Yêu cầu xóa tài khoản</h2>

            <c:if test="${not empty error}">
                <div class="auth-error">${error}</div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="auth-success">${success}</div>
            </c:if>

            <c:choose>
                <c:when test="${hasPendingRequest}">
                    <div class="auth-success" style="margin-top:16px;">
                        Bạn đã gửi yêu cầu xóa tài khoản. Vui lòng chờ Admin xét duyệt.
                    </div>
                    <p style="margin-top:16px;text-align:center;">
                        <a href="${pageContext.request.contextPath}/profile" class="btn btn-ghost">Quay lại thông tin tài khoản</a>
                    </p>
                </c:when>
                <c:otherwise>
                    <div class="del-warning">
                        <strong>Cảnh báo!</strong>
                        Hành động này sẽ xóa vĩnh viễn tài khoản và toàn bộ dữ liệu liên quan. Sau khi Admin phê duyệt, tài khoản sẽ bị xóa và không thể khôi phục.
                    </div>

                    <form action="${pageContext.request.contextPath}/delete-request" method="post" class="auth-form">
                        <div class="auth-field">
                            <label for="reason">Lý do xóa tài khoản</label>
                            <textarea id="reason" name="reason" class="del-reason" placeholder="Vui lòng cho chúng tôi biết lý do bạn muốn xóa tài khoản..." required></textarea>
                        </div>
                        <button type="submit" class="btn btn-primary" style="width:100%;background:#b91c1c;" onclick="return confirm('Bạn có chắc chắn muốn yêu cầu xóa tài khoản? Hành động này không thể hoàn tác sau khi Admin phê duyệt.')">Gửi yêu cầu xóa</button>
                    </form>

                    <p class="profile-links" style="margin-top:16px;text-align:center;">
                        <a href="${pageContext.request.contextPath}/profile">Hủy, quay lại thông tin tài khoản</a>
                    </p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</section>

<footer class="site-footer">
    <div class="footer-inner">
        <a href="${pageContext.request.contextPath}/" class="footer-brand">
            <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
            <span class="footer-brand-text">CGV CINEMA</span>
        </a>
        <p class="footer-copy">&copy; 2026 CGV Cinema.</p>
    </div>
</footer>
</body>
</html>
