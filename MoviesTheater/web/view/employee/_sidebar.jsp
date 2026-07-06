<%-- Employee sidebar. Set request attribute "activeNav" before including. --%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<aside class="cgv-sidebar">

    <div class="cgv-sidebar-top">
        <img class="cgv-logo"
             src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png"
             alt="CGV Cinema">
    </div>

    <nav class="cgv-nav">
        <a href="${pageContext.request.contextPath}/employee"
           class="cgv-nav-link ${activeNav eq 'dashboard' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="7" height="7"/>
                <rect x="14" y="3" width="7" height="7"/>
                <rect x="14" y="14" width="7" height="7"/>
                <rect x="3" y="14" width="7" height="7"/>
            </svg>
            Dashboard
        </a>

        <a href="${pageContext.request.contextPath}/employee/schedules"
           class="cgv-nav-link ${activeNav eq 'schedules' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
            Schedules
        </a>

        <a href="${pageContext.request.contextPath}/employee/checkin"
           class="cgv-nav-link ${activeNav eq 'checkin' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="9 11 12 14 22 4"/>
                <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
            </svg>
            Check-in
        </a>

        <a href="${pageContext.request.contextPath}/employee/my-shifts"
           class="cgv-nav-link ${activeNav eq 'my-shifts' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8"  y1="2" x2="8"  y2="6"/>
                <line x1="3"  y1="10" x2="21" y2="10"/>
                <polyline points="9 16 11 18 15 14"/>
            </svg>
            Ca Làm Việc
        </a>

        <a href="${pageContext.request.contextPath}/employee/profile"
           class="cgv-nav-link ${activeNav eq 'profile' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
            </svg>
            My Profile
        </a>
    </nav>

    <div class="cgv-sidebar-bottom">
        <a href="${pageContext.request.contextPath}/Logout" class="cgv-nav-link">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                <polyline points="16 17 21 12 16 7"/>
                <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            Logout
        </a>
    </div>

</aside>
