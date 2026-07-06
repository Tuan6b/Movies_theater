<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Sửa Thông Tin Phim - CGV Manager</title>

        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
        <style>
            .cgv-form-grid {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 0 24px;
            }
            .cgv-form-full {
                grid-column: span 2;
            }
        </style>
    </head>
    <body class="cgv-body">

        <% request.setAttribute("activeNav", "movies"); %>
        <%@ include file="/view/manager/_sidebar.jsp" %>

        <main class="cgv-main">

            <header class="cgv-header">
                <h1 class="cgv-header-title">Cập Nhật Phim: #${movie.movieId}</h1>

                <div class="cgv-header-right">
                    <a href="${pageContext.request.contextPath}/MovieController" class="btn--cgv-outline">
                        <i class="fa-solid fa-arrow-left"></i> Quay lại
                    </a>
                </div>
            </header>

            <div class="cgv-page" style="flex-direction: column;">

                <c:if test="${not empty error}">
                    <div class="cgv-alert cgv-alert-danger fade-in">
                        <strong>Lỗi!</strong> ${error}
                    </div>
                </c:if>

                <div class="cgv-data-wrap fade-in" style="padding: 32px; max-width: 900px; margin: 0 auto; width: 100%;">

                    <h2 class="cgv-page-title" style="margin-bottom: 24px;">Chỉnh sửa thông tin phim</h2>

                    <form action="${pageContext.request.contextPath}/MovieController" method="POST">
                        <input type="hidden" name="action" value="edit">
                        <input type="hidden" name="movieId" value="${movie.movieId}">

                        <div class="cgv-form-grid">
                            <div class="cgv-field">
                                <label class="cgv-label">Tên phim *</label>
                                <div style="display: flex; gap: 8px;">
                                    <input type="text" name="movieName" class="cgv-input" value="${movie.movieName}" readonly style="background-color: #f5f5f5; flex: 1;" required>
                                    <button type="button" id="btnFetchTMDB" style="background: #e50914; color: white; border: none; padding: 0 12px; font-size: 12px; border-radius: 4px; cursor: pointer; white-space: nowrap;">
                                        <i class="fa-solid fa-cloud-arrow-down"></i> Dữ liệu TMDB
                                    </button>
                                </div>
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Kinh phí sản xuất</label>
                                <input type="text" name="budget" class="cgv-input" value="${movie.budget}" placeholder="VD: 500 Triệu USD">
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Doanh thu toàn cầu</label>
                                <input type="text" name="globalBoxOffice" class="cgv-input" value="${movie.globalBoxOffice}" placeholder="VD: 2.8 Tỷ USD">
                            </div>



                            <div class="cgv-field">
                                <label class="cgv-label">Thời lượng (phút) *</label>
                                <input type="number" name="duration" class="cgv-input" min="40" max="300" value="${movie.duration}" readonly style="background-color: #f5f5f5;" required>
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Giới hạn độ tuổi</label>
                                <select name="ageRestriction" class="cgv-select">
                                    <option value="0" ${movie.ageRestriction == 0 ? 'selected' : ''}>P - Phổ biến</option>
                                    <option value="13" ${movie.ageRestriction == 13 ? 'selected' : ''}>C13 - Khán giả từ 13 tuổi</option>
                                    <option value="16" ${movie.ageRestriction == 16 ? 'selected' : ''}>C16 - Khán giả từ 16 tuổi</option>
                                    <option value="18" ${movie.ageRestriction == 18 ? 'selected' : ''}>C18 - Khán giả từ 18 tuổi</option>
                                </select>
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Ngôn ngữ</label>
                                <input type="text" name="language" class="cgv-input" value="${movie.language}" placeholder="VD: Tiếng Anh">
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Phụ đề</label>
                                <input type="text" name="subtitle" class="cgv-input" value="${movie.subtitle}" placeholder="VD: Phụ đề Tiếng Việt">
                            </div>

                            <div class="cgv-field cgv-form-full">
                                <label class="cgv-label">Thể loại phim (Có thể chọn nhiều)</label>
                                <div style="display: flex; gap: 15px; flex-wrap: wrap; margin-top: 8px;">
                                    <c:forEach items="${genreList}" var="g">
                                        <label style="cursor: pointer; display: flex; align-items: center; gap: 6px; color: var(--cgv-dark);">
                                            <input type="checkbox" name="genreIds" value="${g.genreID}" 
                                                   ${selectedGenres != null && selectedGenres.contains(g.genreID) ? 'checked' : ''}
                                                   style="width: 16px; height: 16px; accent-color: var(--cgv-red);">
                                            ${g.genreName}
                                        </label>
                                    </c:forEach>
                                </div>
                            </div>

                            <div class="cgv-field cgv-form-full">
                                <label class="cgv-label">Đạo diễn</label>
                                <input type="text" name="director" class="cgv-input" value="${movie.director}">
                            </div>

                            <div class="cgv-field cgv-form-full">
                                <label class="cgv-label">Quốc gia</label>
                                <input type="text" name="country" class="cgv-input" value="${movie.country}">
                            </div>

                            <div class="cgv-field cgv-form-full">
                                <label class="cgv-label">Diễn viên</label>
                                <input type="text" name="cast" class="cgv-input" value="${movie.cast}">
                            </div>

                            <div class="cgv-field cgv-form-full">
                                <label class="cgv-label">URL Ảnh Poster</label>
                                <input type="url" name="poster" class="cgv-input" value="${movie.poster}" readonly style="background-color: #f5f5f5;">
                            </div>

                            <div class="cgv-field cgv-form-full">
                                <label class="cgv-label">URL Trailer (Youtube)</label>
                                <input type="url" name="trailer" class="cgv-input" value="${movie.trailer}">
                            </div>

                            <div class="cgv-field cgv-form-full">
                                <label class="cgv-label">Nội dung tóm tắt *</label>
                                <textarea name="description" class="cgv-textarea" rows="4" required>${movie.description}</textarea>
                            </div>

                            <div class="cgv-field cgv-form-full">
                                <label style="display: flex; align-items: center; gap: 8px; font-weight: 500; color: var(--cgv-dark); cursor: pointer;">
                                    <input type="checkbox" name="isActive" value="true" ${movie.active ? 'checked' : ''} style="width: 18px; height: 18px; accent-color: var(--cgv-red);">
                                    Hiển thị phim lên trang chủ ngay lập tức
                                </label>
                            </div>
                        </div>

                        <div style="margin-top: 32px; padding-top: 24px; border-top: 1px solid var(--cgv-border); display: flex; justify-content: flex-end; gap: 16px;">
                            <a href="${pageContext.request.contextPath}/MovieController" class="btn--cgv-outline">Hủy bỏ</a>
                            <button type="submit" class="btn--cgv"><i class="fa-solid fa-save"></i> Cập nhật thông tin</button>
                        </div>
                    </form>

                </div>
            </div>
        </main>



        <script>
            document.getElementById('btnFetchTMDB').addEventListener('click', function () {
                // Vẫn cần Tên phim để biết đường gọi API tìm kiếm
                var movieName = document.querySelector('input[name="movieName"]').value;
                if (!movieName) {
                    alert("Vui lòng nhập Tên phim trước để hệ thống biết cần tra cứu số liệu của phim nào!");
                    return;
                }

                var btn = this;
                btn.innerText = "Đang tra cứu số liệu...";
                btn.disabled = true;

                fetch('TMDBController?query=' + encodeURIComponent(movieName))
                        .then(response => response.json())
                        .then(data => {
                            btn.innerText = "Cập nhật số liệu kinh phí & doanh thu từ TMDB";
                            btn.disabled = false;

                            if (data.error) {
                                alert("Không tìm thấy dữ liệu số của phim này trên TMDB!");
                                return;
                            }

                            // [SỰ KHÁC BIỆT]: Bỏ qua toàn bộ Tên phim, Poster, Trailer, Mô tả...
                            // CHỈ CẬP NHẬT ĐÚNG SỐ LIỆU KINH TẾ!

                            let hasUpdate = false;

                            if (data.Budget && data.Budget !== "0") {
                                let budgetInput = document.querySelector('input[name="budget"]');
                                if (budgetInput) {
                                    budgetInput.value = data.Budget;
                                    hasUpdate = true;
                                }
                            }

                            if (data.GlobalBoxOffice && data.GlobalBoxOffice !== "0") {
                                let boxOfficeInput = document.querySelector('input[name="globalBoxOffice"]');
                                if (boxOfficeInput) {
                                    boxOfficeInput.value = data.GlobalBoxOffice;
                                    hasUpdate = true;
                                }
                            }

                            if (hasUpdate) {
                                alert("Đã tải xong số liệu Kinh phí & Doanh thu thành công!");
                            } else {
                                alert("TMDB không có sẵn số liệu kinh tế của phim này.");
                            }
                        })
                        .catch(error => {
                            console.error('Error:', error);
                            btn.innerText = "Cập nhật số liệu kinh phí & doanh thu từ TMDB";
                            btn.disabled = false;
                            alert("Có lỗi mạng xảy ra khi tra cứu!");
                        });
            });
        </script>
    </body>
</html>