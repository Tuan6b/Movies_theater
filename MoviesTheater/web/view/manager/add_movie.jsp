<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Thêm Phim Mới - CGV Manager</title>

        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager-movie.css">
    </head>
    <body class="cgv-body">
        <% request.setAttribute("activeNav", "movies"); %>
        <%@ include file="/view/manager/_sidebar.jsp" %>

        <main class="cgv-main">

            <header class="cgv-header">
                <h1 class="cgv-header-title">Thêm Phim Mới</h1>

                <div class="cgv-header-right">
                    <a href="${pageContext.request.contextPath}/MovieController" class="btn--cgv-outline">
                        <i class="fa-solid fa-arrow-left"></i> Quay lại
                    </a>
                </div>
            </header>

            <div class="cgv-page" style="flex-direction: column;">
                <div class="cgv-data-wrap fade-in" style="padding: 32px; max-width: 900px; margin: 0 auto; width: 100%;">

                    <h2 class="cgv-page-title" style="margin-bottom: 24px;">Thông tin chi tiết</h2>

                    <form action="${pageContext.request.contextPath}/MovieController" method="POST">
                        <input type="hidden" name="action" value="add">

                        <div class="cgv-form-grid">
                            <div class="cgv-field">
                                <label class="cgv-label">Tên phim *</label>
                                <input type="text" name="movieName" class="cgv-input" required>
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Kinh phí sản xuất</label>
                                <input type="text" name="budget" class="cgv-input" placeholder="VD: 500 Triệu USD">
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Doanh thu toàn cầu</label>
                                <input type="text" name="globalBoxOffice" class="cgv-input" placeholder="VD: 2.8 Tỷ USD">
                            </div>



                            <div class="cgv-field">
                                <label class="cgv-label">Thời lượng (phút) *</label>
                                <input type="number" name="duration" class="cgv-input" min="40" max="300" required>
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Ngày khởi chiếu *</label>
                                <input type="date" name="releaseDate" class="cgv-input" required>
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Giới hạn độ tuổi</label>
                                <select name="ageRestriction" class="cgv-select">
                                    <c:forEach items="${ageRestrictions}" var="age">
                                        <option value="${age.value}">${age.displayName}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Ngôn ngữ</label>
                                <select name="language" class="cgv-select">
                                    <c:forEach items="${languages}" var="lang">
                                        <option value="${lang.displayName}">${lang.displayName}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Phụ đề</label>
                                <select name="subtitle" class="cgv-select">
                                    <c:forEach items="${subtitles}" var="sub">
                                        <option value="${sub.displayName}">${sub.displayName}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="cgv-field cgv-form-full">
                                <label class="cgv-label">Thể loại phim (Có thể chọn nhiều) *</label>
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
                                <input type="text" name="director" class="cgv-input">
                            </div>

                            <div class="cgv-field cgv-form-full">
                                <label class="cgv-label">Quốc gia</label>
                                <select name="country" class="cgv-select">
                                    <c:forEach items="${countries}" var="country">
                                        <option value="${country.displayName}">${country.displayName}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="cgv-field cgv-form-full">
                                <label class="cgv-label">Diễn viên</label>
                                <input type="text" name="cast" class="cgv-input">
                            </div>

                            <div class="cgv-field cgv-form-full">
                                <label class="cgv-label">URL Ảnh Poster</label>
                                <div style="display: flex; gap: 16px; align-items: flex-start;">
                                    <input type="url" name="poster" id="posterInput" class="cgv-input" placeholder="https://..." style="flex: 1"
                                           oninput="document.getElementById('posterPreview').src = this.value || 'https://via.placeholder.com/150x220?text=No+Image'">
                                    <div style="width: 150px; height: 220px; border-radius: 8px; border: 1px dashed #ccc; overflow: hidden; background: #f9f9f9; display: flex; align-items: center; justify-content: center;">
                                        <img id="posterPreview" src="https://via.placeholder.com/150x220?text=No+Image" style="width: 100%; height: 100%; object-fit: cover;">
                                    </div>
                                </div>
                            </div>

                            <div class="cgv-field cgv-form-full">
                                <label class="cgv-label">URL Trailer (Youtube)</label>
                                <input type="url" name="trailer" class="cgv-input" placeholder="https://youtube.com/...">
                            </div>

                            <div class="cgv-field cgv-form-full">
                                <label class="cgv-label">Nội dung tóm tắt *</label>
                                <textarea name="description" class="cgv-textarea" rows="4" required></textarea>
                            </div>

                            <div class="cgv-field cgv-form-full">
                                <label style="display: flex; align-items: center; gap: 8px; font-weight: 500; color: var(--cgv-dark); cursor: pointer;">
                                    <input type="checkbox" name="isActive" value="true" checked style="width: 18px; height: 18px; accent-color: var(--cgv-red);">
                                    Trạng thái hoạt động (Active)
                                </label>
                            </div>
                        </div>

                        <div style="margin-top: 32px; padding-top: 24px; border-top: 1px solid var(--cgv-border); display: flex; justify-content: flex-end; gap: 16px;">
                            <a href="${pageContext.request.contextPath}/MovieController" class="btn--cgv-outline">Hủy bỏ</a>
                            <button type="submit" class="btn--cgv"><i class="fa-solid fa-save"></i> Lưu thông tin</button>
                        </div>
                    </form>

                </div>
            </div>
        </main>

        <script src="${pageContext.request.contextPath}/js/manager-movie.js" charset="UTF-8"></script>
    </body>
</html>
