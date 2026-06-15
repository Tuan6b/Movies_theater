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
            .cgv-form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 24px; }
            .cgv-form-full { grid-column: span 2; }
        </style>
    </head>
    <body class="cgv-body">

        <aside class="cgv-sidebar">
            <div class="cgv-sidebar-top">
                <a href="${pageContext.request.contextPath}/DashboardController">
                    <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV" class="cgv-logo">
                </a>
            </div>

            <nav class="cgv-nav">
                <a href="${pageContext.request.contextPath}/DashboardController" class="cgv-nav-link">
                    <i class="fa-solid fa-chart-pie cgv-nav-icon"></i> Dashboard
                </a>
                <a href="${pageContext.request.contextPath}/MovieController" class="cgv-nav-link active">
                    <i class="fa-solid fa-film cgv-nav-icon"></i> Quản lý Phim
                </a>
                <a href="${pageContext.request.contextPath}/ScheduleController" class="cgv-nav-link">
                    <i class="fa-regular fa-calendar-days cgv-nav-icon"></i> Lịch chiếu
                </a>
                <a href="${pageContext.request.contextPath}/GenreController" class="cgv-nav-link">
                    <i class="fa-solid fa-tags cgv-nav-icon"></i> Thể loại
                </a>
            </nav>

            <div class="cgv-sidebar-bottom">
                <a href="#" class="cgv-nav-link"><i class="fa-solid fa-gear cgv-nav-icon"></i> Cài đặt</a>
                <a href="${pageContext.request.contextPath}/LogoutController" class="cgv-nav-link" style="color: var(--cgv-red);">
                    <i class="fa-solid fa-arrow-right-from-bracket cgv-nav-icon"></i> Đăng xuất
                </a>
            </div>
        </aside>

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
                                <input type="text" name="movieName" class="cgv-input" value="${movie.movieName}" readonly style="background-color: #f5f5f5;" required>
                            </div>
                            
                            <div class="cgv-field">
                                <label class="cgv-label">Ngày khởi chiếu *</label>
                                <input type="date" name="releaseDate" class="cgv-input" value="${movie.releaseDate}" readonly style="background-color: #f5f5f5;" required>
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
    </body>
</html>