<%@taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>CGV Cinema — Trải nghiệm điện ảnh đỉnh cao</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    </head>
    <body class="fade-up">

        <header class="site-header">
            <div class="site-header-inner">
                <a href="${pageContext.request.contextPath}/HomeController" class="site-logo">
                    <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
                    <span class="site-logo-text">CGV CINEMA</span>
                </a>

                <nav class="site-nav">
                    <a href="${pageContext.request.contextPath}/HomeController?status=showing" class="${currentStatus == 'showing' ? 'active' : ''}">Phim Đang Chiếu</a>
                    <a href="${pageContext.request.contextPath}/HomeController?status=upcoming" class="${currentStatus == 'upcoming' ? 'active' : ''}">Phim Sắp Chiếu</a>
                    <a href="#">Rạp & Giá Vé</a>
                    <a href="#">Thành Viên</a>
                </nav>

                <div class="site-header-actions">
                    <c:choose>
                        <c:when test="${not empty sessionScope.account}">
                            <span style="font-size:13px;color:var(--cgv-text-muted);font-weight:500;">
                                Xin chào, <strong>${sessionScope.account.fullName}</strong>
                            </span>
                            <a href="${pageContext.request.contextPath}/Logout" class="btn btn-ghost" style="margin-right: 8px;">Đăng xuất</a>
                            <c:if test="${sessionScope.account.roleId >= 3}">
                                <a href="${pageContext.request.contextPath}/manager" class="btn btn-primary">Quản Lý</a>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/Login" class="btn btn-ghost">Đăng nhập</a>
                            <a href="${pageContext.request.contextPath}/Register" class="btn btn-primary">Đăng ký</a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </header>

        <c:if test="${empty searchKeyword}">
            <section class="hero">
                <div class="hero-inner">
                    <div class="hero-eyebrow">Hệ thống rạp chiếu phim số 1</div>
                    <h1 class="hero-title">Trải nghiệm<br>điện ảnh đỉnh cao</h1>
                    <p class="hero-sub">Đặt vé ngay hôm nay — chọn phim, chọn ghế, thanh toán trong 60 giây.</p>
                    <div class="hero-actions">
                        <a href="#movie-list" class="btn btn-primary btn-lg">Mua Vé Ngay</a>
                    </div>
                </div>
            </section>
        </c:if>

        <section class="section" id="movie-list">
            <div class="section-inner">

                <!-- Tiêu đề và Thanh tìm kiếm chung -->
                <div class="section-header" style="display: flex; align-items: flex-end; justify-content: space-between; border-bottom: 2px solid var(--cgv-red); padding-bottom: 12px; margin-bottom: 32px;">
                    <div>
                        <div class="section-eyebrow">Movie Selection</div>
                        <h2 class="section-title" style="margin: 0;">
                            DANH SÁCH PHIM
                            <c:if test="${not empty searchKeyword}">
                                <span style="font-size: 16px; text-transform: none; color: var(--cgv-text-muted);">
                                    - Kết quả tìm kiếm cho: "${searchKeyword}"
                                </span>
                            </c:if>
                        </h2>
                    </div>

                    <form action="${pageContext.request.contextPath}/HomeController" method="GET" style="display: flex; gap: 8px;">
                        <input type="hidden" name="genreShowing" value="${genreShowing}">
                        <input type="hidden" name="genreUpcoming" value="${genreUpcoming}">
                        <input type="text" name="search" value="${searchKeyword}" placeholder="Tìm tên phim..." 
                               style="padding: 8px 16px; border: 1px solid var(--cgv-border); border-radius: 99px; outline: none; font-family: var(--font-body);">
                        <button type="submit" class="btn btn-primary" style="border-radius: 99px; padding: 0 16px;">
                            <i class="fa-solid fa-magnifying-glass"></i>
                        </button>
                        <c:if test="${not empty searchKeyword}">
                            <a href="${pageContext.request.contextPath}/HomeController?genreShowing=${genreShowing}&genreUpcoming=${genreUpcoming}" class="btn btn-ghost">Xóa lọc</a>
                        </c:if>
                    </form>
                </div>

                <!-- update: Showing and Upcoming sections -->
                <!-- ================= PHẦN PHIM ĐANG CHIẾU ================= -->
                <section class="section" style="padding-top: 0;">
                    <div class="section-inner" style="padding: 0;">
                        <div class="section-header" style="display: flex; justify-content: space-between; align-items: flex-end; border-bottom: 2px solid var(--cgv-red); padding-bottom: 12px; margin-bottom: 32px;">
                            <h2 class="section-title" style="margin: 0; color: var(--cgv-red);">PHIM ĐANG CHIẾU</h2>
                            <form action="${pageContext.request.contextPath}/HomeController" method="GET" id="genreShowingForm">
                                <label for="genreShowing" style="font-weight: bold; margin-right: 10px;">Lọc thể loại:</label>
                                <select name="genreShowing" id="genreShowing" onchange="document.getElementById('genreShowingForm').submit()" 
                                        style="padding: 6px 12px; border: 2px solid var(--cgv-red); border-radius: 4px; font-size: 14px;">
                                    <option value="">Tất cả</option>
                                    <c:forEach items="${genreList}" var="g">
                                        <option value="${g.genreID}" ${genreShowing == g.genreID ? 'selected' : ''}>${g.genreName}</option>
                                    </c:forEach>
                                </select>
                                <input type="hidden" name="search" value="${searchKeyword}">
                                <input type="hidden" name="pageShowing" value="${pageShowing}">
                                <input type="hidden" name="pageUpcoming" value="${pageUpcoming}">
                                <input type="hidden" name="genreUpcoming" value="${genreUpcoming}">
                            </form>
                        </div>

                        <div class="movie-grid">
                            <c:forEach items="${showingList}" var="m">
                                <a href="${pageContext.request.contextPath}/MovieDetailController?id=${m.movieId}" class="movie-card">
                                    <div class="movie-poster">
                                        <img src="${m.poster}" alt="${m.movieName}">
                                        <div class="movie-overlay"><span class="movie-overlay-text">Mua Vé Ngay</span></div>
                                    </div>
                                    <div class="movie-info">
                                        <div class="movie-title" title="${m.movieName}">${m.movieName}</div>
                                        <div class="movie-meta"><i class="fa-regular fa-clock"></i> ${m.duration} phút</div>
                                        <span class="movie-badge">
                                            <c:choose>
                                                <c:when test="${m.ageRestriction > 0}">C${m.ageRestriction}</c:when>
                                                <c:otherwise>P</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                </a>
                            </c:forEach>
                        </div>
                        
                        <c:if test="${empty showingList}">
                            <div style="text-align: center; padding: 40px 0; color: var(--cgv-text-muted);">
                                <i class="fa-solid fa-film" style="font-size: 48px; opacity: 0.3; margin-bottom: 16px;"></i>
                                <h3>Không có phim đang chiếu nào!</h3>
                            </div>
                        </c:if>

                        <!-- PHÂN TRANG: ĐANG CHIẾU -->
                        <c:if test="${totalPagesShowing > 1}">
                            <div style="display: flex; justify-content: center; margin-top: 32px; gap: 8px;">
                                <c:forEach begin="1" end="${totalPagesShowing}" var="i">
                                    <a href="HomeController?pageShowing=${i}&pageUpcoming=${pageUpcoming}&genreShowing=${genreShowing}&genreUpcoming=${genreUpcoming}&search=${searchKeyword}" 
                                       class="btn ${pageShowing == i ? 'btn-primary' : 'btn-ghost'}" 
                                       style="width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; border: 1px solid var(--cgv-border);">
                                        ${i}
                                    </a>
                                </c:forEach>
                            </div>
                        </c:if>
                    </div>
                </section>

                <!-- ================= PHẦN PHIM SẮP CHIẾU ================= -->
                <section class="section" style="padding-top: 40px;">
                    <div class="section-inner" style="padding: 0;">
                        <div class="section-header" style="display: flex; justify-content: space-between; align-items: flex-end; border-bottom: 2px solid var(--cgv-red); padding-bottom: 12px; margin-bottom: 32px;">
                            <h2 class="section-title" style="margin: 0; color: var(--cgv-red);">PHIM SẮP CHIẾU</h2>
                            <form action="${pageContext.request.contextPath}/HomeController" method="GET" id="genreUpcomingForm">
                                <label for="genreUpcoming" style="font-weight: bold; margin-right: 10px;">Lọc thể loại:</label>
                                <select name="genreUpcoming" id="genreUpcoming" onchange="document.getElementById('genreUpcomingForm').submit()" 
                                        style="padding: 6px 12px; border: 2px solid var(--cgv-red); border-radius: 4px; font-size: 14px;">
                                    <option value="">Tất cả</option>
                                    <c:forEach items="${genreList}" var="g">
                                        <option value="${g.genreID}" ${genreUpcoming == g.genreID ? 'selected' : ''}>${g.genreName}</option>
                                    </c:forEach>
                                </select>
                                <input type="hidden" name="search" value="${searchKeyword}">
                                <input type="hidden" name="pageShowing" value="${pageShowing}">
                                <input type="hidden" name="pageUpcoming" value="${pageUpcoming}">
                                <input type="hidden" name="genreShowing" value="${genreShowing}">
                            </form>
                        </div>

                        <div class="movie-grid">
                            <c:forEach items="${upcomingList}" var="m">
                                <a href="${pageContext.request.contextPath}/MovieDetailController?id=${m.movieId}" class="movie-card">
                                    <div class="movie-poster">
                                        <img src="${m.poster}" alt="${m.movieName}">
                                        <div class="movie-overlay"><span class="movie-overlay-text">Xem Chi Tiết</span></div>
                                    </div>
                                    <div class="movie-info">
                                        <div class="movie-title" title="${m.movieName}">${m.movieName}</div>
                                        <div class="movie-meta"><i class="fa-regular fa-clock"></i> ${m.duration} phút</div>
                                        <span class="movie-badge">
                                            <c:choose>
                                                <c:when test="${m.ageRestriction > 0}">C${m.ageRestriction}</c:when>
                                                <c:otherwise>P</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                </a>
                            </c:forEach>
                        </div>
                        
                        <c:if test="${empty upcomingList}">
                            <div style="text-align: center; padding: 40px 0; color: var(--cgv-text-muted);">
                                <i class="fa-solid fa-film" style="font-size: 48px; opacity: 0.3; margin-bottom: 16px;"></i>
                                <h3>Không có phim sắp chiếu nào!</h3>
                            </div>
                        </c:if>

                        <!-- PHÂN TRANG: SẮP CHIẾU -->
                        <c:if test="${totalPagesUpcoming > 1}">
                            <div style="display: flex; justify-content: center; margin-top: 32px; gap: 8px;">
                                <c:forEach begin="1" end="${totalPagesUpcoming}" var="i">
                                    <a href="HomeController?pageShowing=${pageShowing}&pageUpcoming=${i}&genreShowing=${genreShowing}&genreUpcoming=${genreUpcoming}&search=${searchKeyword}" 
                                       class="btn ${pageUpcoming == i ? 'btn-primary' : 'btn-ghost'}" 
                                       style="width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; border: 1px solid var(--cgv-border);">
                                        ${i}
                                    </a>
                                </c:forEach>
                            </div>
                        </c:if>
                    </div>
                </section>
                <!-- end update code -->

            </div>
        </section>

        <footer class="site-footer">
            <div class="footer-inner">
                <a href="#" class="footer-brand">
                    <span class="footer-brand-text">CGV CINEMA VIETNAM</span>
                </a>
                <p class="footer-copy">&copy; 2026 CGV. Demo Website Booking.</p>
                <div class="footer-links">
                    <a href="#">Điều khoản</a>
                    <a href="#">Chính sách</a>
                </div>
            </div>
        </footer>

    </body>
</html>