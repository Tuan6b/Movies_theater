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
                    <c:if test="${not empty sessionScope.account && sessionScope.account.roleId == 2}">
                        <a href="${pageContext.request.contextPath}/my-tickets">Vé của tôi</a>
                    </c:if>
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

        <c:if test="${empty searchKeyword}">
            <section class="hero fade-up" style="background-image: url('${pageContext.request.contextPath}/Image/Hero/cgv-trang-tien-plaza.png'); background-size: cover; background-position: center; position: relative;">
                <div style="position:absolute;inset:0;background:linear-gradient(155deg,rgba(5,0,0,0.90) 0%,rgba(30,0,0,0.82) 45%,rgba(130,0,0,0.68) 100%);pointer-events:none;z-index:0;"></div>
                <div class="hero-inner" style="position:relative;z-index:1;">
                    <div class="hero-eyebrow">Hệ thống rạp chiếu phim hàng đầu Việt Nam</div>
                    <h1 class="hero-title">Trải nghiệm<br>điện ảnh<br>đỉnh cao</h1>
                    <p class="hero-sub">Đặt vé ngay hôm nay — chọn phim, chọn ghế, thanh toán trong 60 giây.</p>
                    <div class="hero-actions">
                        <a href="${pageContext.request.contextPath}/showtimes?movieId=${not empty showingList ? showingList[0].movieId : 1}" class="btn btn-primary btn-lg">Đặt vé ngay</a>
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

                    <div style="position: relative;">
                        <form action="${pageContext.request.contextPath}/HomeController" method="GET" style="display: flex; gap: 8px;">
                            <input type="hidden" name="genreShowing" value="${genreShowing}">
                            <input type="hidden" name="genreUpcoming" value="${genreUpcoming}">
                            <input type="text" id="liveSearchInput" name="search" value="${searchKeyword}" placeholder="Tìm tên phim..."
                                   style="padding: 8px 16px; border: 1px solid var(--cgv-border); border-radius: 99px; outline: none; font-family: var(--font-body); width: 250px;">
                            <button type="submit" class="btn btn-primary" style="border-radius: 99px; padding: 0 16px;">
                                <i class="fa-solid fa-magnifying-glass"></i>
                            </button>
                            <c:if test="${not empty searchKeyword}">
                                <a href="${pageContext.request.contextPath}/HomeController?genreShowing=${genreShowing}&genreUpcoming=${genreUpcoming}" class="btn btn-ghost">Xóa lọc</a>
                            </c:if>
                        </form>
                        <div id="liveSearchResults" style="display: none; position: absolute; top: 100%; left: 0; right: 0; background: #fff; border: 1px solid #ccc; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); z-index: 1000; margin-top: 5px; max-height: 300px; overflow-y: auto;"></div>
                    </div>
                </div>

                <!-- update: Showing and Upcoming sections -->
                <!-- ================= PHẦN PHIM ĐANG CHIẾU ================= -->
                <section class="section" style="padding-top: 0;">
                    <div class="section-inner" style="padding: 0;">
                        <div class="section-eyebrow">Đang chiếu rạp</div>
                        <div class="section-header" style="display: flex; justify-content: space-between; align-items: flex-end; padding-bottom: 12px; margin-bottom: 32px;">
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
                        <div class="section-eyebrow">Sắp ra mắt</div>
                        <div class="section-header" style="display: flex; justify-content: space-between; align-items: flex-end; padding-bottom: 12px; margin-bottom: 32px;">
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

        <script>
            const searchInput = document.getElementById('liveSearchInput');
            const searchResults = document.getElementById('liveSearchResults');
            let debounceTimer;

            searchInput.addEventListener('input', function () {
                clearTimeout(debounceTimer);
                const keyword = this.value.trim();

                if (keyword.length < 2) {
                    searchResults.style.display = 'none';
                    return;
                }

                // Kỹ thuật Debounce (Đợi người dùng ngừng gõ 300ms mới gọi DB)
                debounceTimer = setTimeout(() => {
   
                    const allMovies = document.querySelectorAll('.movie-title');
                    let matches = [];

                    allMovies.forEach(movie => {
                        const title = movie.getAttribute('title');
                        if (title && title.toLowerCase().includes(keyword.toLowerCase())) {
                            matches.push({
                                title: title,
                                url: movie.closest('a').getAttribute('href')
                            });
                        }
                    });

                    if (matches.length > 0) {
                        let html = '<ul style="list-style: none; padding: 0; margin: 0;">';
                        matches.forEach(m => {
                            html += `<li style="border-bottom: 1px solid #f0f0f0;">
                                        <a href="` + m.url + `" style="display: block; padding: 10px 15px; color: #333; text-decoration: none;">
                                            <i class="fa-solid fa-film" style="margin-right: 8px; color: #888;"></i> ` + m.title + `
                                        </a>
                                     </li>`;
                        });
                        html += '</ul>';
                        searchResults.innerHTML = html;
                        searchResults.style.display = 'block';
                    } else {
                        searchResults.innerHTML = '<div style="padding: 10px 15px; color: #888;">Không tìm thấy phim phù hợp</div>';
                        searchResults.style.display = 'block';
                    }
                }, 300);
            });

            // Ẩn box tìm kiếm khi click ra ngoài
            document.addEventListener('click', function (e) {
                if (!searchInput.contains(e.target) && !searchResults.contains(e.target)) {
                    searchResults.style.display = 'none';
                }
            });
        </script>

    </body>
</html>