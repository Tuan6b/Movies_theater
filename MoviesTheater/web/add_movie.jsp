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
        <style>
            /* Class hỗ trợ chia form 2 cột cho ngay ngắn */
            .cgv-form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 24px; }
            .cgv-form-full { grid-column: span 2; }
        </style>
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
                                <label class="cgv-label">Ngày khởi chiếu *</label>
                                <input type="date" name="releaseDate" class="cgv-input" required>
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Thời lượng (phút) *</label>
                                <input type="number" name="duration" class="cgv-input" min="1" required>
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Giới hạn độ tuổi</label>
                                <select name="ageRestriction" class="cgv-select">
                                    <option value="0">P - Phổ biến</option>
                                    <option value="13">C13 - Khán giả từ 13 tuổi</option>
                                    <option value="16">C16 - Khán giả từ 16 tuổi</option>
                                    <option value="18">C18 - Khán giả từ 18 tuổi</option>
                                </select>
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Ngôn ngữ</label>
                                <input type="text" name="language" class="cgv-input" placeholder="VD: Tiếng Anh">
                            </div>
                            
                            <div class="cgv-field">
                                <label class="cgv-label">Phụ đề</label>
                                <input type="text" name="subtitle" class="cgv-input" placeholder="VD: Phụ đề Tiếng Việt">
                            </div>

                            <div class="cgv-field cgv-form-full">
                                <label class="cgv-label">Diễn viên</label>
                                <input type="text" name="cast" class="cgv-input">
                            </div>

                            <div class="cgv-field cgv-form-full">
                                <label class="cgv-label">URL Ảnh Poster *</label>
                                <input type="url" name="poster" class="cgv-input" placeholder="https://..." required>
                            </div>
                            
                            <div class="cgv-field cgv-form-full">
                                <label class="cgv-label">Nội dung tóm tắt *</label>
                                <textarea name="description" class="cgv-textarea" rows="4" required></textarea>
                            </div>
                            
                            <div class="cgv-field cgv-form-full">
                                <label style="display: flex; align-items: center; gap: 8px; font-weight: 500; color: var(--cgv-dark); cursor: pointer;">
                                    <input type="checkbox" name="isActive" value="true" checked style="width: 18px; height: 18px; accent-color: var(--cgv-red);">
                                    Hiển thị phim lên trang chủ (Active)
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

    </body>
</html>