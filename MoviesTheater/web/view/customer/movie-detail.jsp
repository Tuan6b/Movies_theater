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
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

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

            /* STAR RATING CSS */
            .star-rating {
                display: inline-flex;
                flex-direction: row-reverse;
                justify-content: flex-end;
                margin-bottom: 15px;
            }
            .star-rating input[type="radio"] {
                display: none;
            }
            .star-rating label {
                color: #ddd;
                font-size: 30px;
                padding: 0 4px;
                cursor: pointer;
                transition: all 0.2s ease-in-out;
            }
            .star-rating label:hover,
            .star-rating label:hover ~ label {
                color: #ffb400;
                transform: scale(1.1);
            }
            .star-rating input[type="radio"]:checked ~ label {
                color: #ffb400;
            }
            @keyframes fillBar {
                from {
                    width: 0;
                }
            }
            .rating-bar-fill {
                height: 100%;
                background: #ffb400;
                border-radius: 4px;
                animation: fillBar 1.5s ease-out forwards;
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
                            <a href="${pageContext.request.contextPath}/profile" class="btn btn-ghost" style="margin-right: 8px;">Trang cá nhân</a>
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
                <c:if test="${not empty genres}">
                    <span style="border-left: 2px solid var(--cgv-border); padding-left: 15px; margin-left: 5px;">
                        <c:forEach var="genre" items="${genres}" varStatus="status">
                            ${genre.genreName}${not status.last ? ', ' : ''}
                        </c:forEach>
                    </span>
                </c:if>
                <span id="tmdb-user-score-badge" style="display:none; align-items:center; gap:5px; border-left: 2px solid var(--cgv-border); padding-left: 15px; margin-left: 5px;">
                    <i class="fa-solid fa-face-smile" style="color: #ffb400;"></i> 
                    Điểm số trên TMDB: <strong id="tmdb-user-score-value"></strong>
                </span>
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
                                    <c:out value="${movie.budget}"/>
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
                                    <c:out value="${movie.globalBoxOffice}"/>
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
                    <div style="display: flex; gap: 40px; margin-bottom: 40px; background: #fdfdfd; border: 1px solid #e0e0e0; box-shadow: 0 4px 15px rgba(0,0,0,0.03); padding: 20px; border-radius: 8px;">
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
                                <div style="display: flex; align-items: center; gap: 10px; font-size: 14px; color: #333; font-weight: 500;">
                                    <span>${i} <i class="fa-solid fa-star" style="color: #ffb400; font-size: 10px;"></i></span>
                                    <div style="flex-grow: 1; background: #e0e0e0; height: 8px; border-radius: 4px; overflow: hidden;">
                                        <div class="rating-bar-fill" data-width="${totalReviews > 0 ? (starCounts[i] * 100.0 / totalReviews) : 0}%" style="background-color: #ffb400; height: 100%; width: 0%; transition: width 1.2s cubic-bezier(0.22, 1, 0.36, 1);"></div>
                                    </div>
                                    <span style="min-width: 30px; text-align: right; color: #666;">${starCounts[i]}</span>
                                </div>
                            </c:forEach>
                        </div>
                    </div>

                </c:if>

                <!-- Form Đánh giá (UC19 & UC20) -->
                <div class="review-form-container" style="background: #fdfdfd; border: 1px solid #e0e0e0; box-shadow: 0 4px 15px rgba(0,0,0,0.03); padding: 20px; border-radius: 8px; margin-bottom: 30px; position: relative;">

                    <%-- Màn che (Overlay) nếu chưa đăng nhập hoặc chưa mua vé xem phim --%>
                    <c:if test="${empty sessionScope.account or (empty userReview and not canReview)}">
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
                                <div class="star-rating">
                                    <input type="radio" id="star5" name="rating" value="5" required />
                                    <label for="star5" title="Tuyệt vời">
                                        <i class="fa-solid fa-star"></i>
                                    </label>

                                    <input type="radio" id="star4" name="rating" value="4" />
                                    <label for="star4" title="Hay">
                                        <i class="fa-solid fa-star"></i>
                                    </label>

                                    <input type="radio" id="star3" name="rating" value="3" />
                                    <label for="star3" title="Bình thường">
                                        <i class="fa-solid fa-star"></i>
                                    </label>

                                    <input type="radio" id="star2" name="rating" value="2" />
                                    <label for="star2" title="Tệ">
                                        <i class="fa-solid fa-star"></i>
                                    </label>

                                    <input type="radio" id="star1" name="rating" value="1" />
                                    <label for="star1" title="Rất tệ">
                                        <i class="fa-solid fa-star"></i>
                                    </label>
                                </div>

                                <textarea name="comment" rows="3" placeholder="Nhập cảm nhận của bạn về bộ phim..." style="width: 100%; padding: 10px; border-radius: 4px; border: 1px solid #ccc; background: #fff; color: #333; margin-bottom: 15px; font-family: inherit; resize: vertical;"></textarea>
                                <button type="submit" style="background: var(--cgv-red); color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer;">Gửi Đánh Giá</button>
                            </form>
                        </c:when>

                        <%-- ĐÃ CÓ ĐÁNH GIÁ -> FORM SỬA / XÓA --%>
                        <c:otherwise>
                            <h3 style="margin-bottom: 15px; display: flex; justify-content: space-between; align-items: center;">
                                Đánh giá của bạn
                                <div style="position: relative; display: inline-block;">
                                    <button type="button" onclick="toggleEditMenu()" style="background: none; border: none; font-size: 20px; cursor: pointer; color: #666; padding: 0 10px;"><i class="fa-solid fa-ellipsis-vertical"></i></button>
                                    <div id="edit-dropdown-menu" style="display: none; position: absolute; right: 0; top: 100%; background: #fff; border: 1px solid #ddd; box-shadow: 0 4px 12px rgba(0,0,0,0.1); border-radius: 4px; z-index: 100; min-width: 150px; text-align: left;">
                                        <button type="button" onclick="showEditForm()" style="width: 100%; padding: 10px 15px; background: none; border: none; text-align: left; cursor: pointer; border-bottom: 1px solid #eee; font-family: inherit; font-size: 14px;"><i class="fa-solid fa-pen" style="margin-right: 8px; color: #28a745;"></i> Chỉnh sửa</button>
                                        <button type="button" onclick="submitDeleteReview()" style="width: 100%; padding: 10px 15px; background: none; border: none; text-align: left; cursor: pointer; color: #dc3545; font-family: inherit; font-size: 14px;"><i class="fa-solid fa-trash" style="margin-right: 8px;"></i> Xóa</button>
                                    </div>
                                </div>
                            </h3>

                            <%-- Báo lỗi nếu quá 30 ngày --%>
                            <c:if test="${not empty sessionScope.flashError}">
                                <div style="color: #ff4d4f; margin-bottom: 15px; padding: 10px; background: rgba(255,77,79,0.1); border-radius: 4px;">${sessionScope.flashError}</div>
                                <c:remove var="flashError" scope="session" />
                            </c:if>

                            <!-- READ-ONLY VIEW OF USER'S REVIEW -->
                            <div id="user-review-display">
                                <div style="display: flex; gap: 15px;">
                                    <div style="width: 50px; height: 50px; background: #e0e0e0; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #666; font-size: 24px;">
                                        <i class="fa-solid fa-user"></i>
                                    </div>
                                    <div style="flex-grow: 1;">
                                        <div style="display: flex; justify-content: space-between; align-items: baseline;">
                                            <h4 style="margin: 0; font-size: 16px; color: #333;">${sessionScope.account.fullName}</h4>
                                        </div>
                                        <div style="color: #ffb400; font-size: 12px; margin: 5px 0;">
                                            <c:forEach begin="1" end="5" var="i">
                                                <c:choose>
                                                    <c:when test="${i <= userReview.ratingValue}"><i class="fa-solid fa-star"></i></c:when>
                                                    <c:otherwise><i class="fa-regular fa-star"></i></c:otherwise>
                                                </c:choose>
                                            </c:forEach>
                                        </div>
                                        <div style="color: #888; font-size: 12px; margin-bottom: 8px;">
                                            <fmt:formatDate value="${userReview.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                        </div>
                                        <p style="color: #444; font-size: 15px; line-height: 1.5; margin: 0; white-space: pre-wrap;"><c:out value="${userReview.comment}" /></p>
                                    </div>
                                </div>
                            </div>

                            <!-- EDIT FORM (HIDDEN BY DEFAULT) -->
                            <div id="user-review-edit-form" style="display: none; margin-top: 15px;">
                                <form action="ReviewController" method="POST" style="margin-bottom: 10px;">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="movieId" value="${movie.movieId}">
                                    <input type="hidden" name="reviewId" value="${userReview.reviewId}">

                                    <label style="display: block; margin-bottom: 10px;">Chấm điểm (1-5 sao):</label>
                                    <div class="star-rating">
                                        <input type="radio" id="edit_star5" name="rating" value="5" ${userReview.ratingValue == 5 ? 'checked' : ''} required />
                                        <label for="edit_star5" title="Tuyệt vời"><i class="fa-solid fa-star"></i></label>

                                        <input type="radio" id="edit_star4" name="rating" value="4" ${userReview.ratingValue == 4 ? 'checked' : ''} />
                                        <label for="edit_star4" title="Hay"><i class="fa-solid fa-star"></i></label>

                                        <input type="radio" id="edit_star3" name="rating" value="3" ${userReview.ratingValue == 3 ? 'checked' : ''} />
                                        <label for="edit_star3" title="Bình thường"><i class="fa-solid fa-star"></i></label>

                                        <input type="radio" id="edit_star2" name="rating" value="2" ${userReview.ratingValue == 2 ? 'checked' : ''} />
                                        <label for="edit_star2" title="Tệ"><i class="fa-solid fa-star"></i></label>

                                        <input type="radio" id="edit_star1" name="rating" value="1" ${userReview.ratingValue == 1 ? 'checked' : ''} />
                                        <label for="edit_star1" title="Rất tệ"><i class="fa-solid fa-star"></i></label>
                                    </div>

                                    <textarea name="comment" rows="3" style="width: 100%; padding: 10px; border-radius: 4px; border: 1px solid #ccc; background: #fff; color: #333; margin-bottom: 15px; font-family: inherit; resize: vertical;">${userReview.comment}</textarea>
                                    <div style="display: flex; gap: 10px;">
                                        <button type="submit" style="background: #28a745; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer;">Lưu Thay Đổi</button>
                                        <button type="button" onclick="cancelEditForm()" style="background: #e0e0e0; color: #333; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer;">Hủy</button>
                                    </div>
                                </form>
                            </div>

                            <form id="delete-review-form" action="ReviewController" method="POST" style="display: none;">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="movieId" value="${movie.movieId}">
                                <input type="hidden" name="reviewId" value="${userReview.reviewId}">
                            </form>
                        </c:otherwise>
                    </c:choose>
                </div>

                <c:if test="${totalReviews > 0}">
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


        <script>
            // Xử lý xác nhận xóa đánh giá
            function confirmDeleteReview(event, formElement) {
                event.preventDefault();
                if (confirm('Bài đánh giá của bạn sẽ bị xóa vĩnh viễn khỏi phim này! Bạn có chắc chắn?')) {
                    formElement.submit();
                }
            }

            // Xử lý bộ lọc từ ngữ không phù hợp cho form gửi Review
            document.addEventListener("DOMContentLoaded", function() {
                // Hiệu ứng thanh tiến trình đánh giá (UC21 - CSS Animation "WOW" effect)
                const ratingBars = document.querySelectorAll('.rating-bar-fill');
                setTimeout(() => {
                    ratingBars.forEach(bar => {
                        bar.style.width = bar.getAttribute('data-width');
                    });
                }, 100);

                const reviewForms = document.querySelectorAll('form[action="ReviewController"]');
                const badWords = ["ngu", "dm", "vl", "rac", "rác"]; 

                reviewForms.forEach(form => {
                    if (form.id !== 'delete-review-form') {
                        form.addEventListener('submit', function (e) {
                            const commentBox = this.querySelector('textarea[name="comment"]');
                            if (commentBox) {
                                const commentText = commentBox.value.toLowerCase();
                                const containsBadWord = badWords.some(word => commentText.includes(word));

                                if (containsBadWord) {
                                    e.preventDefault();
                                    alert('Ngôn từ không phù hợp! Bình luận của bạn chứa từ ngữ vi phạm tiêu chuẩn cộng đồng. Vui lòng sửa lại.');
                                }
                            }
                        });
                    }
                });
            });

            // Fetch TMDB User Score dynamically for this movie
            const movieName = "${fn:escapeXml(movie.movieName)}";
            if (movieName) {
                console.log("Đang lấy User Score cho phim:", movieName);
                fetch('${pageContext.request.contextPath}/TMDBController?query=' + encodeURIComponent(movieName))
                    .then(response => response.json())
                    .then(data => {
                        console.log("Kết quả từ TMDB:", data);
                        if (!data.error && data.UserScore) {
                            document.getElementById('tmdb-user-score-value').innerText = data.UserScore;
                            document.getElementById('tmdb-user-score-badge').style.display = 'inline-flex';
                        }
                    })
                    .catch(err => console.error("TMDB fetch error:", err));
            }

            function toggleEditMenu() {
                var menu = document.getElementById("edit-dropdown-menu");
                menu.style.display = (menu.style.display === "none" || menu.style.display === "") ? "block" : "none";
            }
            function showEditForm() {
                document.getElementById("edit-dropdown-menu").style.display = "none";
                document.getElementById("user-review-display").style.display = "none";
                document.getElementById("user-review-edit-form").style.display = "block";
            }
            function cancelEditForm() {
                document.getElementById("user-review-display").style.display = "block";
                document.getElementById("user-review-edit-form").style.display = "none";
            }
            function submitDeleteReview() {
                document.getElementById("edit-dropdown-menu").style.display = "none";
                var formElement = document.getElementById("delete-review-form");
                if (confirm('Bạn có chắc chắn muốn xóa đánh giá này không? Hành động này không thể hoàn tác.')) {
                    formElement.submit();
                }
            }
            window.addEventListener('click', function(e) {
                var menu = document.getElementById("edit-dropdown-menu");
                if (menu && menu.style.display === 'block') {
                    if (!e.target.closest('h3') && !e.target.closest('#edit-dropdown-menu')) {
                        menu.style.display = 'none';
                    }
                }
            });
        </script>
    </body>
</html>