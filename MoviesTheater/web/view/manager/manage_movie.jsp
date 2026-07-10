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
        <% request.setAttribute("activeNav", "movies" ); %>
        <%@ include file="/view/manager/_sidebar.jsp" %>

        <main class="cgv-main">
            <header class="cgv-header">
                <h1 class="cgv-header-title">Quản lý Phim</h1>
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

            <div class="cgv-page" style="flex-direction: column;">
                <div class="cgv-page-head" style="align-items: center; width: 100%;">
                    <div>
                        <h2 class="cgv-page-title">Danh sách phim hiện tại</h2>
                        <p class="cgv-page-subtitle">Quản lý kho phim, cập nhật trạng thái chiếu và thông
                            tin chi tiết.</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/MovieController?action=add"
                       class="btn--cgv">
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

                <!-- Dashboard Statistics -->
                <div class="cgv-stats-dashboard fade-in" style="display: flex; width: 100%; box-sizing: border-box; gap: 16px; margin-bottom: 24px; padding: 16px; background: #fff; border: 1px solid #e1d8d8; border-radius: 8px;">
                    <div class="cgv-stat-card" style="flex: 1; text-align: center; border-right: 1px solid #e1d8d8;">
                        <div class="cgv-stat-title" style="font-size: 12px; color: #5e3f3a; font-weight: 600; text-transform: uppercase;">Đang chiếu</div>
                        <div class="cgv-stat-value" style="font-size: 24px; font-weight: 700; color: #2b2b2b; margin-top: 4px;">${not empty movieStats ? movieStats['showing'] : 0}</div>
                    </div>
                    <div class="cgv-stat-card" style="flex: 1; text-align: center; border-right: 1px solid #e1d8d8;">
                        <div class="cgv-stat-title" style="font-size: 12px; color: #5e3f3a; font-weight: 600; text-transform: uppercase;">Sắp chiếu</div>
                        <div class="cgv-stat-value" style="font-size: 24px; font-weight: 700; color: #2b2b2b; margin-top: 4px;">${not empty movieStats ? movieStats['upcoming'] : 0}</div>
                    </div>
                    <div class="cgv-stat-card" style="flex: 1; text-align: center; border-right: 1px solid #e1d8d8;">
                        <div class="cgv-stat-title" style="font-size: 12px; color: #5e3f3a; font-weight: 600; text-transform: uppercase;">Đã chiếu</div>
                        <div class="cgv-stat-value" style="font-size: 24px; font-weight: 700; color: #2b2b2b; margin-top: 4px;">${not empty movieStats ? movieStats['ended'] : 0}</div>
                    </div>
                    <div class="cgv-stat-card" style="flex: 1; text-align: center;">
                        <div class="cgv-stat-title" style="font-size: 12px; color: #5e3f3a; font-weight: 600; text-transform: uppercase;">Đã ẩn</div>
                        <div class="cgv-stat-value" style="font-size: 24px; font-weight: 700; color: #e71a0f; margin-top: 4px;">${not empty movieStats ? movieStats['hidden'] : 0}</div>
                    </div>
                </div>

                <div class="cgv-toolbar" style="margin-bottom: 24px; width: 100%; box-sizing: border-box;">
                    <!-- Form tự động gửi GET request khi đổi thẻ option -->
                    <form action="${pageContext.request.contextPath}/MovieController" method="GET"
                          style="display: flex; align-items: center; gap: 12px;">
                        <label for="filterStatus" style="font-weight: 600; color: var(--cgv-dark);">Lọc
                            trạng thái:</label>

                        <select name="filter" id="filterStatus" onchange="this.form.submit()"
                                class="cgv-select" style="width: 250px;">
                            <option value="upcoming" ${currentFilter=='upcoming' ? 'selected' : '' }>Sắp chiếu</option>
                            <option value="showing" ${currentFilter=='showing' ? 'selected' : '' }>Đang chiếu</option>
                            <option value="unscheduled" ${currentFilter=='unscheduled' ? 'selected' : ''}>Chưa lên lịch</option>
                            <option value="ended" ${currentFilter=='ended' ? 'selected' : '' }>Đã chiếu xong</option>
                            <option value="hidden" ${currentFilter=='hidden' ? 'selected' : '' }>Bị ẩn</option>
                        </select>
                    </form>
                </div>

                <div class="cgv-data-wrap fade-in" style="width: 100%; box-sizing: border-box;">
                    <table class="cgv-dt">
                        <thead>
                            <tr>
                                <th style="width: 5%;">ID</th>
                                <th style="width: 10%;">Poster</th>
                                <th style="width: 35%;">Thông tin Phim</th>
                                <th style="width: 15%;">Thời lượng</th>
                                <th style="width: 15%;">Trạng thái</th>
                                <th style="width: 20%; text-align: right; padding-right: 24px;">Hành động
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${movieList}" var="m">
                                <tr>
                                    <td style="font-weight: 600; color: rgba(94,63,58,0.7);">#${m.movieId}
                                    </td>

                                    <td>
                                        <div
                                            style="width: 48px; height: 68px; border-radius: 4px; overflow: hidden; background: #e8e0df; display: flex; align-items: center; justify-content: center; text-align: center; font-size: 10px; color: #888; padding: 2px;">
                                            <c:choose>
                                                <c:when test="${not empty m.poster}">
                                                    <img src="${m.poster}" alt="Poster"
                                                         style="width: 100%; height: 100%; object-fit: cover;">
                                                </c:when>
                                                <c:otherwise>
                                                    Chưa có poster cho phim này.
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </td>

                                    <td>
                                        <div
                                            style="font-family: var(--font-cgv-ui); font-size: 15px; font-weight: 600; color: var(--cgv-dark); margin-bottom: 4px;">
                                            <a href="${pageContext.request.contextPath}/ScheduleController?movieId=${m.movieId}"
                                               style="color: var(--cgv-dark); text-decoration: none;">${m.movieName}</a>
                                        </div>
                                        <div style="font-size: 12px; color: rgba(94,63,58,0.6);">
                                            <c:choose>
                                                <c:when test="${m.ageRestriction > 0}">C${m.ageRestriction}
                                                </c:when>
                                                <c:otherwise>P - Phổ biến</c:otherwise>
                                            </c:choose>
                                            <small class="text-muted d-block mt-1">
                                                Khởi chiếu: ${m.dateAdded}
                                            </small>
                                            <c:choose>
                                                <c:when test="${not empty m.trailer}">
                                                    <a href="${m.trailer}" target="_blank"
                                                       style="color: var(--cgv-red); text-decoration: none;">Xem
                                                        Trailer</a>
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
                                                <span class="cgv-badge inactive"><i
                                                        class="fa-solid fa-eye-slash"
                                                        style="margin-right:4px;"></i> Đã ẩn</span>
                                                </c:when>
                                                <c:when test="${currentFilter == 'ended'}">
                                                <span class="cgv-badge"
                                                      style="background: #e0e0e0; color: #555; padding: 4px 12px; border-radius: 100px; font-weight: 600; font-size: 12px; display: inline-flex; align-items: center;"><i
                                                        class="fa-solid fa-clock-rotate-left"
                                                        style="margin-right:4px;"></i> Đã chiếu</span>
                                                </c:when>
                                                <c:when test="${currentFilter == 'upcoming'}">
                                                <span class="cgv-badge"
                                                      style="background: #fff3cd; color: #856404; padding: 4px 12px; border-radius: 100px; font-weight: 600; font-size: 12px; display: inline-flex; align-items: center;"><i
                                                        class="fa-regular fa-calendar-plus"
                                                        style="margin-right:4px;"></i> Sắp chiếu</span>
                                                </c:when>
                                                <c:when test="${currentFilter == 'unscheduled'}">
                                                <span class="cgv-badge"
                                                      style="background: #e2e3e5; color: #383d41; padding: 4px 12px; border-radius: 100px; font-weight: 600; font-size: 12px; display: inline-flex; align-items: center;"><i
                                                        class="fa-regular fa-calendar-xmark"
                                                        style="margin-right:4px;"></i> Chưa lên lịch</span>
                                                </c:when>
                                                <c:otherwise>
                                                <span class="cgv-badge active"><i class="fa-solid fa-circle"
                                                                                  style="font-size: 8px; margin-right: 6px;"></i> Đang
                                                    chiếu</span>
                                                </c:otherwise>
                                            </c:choose>
                                    </td>

                                    <td style="text-align: right; padding-right: 24px;">
                                        <div style="display: flex; gap: 8px; justify-content: flex-end;">
                                            <c:choose>
                                                <c:when
                                                    test="${currentFilter == 'showing' || currentFilter == 'ended'}">
                                                    <span class="btn--cgv-outline"
                                                          style="padding: 6px 14px; opacity: 0.5; cursor: not-allowed; background-color: #f5f5f5; border-color: #ddd; color: #999;">
                                                        Sửa
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="${pageContext.request.contextPath}/MovieController?action=edit&id=${m.movieId}"
                                                       class="btn--cgv-outline" style="padding: 6px 14px;">
                                                        Sửa
                                                    </a>
                                                </c:otherwise>
                                            </c:choose>

                                            <!-- CODE MỚI CỦA NÚT ẨN/HIỆN PHIM -->
                                            <c:choose>
                                                <c:when test="${currentFilter == 'hidden'}">
                                                    <form action="${pageContext.request.contextPath}/MovieController" method="POST" onsubmit="confirmEmergencyHide(event, this, false);">
                                                        <input type="hidden" name="action" value="toggleStatus">
                                                        <input type="hidden" name="movieId" value="${m.movieId}">
                                                        <button type="submit" class="btn btn--ghost" style="color: #28a745; padding: 6px 14px;">Hiện phim</button>
                                                    </form>
                                                </c:when>

                                                <c:when test="${currentFilter == 'unscheduled' || currentFilter == 'ended'}">
                                                    <form action="${pageContext.request.contextPath}/MovieController" method="POST" onsubmit="confirmEmergencyHide(event, this, false);">
                                                        <input type="hidden" name="action" value="toggleStatus">
                                                        <input type="hidden" name="movieId" value="${m.movieId}">
                                                        <button type="submit" class="btn btn--ghost" style="color: var(--cgv-red); padding: 6px 14px;">Ẩn phim</button>
                                                    </form>
                                                </c:when>

                                                <c:otherwise>
                                                    <form action="${pageContext.request.contextPath}/MovieController" method="POST" onsubmit="confirmEmergencyHide(event, this, true);">
                                                        <input type="hidden" name="action" value="toggleStatus">
                                                        <input type="hidden" name="movieId" value="${m.movieId}">
                                                        <button type="submit" class="btn btn--ghost" style="color: #fff; background-color: var(--cgv-red); border: none; border-radius: 4px; font-weight: bold; padding: 6px 14px;">
                                                            Ẩn khẩn cấp
                                                        </button>
                                                    </form>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
        <script>
            
            function confirmEmergencyHide(event, formElement, isEmergency) {
                // NGĂN chặn form gửi đi ngay lập tức
                event.preventDefault();

                if (!isEmergency) {
                    // Phim chưa lên lịch / đã chiếu -> Hỏi bình thường
                    Swal.fire({
                        title: 'Xác nhận thay đổi?',
                        text: 'Bạn có chắc chắn muốn thay đổi trạng thái của phim này?',
                        icon: 'question',
                        showCancelButton: true,
                        confirmButtonColor: '#28a745',
                        cancelButtonColor: '#6c757d',
                        confirmButtonText: 'Đồng ý',
                        cancelButtonText: 'Hủy'
                    }).then((result) => {
                        if (result.isConfirmed) {
                            formElement.submit(); // Bấm đồng ý mới gửi Data xuống DB
                        }
                    });
                } else {
                    // Phim đang chiếu / sắp chiếu -> popup Báo Động Đỏ, yêu cầu nhập chữ
                    Swal.fire({
                        title: 'CẢNH BÁO KHẨN CẤP',
                        html: '<span style="color:red; font-size:14px;">Phim này đang có lịch chiếu công khai. Hủy ngang sẽ ảnh hưởng nghiêm trọng đến vé đã bán!</span><br><br>Hãy gõ chữ <b>ANPHIMKHANCAP</b> để xác nhận:',
                        icon: 'warning',
                        input: 'text',
                        inputPlaceholder: 'Nhập ANPHIMKHANCAP...',
                        inputAttributes: {
                            autocomplete: 'off'
                        },
                        showCancelButton: true,
                        confirmButtonColor: '#e50914', // Nút Đỏ
                        cancelButtonColor: '#6c757d',
                        confirmButtonText: 'Thực thi ngay!',
                        cancelButtonText: 'Hủy bỏ',
                        // Hàm preConfirm sẽ kiểm tra input trước khi cho phép đóng Popup
                        preConfirm: (inputValue) => {
                            if (inputValue !== "ANPHIMKHANCAP") {
                                Swal.showValidationMessage('Sai mã xác nhận! Yêu cầu gõ chính xác: ANPHIMKHANCAP');
                                return false;
                            }
                            return true;
                        }
                    }).then((result) => {
                        if (result.isConfirmed) {
                            formElement.submit(); // Nhập đúng mã mới gửi Data xuống DB
                        }
                    });
                }
            }
        </script>

        <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    </body>

</html>