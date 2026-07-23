<%-- Top navigation bar. Set request attribute "activeNav" before including. --%>
<%@ page pageEncoding="UTF-8" %>
<nav class="navbar">
    <div class="navbar-inner">
        <a href="${pageContext.request.contextPath}" class="navbar-brand">
            <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
            <span class="navbar-brand-name">Cinema Admin</span>
        </a>
        <div class="navbar-sep"></div>
        <div class="navbar-links">
               <a href="${pageContext.request.contextPath}/manager"
               class="nlink ${activeNav eq 'promotions' ? 'active' : ''}">Dashboard</a>
               <a href="${pageContext.request.contextPath}/RoomServlet"
               class="nlink ${activeNav eq 'promotions' ? 'active' : ''}">Khuyến mãi</a>
            <a href="${pageContext.request.contextPath}/manager/promotions"
               class="nlink ${activeNav eq 'promotions' ? 'active' : ''}">Khuyến mãi</a>
            <a href="${pageContext.request.contextPath}/admin/genre"
               class="nlink ${activeNav eq 'promotions' ? 'active' : ''}">Thể loai phim</a>
        </div>
        <div class="navbar-end">
            <a href="${pageContext.request.contextPath}/profile" class="navbar-profile-link">
                <span class="navbar-role">
                    <c:choose>
                        <c:when test="${not empty sessionScope.account}">${sessionScope.account.fullName}</c:when>
                        <c:otherwise>Manager</c:otherwise>
                    </c:choose>
                </span>
                <div class="topbar-avatar">MG</div>
            </a>
        </div>
    </div>
</nav>
