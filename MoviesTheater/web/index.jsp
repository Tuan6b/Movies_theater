<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>CGV Cinema — Đặt vé xem phim trực tuyến</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
</head>
<body>

<!-- Header -->
<header class="site-header">
    <a href="${pageContext.request.contextPath}/" class="site-logo">
        <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV Cinema">
        <span class="site-logo-text">CGV Cinema</span>
    </a>

    <nav class="site-nav">
        <a href="${pageContext.request.contextPath}/" class="active">Trang chủ</a>
        <a href="#">Phim đang chiếu</a>
        <a href="#">Lịch chiếu</a>
        <a href="#">Ưu đãi</a>
    </nav>

    <div class="site-header-actions">
        <a href="#" class="btn btn-ghost">Đăng nhập</a>
        <a href="#" class="btn btn-primary">Đăng ký</a>
    </div>
</header>

<!-- Hero -->
<section class="hero fade-up">
    <div class="hero-eyebrow">CGV Cinema — Hệ thống rạp chiếu phim</div>
    <h1 class="hero-title">Đặt vé xem phim<br>trực tuyến dễ dàng</h1>
    <p class="hero-sub">Chọn phim yêu thích, đặt chỗ ngay hôm nay và tận hưởng trải nghiệm điện ảnh đỉnh cao.</p>
    <div class="hero-actions">
        <a href="#" class="btn btn-primary lg">Xem phim ngay</a>
        <a href="#" class="btn btn-outline lg" style="color:#fff; border-color:rgba(255,255,255,0.4);">Lịch chiếu hôm nay</a>
    </div>
</section>

<!-- Phim đang chiếu -->
<section class="section">
    <div class="section-header">
        <h2 class="section-title">Phim đang chiếu</h2>
        <a href="#" class="section-link">Xem tất cả →</a>
    </div>
    <div class="movie-grid">

        <a href="#" class="movie-card">
            <div class="movie-poster">Poster phim</div>
            <div class="movie-info">
                <div class="movie-title">Tên phim 1</div>
                <div class="movie-meta">110 phút · Hành động</div>
                <span class="movie-badge">2D</span>
            </div>
        </a>

        <a href="#" class="movie-card">
            <div class="movie-poster">Poster phim</div>
            <div class="movie-info">
                <div class="movie-title">Tên phim 2</div>
                <div class="movie-meta">125 phút · Phiêu lưu</div>
                <span class="movie-badge">IMAX</span>
            </div>
        </a>

        <a href="#" class="movie-card">
            <div class="movie-poster">Poster phim</div>
            <div class="movie-info">
                <div class="movie-title">Tên phim 3</div>
                <div class="movie-meta">95 phút · Hài</div>
                <span class="movie-badge">2D</span>
            </div>
        </a>

        <a href="#" class="movie-card">
            <div class="movie-poster">Poster phim</div>
            <div class="movie-info">
                <div class="movie-title">Tên phim 4</div>
                <div class="movie-meta">138 phút · Kinh dị</div>
                <span class="movie-badge">3D</span>
            </div>
        </a>

        <a href="#" class="movie-card">
            <div class="movie-poster">Poster phim</div>
            <div class="movie-info">
                <div class="movie-title">Tên phim 5</div>
                <div class="movie-meta">102 phút · Tâm lý</div>
                <span class="movie-badge">2D</span>
            </div>
        </a>

    </div>
</section>

<!-- Phim sắp chiếu -->
<section class="section" style="padding-top:0;">
    <div class="section-header">
        <h2 class="section-title">Phim sắp chiếu</h2>
        <a href="#" class="section-link">Xem tất cả →</a>
    </div>
    <div class="movie-grid">

        <a href="#" class="movie-card">
            <div class="movie-poster">Poster phim</div>
            <div class="movie-info">
                <div class="movie-title">Tên phim 6</div>
                <div class="movie-meta">Khởi chiếu 01/06/2026</div>
                <span class="movie-badge">IMAX</span>
            </div>
        </a>

        <a href="#" class="movie-card">
            <div class="movie-poster">Poster phim</div>
            <div class="movie-info">
                <div class="movie-title">Tên phim 7</div>
                <div class="movie-meta">Khởi chiếu 15/06/2026</div>
                <span class="movie-badge">4DX</span>
            </div>
        </a>

        <a href="#" class="movie-card">
            <div class="movie-poster">Poster phim</div>
            <div class="movie-info">
                <div class="movie-title">Tên phim 8</div>
                <div class="movie-meta">Khởi chiếu 20/06/2026</div>
                <span class="movie-badge">3D</span>
            </div>
        </a>

    </div>
</section>

<!-- Footer -->
<footer class="site-footer">
    <p>&copy; 2026 CGV Cinema. Hệ thống quản lý rạp chiếu phim.</p>
    <p style="margin-top:6px;">
        <a href="${pageContext.request.contextPath}/manager">Quản lý</a>
    </p>
</footer>

</body>
</html>
