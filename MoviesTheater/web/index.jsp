<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.cinema.dao.tbMovie" %>
<%@ page import="com.cinema.model.clsMovie" %>
<%@ page import="java.util.List" %>
<%
    tbMovie movieDAO = new tbMovie();
    List<clsMovie> activeMovies = movieDAO.getAllActiveMovies();
%>
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
        <a href="${pageContext.request.contextPath}/showtimes?movieId=<%= (activeMovies != null && !activeMovies.isEmpty()) ? activeMovies.get(0).getMovieId() : 1 %>">Lịch chiếu</a>
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
        <a href="${pageContext.request.contextPath}/showtimes?movieId=<%= (activeMovies != null && !activeMovies.isEmpty()) ? activeMovies.get(0).getMovieId() : 1 %>" class="btn btn-primary lg">Xem phim ngay</a>
        <a href="${pageContext.request.contextPath}/showtimes?movieId=<%= (activeMovies != null && !activeMovies.isEmpty()) ? activeMovies.get(0).getMovieId() : 1 %>" class="btn btn-outline lg" style="color:#fff; border-color:rgba(255,255,255,0.4);">Lịch chiếu hôm nay</a>
    </div>
</section>

<!-- Phim đang chiếu -->
<section class="section">
    <div class="section-header">
        <h2 class="section-title">Phim đang chiếu</h2>
        <a href="#" class="section-link">Xem tất cả →</a>
    </div>
    <div class="movie-grid">
        <%
            if (activeMovies != null && !activeMovies.isEmpty()) {
                for (clsMovie movie : activeMovies) {
        %>
        <a href="${pageContext.request.contextPath}/showtimes?movieId=<%= movie.getMovieId() %>" class="movie-card">
            <div class="movie-poster">
                <% if (movie.getPoster() != null && !movie.getPoster().trim().isEmpty()) { %>
                    <img src="<%= movie.getPoster() %>" alt="<%= movie.getMovieName() %>" style="width:100%; height:100%; object-fit:cover; border-radius:12px;">
                <% } else { %>
                    <div style="padding: 20px; font-weight: bold; color: #fff;"><%= movie.getMovieName() %></div>
                <% } %>
            </div>
            <div class="movie-info">
                <div class="movie-title"><%= movie.getMovieName() %></div>
                <div class="movie-meta"><%= movie.getDuration() %> phút · <%= movie.getLanguage() %></div>
                <span class="movie-badge"><%= movie.getAgeRestriction() > 0 ? "C" + movie.getAgeRestriction() : "P" %></span>
            </div>
        </a>
        <%
                }
            } else {
        %>
        <p>Không có phim nào đang chiếu.</p>
        <%
            }
        %>
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
