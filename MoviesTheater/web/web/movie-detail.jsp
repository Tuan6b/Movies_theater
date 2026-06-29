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
        .md-wrap { padding: 40px 0 100px; }
        .md-eyebrow { text-align: center; color: var(--cgv-red); font-size: 11px; font-weight: 700; letter-spacing: 2px; text-transform: uppercase; margin-bottom: 16px; }
        .md-title { font-family: var(--font-display); font-size: 64px; text-align: center; text-transform: uppercase; margin-bottom: 40px; letter-spacing: 1px; line-height: 1.1; }
        .md-hero-img { width: 100%; height: 500px; object-fit: cover; border-radius: var(--r-xl); box-shadow: 0 24px 48px rgba(94,63,58,0.15); margin-bottom: 40px; }
        
        .md-meta-bar { display: flex; justify-content: center; gap: 40px; font-size: 12px; font-weight: 700; color: var(--cgv-text-muted); text-transform: uppercase; letter-spacing: 1px; margin-bottom: 40px; }
        
        .md-actions { display: flex; justify-content: center; gap: 16px; margin-bottom: 80px; }
        .md-actions .btn { min-width: 160px; justify-content: center; height: 48px; font-size: 14px; border-radius: 100px; }
        .md-actions .btn-ghost { border: 1px solid var(--cgv-border); background: var(--cgv-surface); }
        .md-actions .btn-ghost:hover { background: var(--cgv-bg); }

        .md-section-title { font-family: var(--font-display); font-size: 28px; text-transform: uppercase; text-align: center; margin-bottom: 32px; letter-spacing: 1px; }
        
        .md-story-text { max-width: 760px; margin: 0 auto 60px; text-align: center; line-height: 1.8; color: var(--cgv-text-muted); font-size: 15px; }
        
        .md-crew-grid { display: flex; justify-content: center; gap: 80px; border-top: 1px solid var(--cgv-border); border-bottom: 1px solid var(--cgv-border); padding: 40px 0; margin-bottom: 80px; text-align: center; }
        .crew-label { display: block; font-size: 10px; color: var(--cgv-text-dim); text-transform: uppercase; letter-spacing: 2px; margin-bottom: 8px; }
        .crew-value { display: block; font-weight: 700; font-size: 14px; }

        .md-cast-header { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 32px; }
        .md-view-all { font-size: 11px; font-weight: 700; color: var(--cgv-red); text-transform: uppercase; letter-spacing: 1px; }
        .md-cast-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; }
        .cast-card { text-align: center; }
        .cast-img { width: 100%; height: 320px; object-fit: cover; border-radius: var(--r-md); margin-bottom: 16px; background-color: #fff; box-shadow: 0 4px 12px rgba(94,63,58,0.08); }
        .cast-name { font-weight: 700; font-size: 15px; margin-bottom: 4px; }
        .cast-role { font-size: 12px; color: var(--cgv-text-dim); }
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
          <jsp:useBean id="now" class="java.util.Date" />
          <div class="md-eyebrow">
              <c:choose>
                  <c:when test="${movie.releaseDate > now}">SẮP CHIẾU</c:when>
                  <c:otherwise>ĐANG CHIẾU</c:otherwise>
              </c:choose> 
              &nbsp;|&nbsp; IMAX 2D
          </div>
        
        <h1 class="md-title">${movie.movieName}</h1>
        
        <img src="${movie.poster}" alt="Cover" class="md-hero-img">
        
        <div class="md-meta-bar">
            <span>${movie.duration} PHÚT</span>
            <span><fmt:formatDate value="${movie.releaseDate}" pattern="dd 'THÁNG' MM, yyyy"/></span>
            <span>C-${movie.ageRestriction}</span>
        </div>

        <div class="md-actions">
            <a href="${pageContext.request.contextPath}/ShowtimeServlet?movieId=${movie.movieId}" class="btn btn-primary">Mua Vé</a>
            <a href="${movie.trailer}" target="_blank" class="btn btn-ghost">Xem Trailer</a>
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
                    <span class="crew-label">NHÀ SẢN XUẤT</span>
                    <span class="crew-value">CGV Films</span>
                </div>
                <div>
                    <span class="crew-label">HÃNG PHIM</span>
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