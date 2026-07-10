<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cinema.dao.tbMovie" %>
<%@ page import="com.cinema.model.clsMovie" %>
<%@ page import="java.util.List" %>
<%
    tbMovie movieDAO = new tbMovie();
    List<clsMovie> activeMovies = movieDAO.getMoviesByFilter("showing");
    List<clsMovie> upcomingMovies = movieDAO.getMoviesByFilter("upcoming");
    int firstMovieId = (activeMovies != null && !activeMovies.isEmpty()) ? activeMovies.get(0).getMovieId() : 1;
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
    <div class="site-header-inner">
        <a href="${pageContext.request.contextPath}/" class="site-logo">
            <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
            <span class="site-logo-text">CGV CINEMA</span>
        </a>

        <nav class="site-nav">
            <a href="${pageContext.request.contextPath}/" class="active">Trang chủ</a>
            <a href="#">Phim đang chiếu</a>
            <a href="#">Ưu đãi</a>
            <a href="#">Góc điện ảnh</a>
        </nav>

        <div class="site-header-actions">
            <c:choose>
                <c:when test="${not empty sessionScope.account}">
                    <span style="font-size:13px;color:var(--cgv-text-muted);font-weight:500;">
                        Xin chào, <strong>${sessionScope.account.fullName}</strong>
                    </span>
                    <a href="${pageContext.request.contextPath}/Logout" class="btn btn-ghost" style="margin-right: 8px;">Đăng xuất</a>
                    <c:choose>
                        <c:when test="${sessionScope.account.roleId == 5}">
                            <a href="${pageContext.request.contextPath}/admin" class="btn btn-primary">Trang Admin</a>
                        </c:when>
                        <c:when test="${sessionScope.account.roleId == 4}">
                            <a href="${pageContext.request.contextPath}/manager" class="btn btn-primary">Trang Quản Lý</a>
                        </c:when>
                        <c:when test="${sessionScope.account.roleId == 3}">
                            <a href="${pageContext.request.contextPath}/employee" class="btn btn-primary">Trang Nhân Viên</a>
                        </c:when>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/Login" class="btn btn-ghost">Đăng nhập</a>
                    <a href="${pageContext.request.contextPath}/Register" class="btn btn-primary">Đăng ký</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>

<!-- Hero -->
<section class="hero fade-up" style="background-image: url('${pageContext.request.contextPath}/Image/Hero/cgv-trang-tien-plaza.png'); background-size: cover; background-position: center; position: relative;">
    <div style="position:absolute;inset:0;background:linear-gradient(155deg,rgba(5,0,0,0.90) 0%,rgba(30,0,0,0.82) 45%,rgba(130,0,0,0.68) 100%);pointer-events:none;z-index:0;"></div>
    <div class="hero-inner" style="position:relative;z-index:1;">
        <div class="hero-eyebrow">Hệ thống rạp chiếu phim hàng đầu Việt Nam</div>
        <h1 class="hero-title">Trải nghiệm<br>điện ảnh<br>đỉnh cao</h1>
        <p class="hero-sub">Đặt vé ngay hôm nay — chọn phim, chọn ghế, thanh toán trong 60 giây.</p>
<div class="hero-actions">
            <a href="${pageContext.request.contextPath}/showtimes?movieId=<%= firstMovieId %>"
               class="btn btn-primary btn-lg">Đặt vé ngay</a>
           
        </div>

        <div class="hero-stats">
            <div class="hero-stat">
                <div class="hero-stat-num">50+</div>
                <div class="hero-stat-label">Phim đang chiếu</div>
            </div>
            <div class="hero-stat-divider"></div>
            <div class="hero-stat">
                <div class="hero-stat-num">IMAX</div>
                <div class="hero-stat-label">Màn hình khổng lồ</div>
            </div>
            <div class="hero-stat-divider"></div>
            <div class="hero-stat">
                <div class="hero-stat-num">4DX</div>
                <div class="hero-stat-label">Trải nghiệm 4 chiều</div>
            </div>
            <div class="hero-stat-divider"></div>
            <div class="hero-stat">
                <div class="hero-stat-num">24/7</div>
                <div class="hero-stat-label">Đặt vé online</div>
            </div>
        </div>
    </div>
</section>

