<%-- Sidebar include. Set request attribute "activeNav" before including. --%>
<%@ page pageEncoding="UTF-8" %>
<<<<<<< Updated upstream
<aside class="sidebar">

    <div class="sidebar-brand">
        <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV Cinema">
        <div class="sidebar-brand-text">
            <h1>Cinema Admin</h1>
        </div>
=======
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<aside class="cgv-sidebar">

    <c:set var="r" value="${sessionScope.account.roleId}" />

    <div class="cgv-sidebar-top">
        <img class="cgv-logo"
             src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png"
             alt="CGV Cinema">
>>>>>>> Stashed changes
    </div>

    <div class="sidebar-role">Manager</div>

    <nav class="sidebar-nav">

        <div class="nav-section-label">Tổng quan</div>
        <a href="${pageContext.request.contextPath}/manager"
           class="nav-item ${activeNav eq 'dashboard' ? 'active' : ''}">
            Tổng quan
        </a>

<<<<<<< Updated upstream
        <div class="nav-section-label">Nội dung</div>
        <a href="#" class="nav-item ${activeNav eq 'movies' ? 'active' : ''}">
            Phim &amp; Thể loại
        </a>
        <a href="#" class="nav-item ${activeNav eq 'ratings' ? 'active' : ''}">
            Đánh giá phim
=======
        <c:if test="${r ge 3}">
        <a href="${pageContext.request.contextPath}/manager/movies"
           class="cgv-nav-link ${activeNav eq 'movies' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="2" width="20" height="20" rx="2"/>
                <line x1="7" y1="2" x2="7" y2="22"/>
                <line x1="17" y1="2" x2="17" y2="22"/>
                <line x1="2" y1="12" x2="22" y2="12"/>
                <line x1="2" y1="7" x2="7" y2="7"/>
                <line x1="17" y1="7" x2="22" y2="7"/>
                <line x1="17" y1="17" x2="22" y2="17"/>
                <line x1="2" y1="17" x2="7" y2="17"/>
            </svg>
            Movies
>>>>>>> Stashed changes
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
        </c:if>

<<<<<<< Updated upstream
        <div class="nav-section-label">Kinh doanh</div>
        <a href="#" class="nav-item ${activeNav eq 'booking' ? 'active' : ''}">
            Thanh toán
        </a>
=======
        <c:if test="${r ge 4}">
>>>>>>> Stashed changes
        <a href="${pageContext.request.contextPath}/manager/promotions"
           class="nav-item done ${activeNav eq 'promotions' ? 'active' : ''}">
            Khuyến mãi
        </a>
        <a href="#" class="nav-item ${activeNav eq 'stats' ? 'active' : ''}">
            Thống kê
        </a>

<<<<<<< Updated upstream
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
=======
        <a href="${pageContext.request.contextPath}/manager/employees"
           class="cgv-nav-link ${activeNav eq 'employees' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
            Employees
        </a>

        <a href="${pageContext.request.contextPath}/RoomServlet"
           class="cgv-nav-link ${activeNav eq 'rooms' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2"/>
                <path d="M3 9h18M9 21V9"/>
            </svg>
            Rooms
        </a>

        <a href="${pageContext.request.contextPath}/manager/analytics"
           class="cgv-nav-link ${activeNav eq 'analytics' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="20" x2="18" y2="10"/>
                <line x1="12" y1="20" x2="12" y2="4"/>
                <line x1="6" y1="20" x2="6" y2="14"/>
            </svg>
            Analytics
        </a>
        </c:if>
    </nav>

    <div class="cgv-sidebar-bottom">
        <c:if test="${r ge 4}">
        <a href="${pageContext.request.contextPath}/manager/settings"
           class="cgv-nav-link ${activeNav eq 'settings' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="3"/>
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06
                         a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09
                         A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83
                         l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09
                         A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83
                         l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09
                         a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83
                         l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09
                         a1.65 1.65 0 0 0-1.51 1z"/>
            </svg>
            Settings
        </a>
        </c:if>

        <a href="${pageContext.request.contextPath}/Logout" class="cgv-nav-link">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                <polyline points="16 17 21 12 16 7"/>
                <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            Logout
        </a>
>>>>>>> Stashed changes
    </div>

</aside>
