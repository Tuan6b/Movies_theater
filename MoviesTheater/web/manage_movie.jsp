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

        <aside class="cgv-sidebar">
            <div class="cgv-sidebar-top">
                <a href="${pageContext.request.contextPath}/">
                    <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV" class="cgv-logo">
                </a>
            </div>

            <nav class="cgv-nav">
                <a href="${pageContext.request.contextPath}/manager" class="cgv-nav-link">
                    <i class="fa-solid fa-chart-pie cgv-nav-icon"></i> Dashboard
                </a>
                <a href="${pageContext.request.contextPath}/MovieController" class="cgv-nav-link active">
                    <i class="fa-solid fa-film cgv-nav-icon"></i> Quản lý Phim
                </a>
                <a href="${pageContext.request.contextPath}/showtimes" class="cgv-nav-link">
                    <i class="fa-regular fa-calendar-days cgv-nav-icon"></i> Lịch chiếu
                </a>
                <a href="${pageContext.request.contextPath}/GenreController" class="cgv-nav-link">
                    <i class="fa-solid fa-tags cgv-nav-icon"></i> Thể loại
                </a>
                <a href="${pageContext.request.contextPath}/RoomServlet" class="cgv-nav-link">
                    <i class="fa-solid fa-desktop cgv-nav-icon"></i> Quản lý Phòng
                </a>
            </nav>

            <div class="cgv-sidebar-bottom">
                <a href="#" class="cgv-nav-link">
                    <i class="fa-solid fa-gear cgv-nav-icon"></i> Cài đặt
                </a>
                <a href="${pageContext.request.contextPath}/Logout" class="cgv-nav-link" style="color: var(--cgv-red);">
                    <i class="fa-solid fa-arrow-right-from-bracket cgv-nav-icon"></i> Đăng xuất
                </a>
            </div>
        </aside>

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
                        <a href="#" class="cgv-pill active">Tất cả phim</a>
                        <a href="#" class="cgv-pill">Đang chiếu</a>
                        <a href="#" class="cgv-pill">Sắp chiếu</a>
                        <a href="#" class="cgv-pill">Đã ẩn</a>
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
                                        <div style="width: 48px; height: 68px; border-radius: 4px; overflow: hidden; background: #e8e0df;">
                                            <img src="${m.poster}" alt="Poster" style="width: 100%; height: 100%; object-fit: cover;">
                                        </div>
                                    </td>

                                    <td>
                                        <div style="font-family: var(--font-cgv-ui); font-size: 15px; font-weight: 600; color: var(--cgv-dark); margin-bottom: 4px;">
                                            ${m.movieName}
                                        </div>
                                        <div style="font-size: 12px; color: rgba(94,63,58,0.6);">
                                            <c:choose>
                                                <c:when test="${m.ageRestriction > 0}">C${m.ageRestriction}</c:when>
                                                <c:otherwise>P - Phổ biến</c:otherwise>
                                            </c:choose> 
                                            • Khởi chiếu: ${m.releaseDate}
                                        </div>
                                    </td>
                                    
                                    <td>${m.duration} phút</td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${m.active}">
                                                <span class="cgv-badge active">Đang chiếu</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="cgv-badge inactive">Đã ẩn</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    
                                    <td style="text-align: right; padding-right: 24px;">
                                        <div style="display: flex; gap: 8px; justify-content: flex-end;">
                                            <a href="${pageContext.request.contextPath}/MovieController?action=edit&id=${m.movieId}" class="btn--cgv-outline" style="padding: 6px 14px;">
                                                Sửa
                                            </a>
                                            <form action="${pageContext.request.contextPath}/MovieController" method="POST" onsubmit="return confirm('Bạn có chắc chắn muốn thay đổi trạng thái phim?');">
                                                <input type="hidden" name="action" value="toggleStatus">
                                                <input type="hidden" name="movieId" value="${m.movieId}">
                                                <button type="submit" class="btn btn--ghost" style="color: var(--cgv-red); padding: 6px 14px;">
                                                    ${m.active ? 'Ẩn phim' : 'Hiện phim'}
                                                </button>
                                            </form>
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