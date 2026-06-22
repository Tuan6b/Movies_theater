<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Quản lý Phim - CGV Manager</title>

        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    </head>
    <body class="cgv-body">
        <% request.setAttribute("activeNav", "movies"); %>
        <%@ include file="WEB-INF/manager/_sidebar.jsp" %>

        <main class="cgv-main">
            <header class="cgv-header">
                <h1 class="cgv-header-title">Quản lý Phim</h1>
                <div class="cgv-header-right">
                    <div class="cgv-search-wrap">
                        <i class="fa-solid fa-magnifying-glass cgv-search-icon"></i>
                        <input type="text" class="cgv-search" placeholder="Tìm tên phim...">
                    </div>
                    <div class="cgv-header-actions">
                        <button class="cgv-bell-btn"><i class="fa-regular fa-bell"></i></button>
                        <div class="cgv-header-divider"></div>
                        <div class="cgv-user-wrap">
                            <div class="cgv-avatar">M</div>
                            <span class="cgv-user-name">Manager</span>
                        </div>
                    </div>
                </div>
            </header>

            <div class="cgv-page" style="flex-direction: column;">
                <div class="cgv-page-head" style="align-items: center; width: 100%;">
                    <div>
                        <h2 class="cgv-page-title">Danh sách phim hiện tại</h2>
                        <p class="cgv-page-subtitle">Quản lý kho phim, cập nhật trạng thái chiếu và thông tin chi tiết.</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/MovieController?action=add" class="btn--cgv">
                        <i class="fa-solid fa-plus"></i> Thêm Phim Mới
                    </a>
                </div>

                <c:if test="${not empty error}">
                    <div class="cgv-alert cgv-alert-danger fade-in">
                        <strong>Lỗi!</strong> ${error}
                    </div>
                </c:if>
                <c:if test="${not empty success}">
                    <div class="cgv-alert cgv-alert-success fade-in">
                        <strong>Thành công!</strong> ${success}
                    </div>
                </c:if>

                <div class="cgv-toolbar" style="margin-bottom: 24px;">
                    <div class="cgv-pills">
                        <a href="${pageContext.request.contextPath}/MovieController?filter=upcoming" class="cgv-pill ${currentFilter == 'upcoming' ? 'active' : ''}">Sắp chiếu</a>
                        <a href="${pageContext.request.contextPath}/MovieController?filter=showing" class="cgv-pill ${currentFilter == 'showing' ? 'active' : ''}">Đang chiếu</a>
                        <a href="${pageContext.request.contextPath}/MovieController?filter=ended" class="cgv-pill ${currentFilter == 'ended' ? 'active' : ''}">Đã chiếu</a>
                        <a href="${pageContext.request.contextPath}/MovieController?filter=unscheduled" class="cgv-pill ${currentFilter == 'unscheduled' ? 'active' : ''}">Chưa lên lịch</a>
                        <a href="${pageContext.request.contextPath}/MovieController?filter=hidden" class="cgv-pill ${currentFilter == 'hidden' ? 'active' : ''}">Đã ẩn</a>
                    </div>
                </div>

                <div class="cgv-data-wrap fade-in">
                    <table class="cgv-dt">
                        <thead>
                            <tr>
                                <th style="width: 5%;">ID</th>
                                <th style="width: 10%;">Poster</th>
                                <th style="width: 35%;">Thông tin Phim</th>
                                <th style="width: 15%;">Thời lượng</th>
                                <th style="width: 15%;">Trạng thái</th>
                                <th style="width: 20%; text-align: right; padding-right: 24px;">Hành động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${movieList}" var="m">
                                <tr>
                                    <td style="font-weight: 600; color: rgba(94,63,58,0.7);">#${m.movieId}</td>
                                    
                                    <td>
                                        <div style="width: 48px; height: 68px; border-radius: 4px; overflow: hidden; background: #e8e0df; display: flex; align-items: center; justify-content: center; text-align: center; font-size: 10px; color: #888; padding: 2px;">
                                            <c:choose>
                                                <c:when test="${not empty m.poster}">
                                                    <img src="${m.poster}" alt="Poster" style="width: 100%; height: 100%; object-fit: cover;">
                                                </c:when>
                                                <c:otherwise>
                                                    Chưa có poster cho phim này.
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </td>

                                    <td>
                                        <div style="font-family: var(--font-cgv-ui); font-size: 15px; font-weight: 600; color: var(--cgv-dark); margin-bottom: 4px;">
                                            <a href="${pageContext.request.contextPath}/ScheduleController?movieId=${m.movieId}" style="color: var(--cgv-dark); text-decoration: none;">${m.movieName}</a>
                                        </div>
                                        <div style="font-size: 12px; color: rgba(94,63,58,0.6);">
                                            <c:choose>
                                                <c:when test="${m.ageRestriction > 0}">C${m.ageRestriction}</c:when>
                                                <c:otherwise>P - Phổ biến</c:otherwise>
                                            </c:choose> 
                                            • Khởi chiếu: ${m.releaseDate}
                                            <br>
                                            <c:choose>
                                                <c:when test="${not empty m.trailer}">
                                                    <a href="${m.trailer}" target="_blank" style="color: var(--cgv-red); text-decoration: none;">Xem Trailer</a>
                                                </c:when>
                                                <c:otherwise>
                                                    Chưa có trailer cho phim này.
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </td>
                                    
                                    <td>${m.duration} phút</td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${currentFilter == 'hidden'}">
                                                <span class="cgv-badge inactive"><i class="fa-solid fa-eye-slash" style="margin-right:4px;"></i> Đã ẩn</span>
                                            </c:when>
                                            <c:when test="${currentFilter == 'ended'}">
                                                <span class="cgv-badge" style="background: #e0e0e0; color: #555; padding: 4px 12px; border-radius: 100px; font-weight: 600; font-size: 12px; display: inline-flex; align-items: center;"><i class="fa-solid fa-clock-rotate-left" style="margin-right:4px;"></i> Đã chiếu</span>
                                            </c:when>
                                            <c:when test="${currentFilter == 'upcoming'}">
                                                <span class="cgv-badge" style="background: #fff3cd; color: #856404; padding: 4px 12px; border-radius: 100px; font-weight: 600; font-size: 12px; display: inline-flex; align-items: center;"><i class="fa-regular fa-calendar-plus" style="margin-right:4px;"></i> Sắp chiếu</span>
                                            </c:when>
                                            <c:when test="${currentFilter == 'unscheduled'}">
                                                <span class="cgv-badge" style="background: #e2e3e5; color: #383d41; padding: 4px 12px; border-radius: 100px; font-weight: 600; font-size: 12px; display: inline-flex; align-items: center;"><i class="fa-regular fa-calendar-xmark" style="margin-right:4px;"></i> Chưa lên lịch</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="cgv-badge active"><i class="fa-solid fa-circle" style="font-size: 8px; margin-right: 6px;"></i> Đang chiếu</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    
                                    <td style="text-align: right; padding-right: 24px;">
                                        <div style="display: flex; gap: 8px; justify-content: flex-end;">
                                            <c:choose>
                                                <c:when test="${currentFilter == 'showing' || currentFilter == 'ended'}">
                                                    <span class="btn--cgv-outline" style="padding: 6px 14px; opacity: 0.5; cursor: not-allowed; background-color: #f5f5f5; border-color: #ddd; color: #999;">
                                                        Sửa
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="${pageContext.request.contextPath}/MovieController?action=edit&id=${m.movieId}" class="btn--cgv-outline" style="padding: 6px 14px;">
                                                        Sửa
                                                    </a>
                                                </c:otherwise>
                                            </c:choose>

                                            <c:if test="${currentFilter == 'unscheduled' || currentFilter == 'hidden'}">
                                                <form action="${pageContext.request.contextPath}/MovieController" method="POST" onsubmit="return confirm('Bạn có chắc chắn muốn thay đổi trạng thái phim?');">
                                                    <input type="hidden" name="action" value="toggleStatus">
                                                    <input type="hidden" name="movieId" value="${m.movieId}">
                                                    <button type="submit" class="btn btn--ghost" style="color: var(--cgv-red); padding: 6px 14px;">
                                                        ${currentFilter == 'hidden' ? 'Hiện phim' : 'Ẩn phim'}
                                                    </button>
                                                </form>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </body>
</html>