<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "dashboard"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>User &amp; Role Management — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">User &amp; Roles</h1>
        <div class="cgv-header-right">
            <div class="cgv-search-wrap">
                <svg class="cgv-search-icon" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8"/>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                </svg>
                <input class="cgv-search" type="text" placeholder="Search users..." value="${param.q}" name="q">
            </div>
            <div class="cgv-header-actions">
                <div class="cgv-header-divider"></div>
                <div class="cgv-user-wrap">
                    <div class="cgv-avatar">
                        <c:choose>
                            <c:when test="${not empty sessionScope.LOGIN_USER}">MG</c:when>
                            <c:otherwise>MG</c:otherwise>
                        </c:choose>
                    </div>
                    <span class="cgv-user-name">
                        <c:choose>
                            <c:when test="${not empty sessionScope.LOGIN_USER}">${sessionScope.LOGIN_USER.fullName}</c:when>
                            <c:otherwise>Manager</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>
        </div>
    </header>

    <div class="cgv-page">

        <div class="cgv-table-wrap">

            <c:if test="${not empty flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${flashSuccess}</div>
            </c:if>
            <c:if test="${not empty flashError}">
                <div class="cgv-alert cgv-alert-danger">${flashError}</div>
            </c:if>

            <div class="cgv-toolbar">
                <div class="cgv-pills">
                    <a href="?role=all"  class="cgv-pill ${empty param.role || param.role eq 'all' ? 'active' : ''}">All Users</a>
                    <a href="?role=admin"   class="cgv-pill ${param.role eq 'admin'   ? 'active' : ''}">Admin</a>
                    <a href="?role=manager" class="cgv-pill ${param.role eq 'manager' ? 'active' : ''}">Manager</a>
                    <a href="?role=staff"   class="cgv-pill ${param.role eq 'staff'   ? 'active' : ''}">Staff</a>
                </div>
                <a href="?action=add" class="btn--cgv">
                    <svg width="10" height="10" viewBox="0 0 12 12" fill="none"
                         stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                        <line x1="6" y1="1" x2="6" y2="11"/><line x1="1" y1="6" x2="11" y2="6"/>
                    </svg>
                    Add User
                </a>
            </div>

            <div class="cgv-data-wrap">
                <div class="cgv-data-toolbar">
                    <form method="get" style="display:flex;gap:10px;align-items:center;flex:1;flex-wrap:wrap;">
                        <input class="cgv-input" style="max-width:240px;height:38px;"
                               type="text" name="q" placeholder="Search name or email…"
                               value="${param.q}">
                        <select class="cgv-select" style="max-width:160px;height:38px;" name="role">
                            <option value="">All Roles</option>
                            <option value="ADMIN"   ${param.role eq 'ADMIN'   ? 'selected' : ''}>Admin</option>
                            <option value="MANAGER" ${param.role eq 'MANAGER' ? 'selected' : ''}>Manager</option>
                            <option value="STAFF"   ${param.role eq 'STAFF'   ? 'selected' : ''}>Staff</option>
                            <option value="CUSTOMER"${param.role eq 'CUSTOMER'? 'selected' : ''}>Customer</option>
                        </select>
                        <button type="submit" class="btn--cgv-outline">Filter</button>
                    </form>
                </div>

                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Full Name</th>
                            <th>Email</th>
                            <th>Role</th>
                            <th>Status</th>
                            <th>Joined</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty userList}">
                                <c:forEach var="u" items="${userList}" varStatus="st">
                                    <tr>
                                        <td style="color:rgba(94,63,58,0.5);font-size:12px;">${st.index + 1}</td>
                                        <td style="font-weight:500;">${u.fullName}</td>
                                        <td style="color:rgba(94,63,58,0.7);">${u.email}</td>
                                        <td>
                                            <span class="cgv-badge ${u.role eq 'ADMIN' ? 'danger' : u.role eq 'MANAGER' ? 'upcoming' : 'inactive'}">
                                                ${u.role}
                                            </span>
                                        </td>
                                        <td>
                                            <span class="cgv-badge ${u.active ? 'active' : 'inactive'}">
                                                ${u.active ? 'Active' : 'Inactive'}
                                            </span>
                                        </td>
                                        <td style="color:rgba(94,63,58,0.6);font-size:13px;">${u.createdAt}</td>
                                        <td>
                                            <div style="display:flex;gap:8px;">
                                                <a href="?action=edit&id=${u.userId}" class="btn--cgv-outline">Edit</a>
                                                <form method="post" style="display:inline;">
                                                    <input type="hidden" name="action" value="${u.active ? 'deactivate' : 'activate'}">
                                                    <input type="hidden" name="id" value="${u.userId}">
                                                    <button type="submit" class="btn--cgv-outline"
                                                            onclick="return confirm('${u.active ? 'Deactivate' : 'Activate'} this user?')">
                                                        ${u.active ? 'Deactivate' : 'Activate'}
                                                    </button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="7" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">No users found.</td></tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>

                <div class="cgv-pager">
                    <span>Showing ${not empty userList ? userList.size() : 0} of ${not empty totalUsers ? totalUsers : 0} users</span>
                    <div class="cgv-pager-pages">
                        <c:forEach begin="1" end="${not empty totalPages ? totalPages : 1}" var="p">
                            <button class="cgv-pager-btn ${p eq currentPage ? 'active' : ''}"
                                    onclick="location.href='?page=${p}&q=${param.q}&role=${param.role}'">${p}</button>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>

        <aside class="cgv-aside">
            <div class="cgv-stats-section">
                <div class="cgv-aside-heading">OVERVIEW</div>
                <div class="cgv-stats-group">
                    <div>
                        <div class="cgv-stat-num">${not empty totalUsers ? totalUsers : '248'}</div>
                        <div class="cgv-stat-key">TOTAL ACCOUNTS</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num amber">${not empty staffCount ? staffCount : '12'}</div>
                        <div class="cgv-stat-key">STAFF MEMBERS</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num red">${not empty lockedCount ? lockedCount : '3'}</div>
                        <div class="cgv-stat-key">LOCKED ACCOUNTS</div>
                    </div>
                </div>
            </div>
            <div class="cgv-aside-divider">
                <div class="cgv-aside-heading">RECENT ACTIVITY</div>
                <div class="cgv-events-list">
                    <c:choose>
                        <c:when test="${not empty recentActivity}">
                            <c:forEach var="a" items="${recentActivity}">
                                <div>
                                    <div class="cgv-event-title">${a.title}</div>
                                    <div class="cgv-event-desc">${a.description}</div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div>
                                <div class="cgv-event-title">New Registration</div>
                                <div class="cgv-event-desc">user@email.com signed up</div>
                            </div>
                            <div>
                                <div class="cgv-event-title">Role Updated</div>
                                <div class="cgv-event-desc">Staff role assigned to Minh</div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
