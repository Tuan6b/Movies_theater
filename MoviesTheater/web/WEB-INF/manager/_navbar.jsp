<%-- Top navigation bar. Set request attribute "activeNav" before including. --%>
<%@ page pageEncoding="UTF-8" %>
<nav class="navbar">
    <div class="navbar-inner">
        <a href="${pageContext.request.contextPath}/manager" class="navbar-brand">
            <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
            <span class="navbar-brand-name">Cinema Admin</span>
        </a>
        <div class="navbar-sep"></div>
        <div class="navbar-links">
            <a href="${pageContext.request.contextPath}/manager"
               class="nlink ${activeNav eq 'dashboard' ? 'active' : ''}">Dashboard</a>
            <a href="${pageContext.request.contextPath}/RoomServlet"
               class="nlink ${activeNav eq 'rooms' ? 'active' : ''}">Phòng chiếu</a>
            <a href="${pageContext.request.contextPath}/manager/promotions"
               class="nlink done ${activeNav eq 'promotions' ? 'active' : ''}">Khuyến mãi</a>
        </div>
        <div class="navbar-end">
            <span class="navbar-role">Manager</span>
            <div class="topbar-avatar">MG</div>
        </div>
    </div>
</nav>
