<%-- Sidebar include. Set request attribute "activeNav" before including. --%>
<%@ page pageEncoding="UTF-8" %>
<aside class="sidebar">

    <div class="sidebar-brand">
        <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV Cinema">
        <div class="sidebar-brand-text">
            <h1>Cinema Admin</h1>
        </div>
    </div>

    <div class="sidebar-role">Manager</div>

    <nav class="sidebar-nav">

        <div class="nav-section-label">Tổng quan</div>
        <a href="${pageContext.request.contextPath}/manager"
           class="nav-item ${activeNav eq 'dashboard' ? 'active' : ''}">
            Tổng quan
        </a>

        <div class="nav-section-label">Nội dung</div>
        <a href="#" class="nav-item ${activeNav eq 'movies' ? 'active' : ''}">
            Phim &amp; Thể loại
        </a>
        <a href="#" class="nav-item ${activeNav eq 'ratings' ? 'active' : ''}">
            Đánh giá phim
        </a>

        <div class="nav-section-label">Vận Hành</div>
        <a href="${pageContext.request.contextPath}/RoomServlet" class="nav-item ${activeNav eq 'rooms' ? 'active' : ''}">
            phòng chiếu
        </a>
        <a href="#" class="nav-item ${activeNav eq 'schedules' ? 'active' : ''}">
            Lịch chiếu
        </a>
        <a href="#" class="nav-item ${activeNav eq 'food' ? 'active' : ''}">
            Đồ ăn &amp; Menu
        </a>

        <div class="nav-section-label">Kinh doanh</div>
        <a href="#" class="nav-item ${activeNav eq 'booking' ? 'active' : ''}">
            Thanh toán
        </a>
        <a href="${pageContext.request.contextPath}/manager/promotions"
           class="nav-item done ${activeNav eq 'promotions' ? 'active' : ''}">
            Khuyến mãi
        </a>
        <a href="#" class="nav-item ${activeNav eq 'stats' ? 'active' : ''}">
            Thống kê
        </a>

        <div class="nav-section-label">Hệ thống</div>
        <a href="#" class="nav-item ${activeNav eq 'employees' ? 'active' : ''}">
            Nhân viên
        </a>
        <a href="#" class="nav-item ${activeNav eq 'accounts' ? 'active' : ''}">
            Tài khoản
        </a>
        <a href="#" class="nav-item ${activeNav eq 'config' ? 'active' : ''}">
            Cấu hình hệ thống
        </a>

    </nav>

    <div class="sidebar-footer">
        Cinema Manager v1.0
    </div>

</aside>
