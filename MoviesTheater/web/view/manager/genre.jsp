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

        <% request.setAttribute("activeNav", "genres"); %>
        <%@ include file="/view/manager/_sidebar.jsp" %>

        <main class="cgv-main">

            <header class="cgv-header">
                <h1 class="cgv-header-title">Quản lý Thể loại</h1>

                <div class="cgv-header-right">
                    <div class="cgv-header-actions">
                        <div class="cgv-user-wrap">
                            <div class="cgv-avatar">M</div>
                            <span class="cgv-user-name">
                                <c:choose>
                                    <c:when test="${not empty sessionScope.account}">${sessionScope.account.fullName}</c:when>
                                    <c:otherwise>Manager</c:otherwise>
                                </c:choose>
                            </span>
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
                                            <div id="view-mode-${g.genreID}" style="display: flex; align-items: center; height: 36px;">
                                                <span style="font-weight: 500; font-size: 15px;">${g.genreName}</span> 
                                            </div>
                                            <form id="edit-form-${g.genreID}" action="${pageContext.request.contextPath}/GenreController" method="POST" style="display: none; gap: 8px; align-items: center;">
                                                <input type="hidden" name="action" value="edit">
                                                <input type="hidden" name="genreID" value="${g.genreID}">
                                                <input type="text" name="genreName" value="${g.genreName}" class="cgv-input" style="height: 36px; width: 200px;" required>
                                                <button type="submit" class="btn--cgv-outline" style="padding: 6px 14px; height: 36px;">Lưu</button>
                                                <button type="button" onclick="cancelEdit(${g.genreID})" class="btn btn-ghost" style="padding: 6px; height: 36px;"><i class="fa-solid fa-xmark"></i></button>
                                            </form>
                                        </td>

                                        <td>
                                            <span class="cgv-badge active">Hoạt động</span>
                                        </td>

                                        <td style="text-align: right; padding-right: 24px;">
                                            <div style="display: flex; justify-content: flex-end; gap: 8px;">
                                                <!-- Nút Kích hoạt Edit -->
                                                <button type="button" onclick="enableEdit(${g.genreID})" class="btn btn-ghost" style="color: var(--cgv-dark); padding: 6px 12px;">
                                                    <i class="fa-solid fa-pen"></i> Sửa
                                                </button>
                                                <!-- Form Xóa tích hợp SweetAlert -->
                                                <form id="delete-form-${g.genreID}" action="${pageContext.request.contextPath}/GenreController" method="POST" class="d-inline">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="genreID" value="${g.genreID}">
                                                    <button type="button" onclick="confirmDeleteGenre(${g.genreID}, '${g.genreName}')" class="btn btn-ghost" style="color: var(--cgv-red); padding: 6px 12px;">
                                                        <i class="fa-solid fa-trash"></i> Xóa
                                                    </button>
                                                </form>
                                            </div>
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

        <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11">
        </script>

        <script>
            // Hàm hiển thị Form Sửa
            function enableEdit(id) {
                document.getElementById('view-mode-' + id).style.display = 'none';
                document.getElementById('edit-form-' + id).style.display = 'flex';
            }

            // Hàm Hủy Sửa (Quay lại hiển thị chữ)
            function cancelEdit(id) {
                document.getElementById('view-mode-' + id).style.display = 'flex';
                document.getElementById('edit-form-' + id).style.display = 'none';
            }

            // Hàm Xác nhận Xóa thể loại
            function confirmDeleteGenre(id, name) {
                Swal.fire({
                    title: 'Cảnh báo xóa!',
                    html: `Bạn có chắc chắn muốn xóa thể loại <b>${name}</b> không?<br>Hành động này không thể hoàn tác!`,
                    icon: 'warning',
                    showCancelButton: true,
                    confirmButtonColor: '#e50914',
                    cancelButtonColor: '#6c757d',
                    confirmButtonText: 'Đồng ý Xóa!',
                    cancelButtonText: 'Hủy bỏ'
                }).then((result) => {
                    if (result.isConfirmed) {
                        // Submit cái form Xóa tương ứng
                        document.getElementById('delete-form-' + id).submit();
                    }
                });
            }
        </script>

    </body>
</html>
