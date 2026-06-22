<%-- CGV Sidebar navigation. Set request attribute "activeNav" before including. --%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<aside class="cgv-sidebar">

    <c:set var="r" value="${sessionScope.account.roleId}" />

    <div class="cgv-sidebar-top">
        <a href="${pageContext.request.contextPath}/">
            <img class="cgv-logo"
                 src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png"
                 alt="CGV Cinema">
        </a>
    </div>

    <nav class="cgv-nav">
        <a href="${pageContext.request.contextPath}/manager"
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
        </a>

        <a href="${pageContext.request.contextPath}/manager/schedules"
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
        </c:if>

        <c:if test="${r ge 4}">
        <a href="${pageContext.request.contextPath}/manager/promotions"
           class="cgv-nav-link ${activeNav eq 'promotions' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
                <line x1="7" y1="7" x2="7.01" y2="7"/>
            </svg>
            Promotions
        </a>

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

        <a href="${pageContext.request.contextPath}/manager/shifts"
           class="cgv-nav-link ${activeNav eq 'shifts' ? 'active' : ''}">
            <svg class="cgv-nav-icon" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
            </svg>
            Shifts
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
