<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Thông tin tài khoản — CGV Cinema</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/profile.css">
    <style>
        .profile-page { padding: 40px 0; min-height: calc(100vh - 300px); }
        .profile-page .section-inner { max-width: 640px; margin: 0 auto; }
        .profile-btn { margin-top: 20px; }
        .profile-links { margin-top: 16px; font-size: 13px; color: var(--cgv-text-dim); }
        .profile-links a { color: var(--cgv-accent); text-decoration: none; }
        .profile-links a:hover { text-decoration: underline; }
        .profile-links .sep { margin: 0 8px; color: var(--cgv-border); }
    </style>
</head>
<body>

<!-- Header -->
<header class="site-header">
    <div class="site-header-inner">
        <a href="${pageContext.request.contextPath}/" class="site-logo">
            <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
            <span class="site-logo-text">CGV CINEMA</span>
        </a>

        <nav class="site-nav">
            <a href="${pageContext.request.contextPath}/">Trang chủ</a>
            <a href="#">Phim đang chiếu</a>
            <a href="#">Ưu đãi</a>
            <a href="#">Góc điện ảnh</a>
        </nav>

        <div class="site-header-actions">
            <c:choose>
                <c:when test="${not empty sessionScope.account}">
                    <a href="${pageContext.request.contextPath}/profile" style="font-size:13px;color:var(--cgv-text-muted);font-weight:500;text-decoration:none;">
                        Xin chào, <strong>${sessionScope.account.profile.fullName}</strong>
                    </a>
                    <a href="${pageContext.request.contextPath}/Logout" class="btn btn-ghost">Đăng xuất</a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/Login" class="btn btn-ghost">Đăng nhập</a>
                    <a href="${pageContext.request.contextPath}/Register" class="btn btn-primary">Đăng ký</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>

<!-- Profile Content -->
<section class="profile-page">
    <div class="section-inner">
        <div class="profile-card">
            <h2 class="auth-title">Thông tin tài khoản</h2>

            <c:if test="${not empty error}">
                <div class="auth-error">${error}</div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="auth-success">${success}</div>
            </c:if>

            <table class="profile-table">
                <tr><td class="pf-label">Họ tên</td><td class="pf-value">${sessionScope.account.profile.fullName}</td></tr>
                <tr><td class="pf-label">Email</td><td class="pf-value">${sessionScope.account.email}</td></tr>
                <tr><td class="pf-label">Số điện thoại</td><td class="pf-value">${sessionScope.account.profile.phoneNumber}</td></tr>
                <tr><td class="pf-label">Ngày sinh</td><td class="pf-value">${sessionScope.account.profile.dob}</td></tr>
                <tr><td class="pf-label">Địa chỉ</td><td class="pf-value">${sessionScope.account.profile.address}</td></tr>
                <tr><td class="pf-label">Vai trò</td><td class="pf-value">${sessionScope.account.roleName}</td></tr>
                <tr><td class="pf-label">Trạng thái</td><td class="pf-value">
                    <c:choose>
                        <c:when test="${sessionScope.account.isBlocked}">Đã khóa</c:when>
                        <c:otherwise>Hoạt động</c:otherwise>
                    </c:choose>
                </td></tr>
                <tr><td class="pf-label">Ngày tạo</td><td class="pf-value">${sessionScope.account.createdAt}</td></tr>
            </table>

            <button class="btn btn-primary profile-btn" onclick="openModal()">Chỉnh sửa thông tin</button>

            <p class="profile-links">
                <a href="${pageContext.request.contextPath}/change-password">Đổi mật khẩu</a>
                <span class="sep">&middot;</span>
                <a href="${pageContext.request.contextPath}/">Quay lại trang chủ</a>
            </p>
        </div>
    </div>
</section>

<!-- Edit Modal -->
<div id="editModal" class="modal-overlay" style="display:${showModal ? 'flex' : 'none'};">
    <div class="modal-content">
        <button class="modal-close" onclick="closeModal()">&times;</button>
        <h3 style="margin-bottom:20px;font-size:18px;">Chỉnh sửa thông tin</h3>
        <form action="${pageContext.request.contextPath}/profile" method="post" class="auth-form">
            <div class="auth-field">
                <label for="fullName">Họ tên</label>
                <input type="text" id="fullName" name="fullName"
                       value="<%= request.getAttribute("fullName") != null ? request.getAttribute("fullName") : "" %>"
                       placeholder="${sessionScope.account.profile.fullName}" required>
            </div>
            <div class="auth-field">
                <label for="phoneNumber">Số điện thoại</label>
                <input type="tel" id="phoneNumber" name="phoneNumber"
                       value="<%= request.getAttribute("phoneNumber") != null ? request.getAttribute("phoneNumber") : "" %>"
                       placeholder="${sessionScope.account.profile.phoneNumber}">
            </div>
            <div class="auth-field">
                <label for="dob">Ngày sinh</label>
                <input type="date" id="dob" name="dob"
                       value="<%= request.getAttribute("dob") != null ? request.getAttribute("dob") : "" %>">
            </div>
            <div class="auth-field">
                <label for="address">Địa chỉ</label>
                <input type="text" id="address" name="address"
                       value="<%= request.getAttribute("address") != null ? request.getAttribute("address") : "" %>"
                       placeholder="${sessionScope.account.profile.address}">
            </div>
            <button type="submit" class="btn btn-primary" style="width:100%;">Lưu thay đổi</button>
        </form>
    </div>
</div>

<script>
function openModal() { document.getElementById("editModal").style.display = "flex"; }
function closeModal() { document.getElementById("editModal").style.display = "none"; }
<c:if test="${showModal}">openModal();</c:if>
</script>

<!-- Footer -->
<footer class="site-footer">
    <div class="footer-inner">
        <a href="${pageContext.request.contextPath}/" class="footer-brand">
            <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
            <span class="footer-brand-text">CGV CINEMA</span>
        </a>
        <p class="footer-copy">&copy; 2026 CGV Cinema. Hệ thống quản lý rạp chiếu phim.</p>
        <div class="footer-links">
            <a href="${pageContext.request.contextPath}/manager">Quản lý</a>
            <a href="#">Điều khoản</a>
            <a href="#">Hỗ trợ</a>
        </div>
    </div>
</footer>

</body>
</html>
