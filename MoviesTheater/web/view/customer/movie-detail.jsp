<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${movie.movieName} - CGV Cinemas</title>

        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">

        <style>
            .md-wrap {
                padding: 40px 0 100px;
            }
            .md-eyebrow {
                text-align: center;
                color: var(--cgv-red);
                font-size: 11px;
                font-weight: 700;
                letter-spacing: 2px;
                text-transform: uppercase;
                margin-bottom: 16px;
            }
            .md-title {
                font-family: var(--font-display);
                font-size: 64px;
                text-align: center;
                text-transform: uppercase;
                margin-bottom: 40px;
                letter-spacing: 1px;
                line-height: 1.1;
            }
            .md-hero-img {
                width: 100%;
                height: 500px;
                object-fit: cover;
                border-radius: var(--r-xl);
                box-shadow: 0 24px 48px rgba(94,63,58,0.15);
                margin-bottom: 40px;
            }

            .md-meta-bar {
                display: flex;
                justify-content: center;
                gap: 40px;
                font-size: 12px;
                font-weight: 700;
                color: var(--cgv-text-muted);
                text-transform: uppercase;
                letter-spacing: 1px;
                margin-bottom: 40px;
            }

            .md-actions {
                display: flex;
                justify-content: center;
                gap: 16px;
                margin-bottom: 80px;
            }
            .md-actions .btn {
                min-width: 160px;
                justify-content: center;
                height: 48px;
                font-size: 14px;
                border-radius: 100px;
            }
            .md-actions .btn-ghost {
                border: 1px solid var(--cgv-border);
                background: var(--cgv-surface);
            }
            .md-actions .btn-ghost:hover {
                background: var(--cgv-bg);
            }

            .md-section-title {
                font-family: var(--font-display);
                font-size: 28px;
                text-transform: uppercase;
                text-align: center;
                margin-bottom: 32px;
                letter-spacing: 1px;
            }

            .md-story-text {
                max-width: 760px;
                margin: 0 auto 60px;
                text-align: center;
                line-height: 1.8;
                color: var(--cgv-text-muted);
                font-size: 15px;
            }

            .md-crew-grid {
                display: flex;
                justify-content: center;
                gap: 80px;
                border-top: 1px solid var(--cgv-border);
                border-bottom: 1px solid var(--cgv-border);
                padding: 40px 0;
                margin-bottom: 80px;
                text-align: center;
            }
            .crew-label {
                display: block;
                font-size: 10px;
                color: var(--cgv-text-dim);
                text-transform: uppercase;
                letter-spacing: 2px;
                margin-bottom: 8px;
            }
            .crew-value {
                display: block;
                font-weight: 700;
                font-size: 14px;
            }

            .md-cast-header {
                display: flex;
                justify-content: space-between;
                align-items: baseline;
                margin-bottom: 32px;
            }
            .md-view-all {
                font-size: 11px;
                font-weight: 700;
                color: var(--cgv-red);
                text-transform: uppercase;
                letter-spacing: 1px;
            }
            .md-cast-grid {
                display: grid;
                grid-template-columns: repeat(4, 1fr);
                gap: 24px;
            }
            .cast-card {
                text-align: center;
            }
            .cast-img {
                width: 100%;
                height: 320px;
                object-fit: cover;
                border-radius: var(--r-md);
                margin-bottom: 16px;
                background-color: #fff;
                box-shadow: 0 4px 12px rgba(94,63,58,0.08);
            }
            .cast-name {
                font-weight: 700;
                font-size: 15px;
                margin-bottom: 4px;
            }
            .cast-role {
                font-size: 12px;
                color: var(--cgv-text-dim);
            }
        </style>
    </head>
    <body>

        <header class="site-header">
            <div class="site-header-inner">
                <a href="${pageContext.request.contextPath}/HomeController" class="site-logo">
                    <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
                    <span class="site-logo-text">CGV CINEMA</span>
                </a>

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

        <div class="site-inner md-wrap">
            <div class="md-eyebrow">
                PHIM ĐIỆN ẢNH &nbsp;|&nbsp; IMAX 2D
            </div>

            <h1 class="md-title">${movie.movieName}</h1>

            <c:choose>
                <c:when test="${not empty movie.trailer}">
                    <c:set var="youtubeId" value="${fn:substringAfter(movie.trailer, 'v=')}" />
                    <c:if test="${fn:contains(youtubeId, '&')}">
                        <c:set var="youtubeId" value="${fn:substringBefore(youtubeId, '&')}" />
                    </c:if>
                    <iframe class="md-hero-img" src="https://www.youtube.com/embed/${youtubeId}?rel=0" frameborder="0" allowfullscreen></iframe>
                    </c:when>
                    <c:otherwise>
                    <img src="${movie.poster}" alt="Cover" class="md-hero-img">
                </c:otherwise>
            </c:choose>

            <div class="md-meta-bar">
                <span>${movie.duration} PHÚT</span>
                <span><fmt:formatDate value="${movie.dateAdded}" pattern="dd 'THÁNG' MM, yyyy"/></span>
                <span>C-${movie.ageRestriction}</span>
            </div>

            <div class="md-actions">
                <a href="${pageContext.request.contextPath}/showtimes?movieId=${movie.movieId}" class="btn btn-primary">Mua Vé</a>
            </div>

            <div class="md-story" style="margin-bottom: 30px;">
                <h2 class="md-section-title">Thông Số Doanh Thu</h2>
                <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; text-align: center;">
                    <div style="background: rgba(255,255,255,0.05); padding: 20px; border-radius: 8px;">
                        <div style="font-size: 12px; color: #888; margin-bottom: 8px; text-transform: uppercase;">Kinh phí</div>
                        <div style="font-size: 20px; font-weight: bold; color: var(--cgv-red);">
                            <c:choose>
                                <c:when test="${not empty movie.budget and movie.budget ne '0' and movie.budget ne ''}">
                                    <fmt:formatNumber value="${movie.budget}" type="number"/> USD
                                </c:when>
                                <c:otherwise>--</c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div style="background: rgba(255,255,255,0.05); padding: 20px; border-radius: 8px;">
                        <div style="font-size: 12px; color: #888; margin-bottom: 8px; text-transform: uppercase;">Doanh thu toàn cầu</div>
                        <div style="font-size: 20px; font-weight: bold; color: var(--cgv-red);">
                            <c:choose>
                                <c:when test="${not empty movie.globalBoxOffice and movie.globalBoxOffice ne '0' and movie.globalBoxOffice ne ''}">
                                    <fmt:formatNumber value="${movie.globalBoxOffice}" type="number"/> USD
                                </c:when>
                                <c:otherwise>--</c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div style="background: rgba(255,255,255,0.05); padding: 20px; border-radius: 8px;">
                        <div style="font-size: 12px; color: #888; margin-bottom: 8px; text-transform: uppercase;">Top Tuần</div>
                        <div style="font-size: 20px; font-weight: bold; color: var(--cgv-red);">#${movie.weeklyRevenueRank > 0 ? movie.weeklyRevenueRank : '--'}</div>
                    </div>
                    <div style="background: rgba(255,255,255,0.05); padding: 20px; border-radius: 8px;">
                        <div style="font-size: 12px; color: #888; margin-bottom: 8px; text-transform: uppercase;">Cột mốc vé bán</div>
                        <div style="font-size: 20px; font-weight: bold; color: var(--cgv-red);"><fmt:formatNumber value="${movie.ticketsSoldMilestone}" type="number"/></div>
                    </div>
                </div>
            </div>

            <div class="md-story">
                <h2 class="md-section-title">Cốt Truyện</h2>
                <p class="md-story-text">${movie.description}</p>

                <div class="md-crew-grid">
                    <div>
                        <span class="crew-label">ĐẠO DIỄN</span>
                        <span class="crew-value">${movie.director}</span>
                    </div>

                    <div>
                        <span class="crew-label">QUỐC GIA</span>
                        <span class="crew-value">${movie.country}</span>
                    </div>
                    <div>
                        <span class="crew-label">NGÔN NGỮ</span>
                        <span class="crew-value">${movie.language}</span>
                    </div>
                </div>
            </div>

            <div class="md-cast">
                <div class="md-cast-header">
                    <h2 class="md-section-title" style="margin:0;">Diễn Viên Chính</h2>
                    <a href="#" class="md-view-all">XEM TẤT CẢ</a>
                </div>
                <div class="md-cast-grid">
                    <c:set var="castList" value="${fn:split(movie.cast, ',')}" />
                    <c:forEach var="actor" items="${castList}" begin="0" end="3">
                        <div class="cast-card">
                            <img src="https://ui-avatars.com/api/?name=${fn:replace(actor, ' ', '+')}&background=random&size=300" alt="${actor}" class="cast-img">
                            <div class="cast-name">${fn:trim(actor)}</div>
                            <div class="cast-role">Diễn viên</div>
                        </div>
                    </c:forEach>
                </div>
            </div>

            <!-- ================= RATING & REVIEW SECTION ================= -->
            <div class="site-inner" style="margin-top: 40px; border-top: 1px solid #333; padding-top: 30px;">
                <h2 style="color: var(--cgv-red); font-size: 24px; margin-bottom: 20px;">Đánh Giá Từ Khách Hàng</h2>

                <c:if test="${totalReviews > 0}">
                    <!-- UC21: Thống Kê Rating -->
                    <div style="display: flex; gap: 40px; margin-bottom: 40px; background: #111; padding: 20px; border-radius: 8px;">
                        <div style="text-align: center; min-width: 150px;">
                            <div style="font-size: 48px; font-weight: bold; color: #ffb400;">${avgRating}</div>
                            <div style="color: #ffb400; font-size: 20px; letter-spacing: 2px;">
                                <c:forEach begin="1" end="5" var="i">
                                    <c:choose>
                                        <c:when test="${i <= avgRating}"><i class="fa-solid fa-star"></i></c:when>
                                        <c:when test="${i - 0.5 <= avgRating}"><i class="fa-solid fa-star-half-stroke"></i></c:when>
                                        <c:otherwise><i class="fa-regular fa-star"></i></c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </div>
                            <div style="color: #888; font-size: 14px; margin-top: 5px;">${totalReviews} đánh giá</div>
                        </div>

                        <div style="flex-grow: 1; display: flex; flex-direction: column-reverse; justify-content: center; gap: 8px;">
                            <c:forEach begin="1" end="5" var="i">
                                <div style="display: flex; align-items: center; gap: 10px; font-size: 14px; color: #ccc;">
                                    <span>${i} <i class="fa-solid fa-star" style="color: #ffb400; font-size: 10px;"></i></span>
                                    <div style="flex-grow: 1; background: #e0e0e0; height: 8px; border-radius: 4px; overflow: hidden;">
                                        <div style="background: #ffb400; height: 100%; width: ${(starCounts[i] / totalReviews) * 100}%;"></div>
                                    </div>
                                    <span style="min-width: 30px; text-align: right; color: var(--cgv-dark);">${starCounts[i]}</span>
                                </div>
                            </c:forEach>
                        </div>
                    </div>

                    <!-- Form Đánh giá (UC19 & UC20) -->
                        <div class="review-form-container" style="background: rgba(255,255,255,0.05); padding: 20px; border-radius: 8px; margin-bottom: 30px; position: relative;">
                            
                            <%-- Màn che (Overlay) nếu chưa đăng nhập hoặc chưa mua vé xem phim --%>
                            <c:if test="${empty sessionScope.account or not canReview}">
                                <div style="position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.7); border-radius: 8px; z-index: 10; display: flex; align-items: center; justify-content: center; flex-direction: column;">
                                    <i class="fas fa-lock" style="font-size: 24px; color: #888; margin-bottom: 10px;"></i>
                                    <p style="color: white; font-weight: bold; text-align: center; margin: 0 20px;">
                                        <c:choose>
                                            <c:when test="${empty sessionScope.account}">
                                                Vui lòng <a href="${pageContext.request.contextPath}/Login" style="color: var(--cgv-red); text-decoration: underline;">Đăng nhập</a> và mua vé để đánh giá!
                                            </c:when>
                                            <c:otherwise>
                                                Bạn cần mua vé và quét mã Check-in xem phim này để có thể đánh giá!
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                            </c:if>

                            <c:choose>
                                <%-- CHƯA CÓ ĐÁNH GIÁ -> FORM THÊM MỚI --%>
                                <c:when test="${empty userReview}">
                                    <h3 style="margin-bottom: 15px;">Viết đánh giá của bạn</h3>
                                    <form action="ReviewController" method="POST">
                                        <input type="hidden" name="action" value="add">
                                        <input type="hidden" name="movieId" value="${movie.movieId}">

                                        <label style="display: block; margin-bottom: 10px;">Chấm điểm (1-5 sao):</label>
                                        <select name="rating" required style="padding: 5px; margin-bottom: 15px; background: #333; color: white; border: 1px solid #555; border-radius: 4px;">
                                            <option value="5">5 - Tuyệt vời</option>
                                            <option value="4">4 - Rất hay</option>
                                            <option value="3">3 - Bình thường</option>
                                            <option value="2">2 - Tệ</option>
                                            <option value="1">1 - Rất tệ</option>
                                        </select>

                                        <textarea name="comment" rows="3" placeholder="Nhập cảm nhận của bạn về bộ phim..." style="width: 100%; padding: 10px; border-radius: 4px; border: 1px solid #555; background: #222; color: white; margin-bottom: 15px;"></textarea>
                                        <button type="submit" style="background: var(--cgv-red); color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer;">Gửi Đánh Giá</button>
                                    </form>
                                </c:when>

                                <%-- ĐÃ CÓ ĐÁNH GIÁ -> FORM SỬA / XÓA --%>
                                <c:otherwise>
                                    <h3 style="margin-bottom: 15px;">Đánh giá của bạn</h3>

                                    <%-- Báo lỗi nếu quá 30 ngày --%>
                                    <c:if test="${not empty sessionScope.flashError}">
                                        <div style="color: #ff4d4f; margin-bottom: 15px; padding: 10px; background: rgba(255,77,79,0.1); border-radius: 4px;">${sessionScope.flashError}</div>
                                        <c:remove var="flashError" scope="session" />
                                    </c:if>

                                    <form action="ReviewController" method="POST" style="margin-bottom: 10px;">
                                        <input type="hidden" name="action" value="update">
                                        <input type="hidden" name="movieId" value="${movie.movieId}">
                                        <input type="hidden" name="reviewId" value="${userReview.reviewId}">

                                        <select name="rating" required style="padding: 5px; margin-bottom: 15px; background: #333; color: white; border: 1px solid #555; border-radius: 4px;">
                                            <option value="5" ${userReview.ratingValue == 5 ? 'selected' : ''}>5 - Tuyệt vời</option>
                                            <option value="4" ${userReview.ratingValue == 4 ? 'selected' : ''}>4 - Rất hay</option>
                                            <option value="3" ${userReview.ratingValue == 3 ? 'selected' : ''}>3 - Bình thường</option>
                                            <option value="2" ${userReview.ratingValue == 2 ? 'selected' : ''}>2 - Tệ</option>
                                            <option value="1" ${userReview.ratingValue == 1 ? 'selected' : ''}>1 - Rất tệ</option>
                                        </select>

                                        <textarea name="comment" rows="3" style="width: 100%; padding: 10px; border-radius: 4px; border: 1px solid #555; background: #222; color: white; margin-bottom: 15px;">${userReview.comment}</textarea>
                                        <button type="submit" style="background: #28a745; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; margin-right: 10px;">Lưu Thay Đổi</button>
                                    </form>

                                    <form action="ReviewController" method="POST" onsubmit="return confirm('Bạn có chắc chắn muốn xóa đánh giá này không?');">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="movieId" value="${movie.movieId}">
                                        <input type="hidden" name="reviewId" value="${userReview.reviewId}">
                                        <button type="submit" style="background: #dc3545; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer;">Xóa Đánh Giá</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </div>

                    <!-- UC18: Danh Sách Review -->
                    <div style="display: flex; flex-direction: column; gap: 20px;">
                        <c:forEach var="r" items="${reviews}">
                            <div style="background: #fdfcf0; border: 1px solid #e5e5e5; padding: 20px; border-radius: 8px; display: flex; gap: 15px;">
                                <div style="width: 50px; height: 50px; border-radius: 50%; background: #e0e0e0; overflow: hidden; flex-shrink: 0;">
                                    <c:if test="${not empty r.avatarUrl}">
                                        <img src="${r.avatarUrl}" style="width: 100%; height: 100%; object-fit: cover;">
                                    </c:if>
                                    <c:if test="${empty r.avatarUrl}">
                                        <i class="fa-solid fa-user" style="color: #666; font-size: 24px; margin: 13px;"></i>
                                    </c:if>
                                </div>
                                <div>
                                    <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 5px;">
                                        <strong style="color: var(--cgv-dark); font-size: 16px;">${not empty r.reviewerName ? r.reviewerName : 'Khách Hàng'}</strong>
                                        <div style="color: #ffb400; font-size: 12px;">
                                            <c:forEach begin="1" end="5" var="i">
                                                <i class="fa-${i <= r.ratingValue ? 'solid' : 'regular'} fa-star"></i>
                                            </c:forEach>
                                        </div>
                                    </div>
                                    <div style="color: #888; font-size: 12px; margin-bottom: 10px;">
                                        <fmt:formatDate value="${r.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                    </div>
                                    <div style="color: #444; line-height: 1.5; font-size: 15px;">
                                        ${r.comment}
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:if>

                <c:if test="${totalReviews == 0}">
                    <div style="text-align: center; padding: 40px; background: #fdfcf0; border: 1px solid #e5e5e5; border-radius: 8px; color: #888;">
                        <i class="fa-regular fa-comment-dots" style="font-size: 40px; margin-bottom: 15px;"></i>
                        <p>Chưa có đánh giá nào cho phim này.</p>
                    </div>
                </c:if>
            </div>
            <!-- ================= END RATING & REVIEW ================= -->
        </div>
    </body>
</html>