<!-- Phim đang chiếu -->
<section class="section">
    <div class="section-inner">
        <div class="section-eyebrow">Đang chiếu rạp</div>
        <div class="section-header">
            <h2 class="section-title">Phim đang chiếu</h2>
            <a href="#" class="section-link">Xem tất cả →</a>
        </div>
        <div class="movie-grid">
            <%
                if (activeMovies != null && !activeMovies.isEmpty()) {
                    for (clsMovie movie : activeMovies) {
            %>
            <a href="${pageContext.request.contextPath}/MovieDetailController?id=<%= movie.getMovieId() %>" class="movie-card">
                <div class="movie-poster">
                    <% if (movie.getPoster() != null && !movie.getPoster().trim().isEmpty()) { %>
                        <img src="<%= movie.getPoster() %>" alt="<%= movie.getMovieName() %>">
                    <% } else { %>
                        <span><%= movie.getMovieName() %></span>
                    <% } %>
                    <div class="movie-overlay">
                        <span class="movie-overlay-text">Đặt vé</span>
                    </div>
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
            <p style="color:var(--cgv-text-dim);padding:40px 0;">Không có phim nào đang chiếu.</p>
            <%
                }
%>
        </div>
    </div>
</section>

<!-- Phim sắp chiếu -->
<section class="section">
    <div class="section-inner">
        <div class="section-eyebrow">Sắp ra mắt</div>
        <div class="section-header">
            <h2 class="section-title">Phim sắp chiếu</h2>
            <a href="#" class="section-link">Xem tất cả →</a>
        </div>
        <div class="movie-grid">
            <%
                if (upcomingMovies != null && !upcomingMovies.isEmpty()) {
                    for (clsMovie movie : upcomingMovies) {
            %>
            <a href="${pageContext.request.contextPath}/MovieDetailController?id=<%= movie.getMovieId() %>" class="movie-card">
                <div class="movie-poster">
                    <% if (movie.getPoster() != null && !movie.getPoster().trim().isEmpty()) { %>
                        <img src="<%= movie.getPoster() %>" alt="<%= movie.getMovieName() %>">
                    <% } else { %>
                        <span><%= movie.getMovieName() %></span>
                    <% } %>
                    <div class="movie-overlay">
                        <span class="movie-overlay-text">Nhắc tôi</span>
                    </div>
                </div>
                <div class="movie-info">
                    <div class="movie-title"><%= movie.getMovieName() %></div>
                    <div class="movie-meta">Khởi chiếu sắp tới</div>
                    <span class="movie-badge"><%= movie.getAgeRestriction() > 0 ? "C" + movie.getAgeRestriction() : "P" %></span>
                </div>
            </a>
            <%
                    }
                } else {
            %>
            <p style="color:var(--cgv-text-dim);padding:40px 0;">Không có phim nào sắp chiếu.</p>
            <%
                }
            %>
        </div>
    </div>
</section>

<!-- Tại sao chọn CGV -->
<section class="experience-strip">
    <div class="section-inner">
        <div class="section-eyebrow">Tại sao chọn CGV?</div>
        <h2 class="section-title">Trải nghiệm khác biệt</h2>
        <div class="experience-grid">
            <div class="experience-card">
                <div class="experience-icon">
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="2" y="3" width="20" height="14" rx="2"/><polyline points="8 21 12 17 16 21"/>
                    </svg>
                </div>
                <h3>Màn hình IMAX</h3>
                <p>Công nghệ chiếu hình IMAX mang lại hình ảnh sắc nét chưa từng có.</p>
            </div>
            <div class="experience-card">
                <div class="experience-icon">
<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2z"/><path d="M12 8v4l3 3"/>
                    </svg>
                </div>
                <h3>Đặt vé 24/7</h3>
                <p>Chọn phim, chọn ghế và thanh toán mọi lúc mọi nơi chỉ trong vài giây.</p>
            </div>
            <div class="experience-card">
                <div class="experience-icon">
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                    </svg>
                </div>
                <h3>Ghế VIP cao cấp</h3>
                <p>Ghế recliner êm ái, không gian riêng tư tuyệt đối cho trải nghiệm VIP.</p>
            </div>
            <div class="experience-card">
                <div class="experience-icon">
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                    </svg>
                </div>
                <h3>4DX sống động</h3>
                <p>Cảm nhận phim bằng toàn thân — ghế chuyển động, gió, nước và ánh sáng.</p>
            </div>
        </div>
    </div>
</section>

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
