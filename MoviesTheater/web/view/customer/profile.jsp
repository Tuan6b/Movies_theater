<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Hồ sơ của tôi — CGV Cinema</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
</head>
<body>

<div class="top-dark">
    <div class="header-inner">
        <a href="${pageContext.request.contextPath}/" class="logo">CGV CINEMA</a>
        <div class="nav">
            <a href="${pageContext.request.contextPath}/">Trang chủ</a>
            <a href="${pageContext.request.contextPath}/showtimes">Lịch chiếu</a>
            <a href="${pageContext.request.contextPath}/profile">Hồ sơ</a>
            <a href="${pageContext.request.contextPath}/Logout">Đăng xuất</a>
        </div>
    </div>
</div>

<main class="profile-page">
    <div class="profile-container">
        <%@ include file="/view/common/profile-content.jsp" %>
    </div>
</main>

<footer class="footer-dark">
    <div class="footer-inner">
        <strong>CGV CINEMA</strong>
        <span>&copy; 2026 CGV Cinema. Hệ thống quản lý rạp chiếu phim</span>
    </div>
</footer>

</body>
</html>
