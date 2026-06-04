<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Quản lý Thể loại Phim - CGV Manager</title>

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
                <a href="${pageContext.request.contextPath}/MovieController" class="cgv-nav-link">
                    <i class="fa-solid fa-film cgv-nav-icon"></i> Quản lý Phim
                </a>
                <a href="${pageContext.request.contextPath}/showtimes" class="cgv-nav-link">
                    <i class="fa-regular fa-calendar-days cgv-nav-icon"></i> Lịch chiếu
                </a>
                <a href="${pageContext.request.contextPath}/GenreController" class="cgv-nav-link active">
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
                <h1 class="cgv-header-title">Quản lý Thể loại</h1>

                <div class="cgv-header-right">
                    <div class="cgv-search-wrap">
                        <i class="fa-solid fa-magnifying-glass cgv-search-icon"></i>
                        <input type="text" class="cgv-search" placeholder="Tìm kiếm...">
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

            <div class="cgv-page">

                <div class="cgv-list-wrap">

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

                    <div class="cgv-toolbar">
                        <div class="cgv-pills">
                            <a href="#" class="cgv-pill active">Tất cả thể loại</a>
                        </div>
                    </div>

                    <div class="cgv-data-wrap fade-in">
                        <table class="cgv-dt">
                            <thead>
                                <tr>
                                    <th style="width: 15%;">ID</th>
                                    <th style="width: 50%;">Tên Thể loại</th>
                                    <th style="width: 15%;">Trạng thái</th>
                                    <th style="width: 20%; text-align: right; padding-right: 24px;">Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${genreList}" var="g">
                                    <tr>
                                        <td style="font-weight: 600; color: rgba(94,63,58,0.7);">#${g.genreID}</td>

                                        <td>
                                            <form action="${pageContext.request.contextPath}/GenreController" method="POST" style="display: flex; gap: 8px; align-items: center;">
                                                <input type="hidden" name="action" value="edit">
                                                <input type="hidden" name="genreID" value="${g.genreID}">
                                                <input type="text" name="genreName" value="${g.genreName}" class="cgv-input" style="height: 36px;" required>
                                                <button type="submit" class="btn--cgv-outline" style="padding: 6px 14px; height: 36px;">Lưu</button>
                                            </form>
                                        </td>

                                        <td>
                                            <span class="cgv-badge active">Hoạt động</span>
                                        </td>

                                        <td style="text-align: right; padding-right: 24px;">
                                            <form action="${pageContext.request.contextPath}/GenreController" method="POST" class="d-inline" onsubmit="return confirm('Bạn có chắc chắn muốn xóa thể loại này?');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="genreID" value="${g.genreID}">
                                                <button type="submit" class="btn btn--ghost" style="color: var(--cgv-red); padding: 0;">Xóa</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>

                        <div class="cgv-pager">
                            <span>Hiển thị ${genreList.size()} kết quả</span>
                            <div class="cgv-pager-pages">
                                <button class="cgv-pager-btn active">1</button>
                            </div>
                        </div>
                    </div>
                </div>

                <aside class="cgv-aside fade-in">

                    <div>
                        <h3 class="cgv-aside-heading">Thêm Thể loại Mới</h3>
                        <form action="${pageContext.request.contextPath}/GenreController" method="POST">
                            <input type="hidden" name="action" value="add">

                            <div class="cgv-field">
                                <label class="cgv-label">Tên thể loại *</label>
                                <input type="text" name="genreName" class="cgv-input" placeholder="VD: Hành động, Hài kịch..." required>
                            </div>

                            <button type="submit" class="btn--cgv" style="width: 100%; justify-content: center;">
                                <i class="fa-solid fa-plus"></i> Thêm mới
                            </button>
                        </form>
                    </div>

                    <div class="cgv-aside-divider"></div>

                    <div class="cgv-stats-section">
                        <h3 class="cgv-aside-heading">Thống kê nhanh</h3>
                        <div class="cgv-stats-group">
                            <div>
                                <div class="cgv-stat-num red">${genreList.size()}</div>
                                <div class="cgv-stat-key">Tổng số Thể loại</div>
                            </div>
                        </div>
                    </div>

                </aside>

            </div>
        </main>

    </body>
</html>