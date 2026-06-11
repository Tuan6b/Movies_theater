<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<aside class="cgv-sidebar" style="background: linear-gradient(180deg, #1a0a0a 0%, #0d0505 100%); border-right: 1px solid rgba(220, 38, 38, 0.25);">

    <div class="cgv-sidebar-top" style="border-bottom: 1px solid rgba(220, 38, 38, 0.2);">
        <img class="cgv-logo" src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV Cinema">
        <div style="font-size:9px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(220,80,80,0.7);margin-top:4px;">Admin Panel</div>
    </div>

    <nav class="cgv-nav">
        <a href="${pageContext.request.contextPath}/admin" class="cgv-nav-link ${activeNav eq 'dashboard' ? 'active' : ''}"
           style="${activeNav eq 'dashboard' ? 'background:rgba(220,38,38,0.2);color:#fff;' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
                <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
            </svg>
            Dashboard
        </a>

        <div style="font-size:9px;font-weight:700;letter-spacing:1.5px;text-transform:uppercase;color:rgba(220,80,80,0.5);padding:16px 16px 6px;">Content</div>

        <a href="${pageContext.request.contextPath}/manager/movies" class="cgv-nav-link ${activeNav eq 'movies' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="2" width="20" height="20" rx="2"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/>
                <line x1="2" y1="12" x2="22" y2="12"/><line x1="2" y1="7" x2="7" y2="7"/><line x1="17" y1="7" x2="22" y2="7"/>
                <line x1="17" y1="17" x2="22" y2="17"/><line x1="2" y1="17" x2="7" y2="17"/>
            </svg>
            Movies
        </a>

        <a href="${pageContext.request.contextPath}/admin/genre" class="cgv-nav-link ${activeNav eq 'genre' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
            </svg>
            Genres
        </a>

        <a href="${pageContext.request.contextPath}/manager/schedules" class="cgv-nav-link ${activeNav eq 'schedules' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
            Schedules
        </a>

        <div style="font-size:9px;font-weight:700;letter-spacing:1.5px;text-transform:uppercase;color:rgba(220,80,80,0.5);padding:16px 16px 6px;">Business</div>

        <a href="${pageContext.request.contextPath}/manager/promotions" class="cgv-nav-link ${activeNav eq 'promotions' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/>
            </svg>
            Promotions
        </a>

        <a href="${pageContext.request.contextPath}/RoomServlet" class="cgv-nav-link ${activeNav eq 'rooms' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18M9 21V9"/>
            </svg>
            Rooms
        </a>

        <a href="${pageContext.request.contextPath}/manager/analytics" class="cgv-nav-link ${activeNav eq 'analytics' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>
            </svg>
            Analytics
        </a>

        <a href="${pageContext.request.contextPath}/manager/food" class="cgv-nav-link ${activeNav eq 'food' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 8h1a4 4 0 0 1 0 8h-1"/><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/><line x1="6" y1="1" x2="6" y2="4"/><line x1="10" y1="1" x2="10" y2="4"/><line x1="14" y1="1" x2="14" y2="4"/>
            </svg>
            Food &amp; Menu
        </a>

        <div style="font-size:9px;font-weight:700;letter-spacing:1.5px;text-transform:uppercase;color:rgba(220,80,80,0.5);padding:16px 16px 6px;">System</div>

        <a href="${pageContext.request.contextPath}/manager/users" class="cgv-nav-link ${activeNav eq 'users' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
            Users
        </a>

        <a href="${pageContext.request.contextPath}/manager/checkin" class="cgv-nav-link ${activeNav eq 'checkin' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
            </svg>
            Check-in
        </a>

        <a href="${pageContext.request.contextPath}/manager/settings" class="cgv-nav-link ${activeNav eq 'settings' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33..."/>
            </svg>
            Settings
        </a>
    </nav>

    <div class="cgv-sidebar-bottom" style="border-top: 1px solid rgba(220, 38, 38, 0.2);">
        <a href="${pageContext.request.contextPath}/" class="cgv-nav-link" style="color:rgba(220,180,180,0.6);">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>
            </svg>
            Vào trang chủ
        </a>
        <a href="${pageContext.request.contextPath}/Logout" class="cgv-nav-link">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            Đăng xuất
        </a>
    </div>
</aside>
