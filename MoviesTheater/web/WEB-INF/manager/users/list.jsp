<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "users"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>User &amp; Role Management — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .bulk-bar { display:none; background:#fff; border:1px solid var(--cgv-border); border-radius:8px; padding:12px 16px; margin-bottom:16px; align-items:center; gap:10px; flex-wrap:wrap; }
        .bulk-bar.visible { display:flex; }
        .bulk-bar .count { font-size:13px; font-weight:600; color:var(--cgv-text); margin-right:8px; }
    </style>
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
                    <div class="cgv-avatar">MG</div>
                    <span class="cgv-user-name">
                        ${sessionScope.account.profile.fullName}
                    </span>
                </div>
            </div>
        </div>
    </header>

    <div class="cgv-page">
        <div class="cgv-table-wrap">

            <c:if test="${not empty flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${flashSuccess}</div>
                <% session.removeAttribute("flashSuccess"); %>
            </c:if>
            <c:if test="${not empty flashError}">
                <div class="cgv-alert cgv-alert-danger">${flashError}</div>
                <% session.removeAttribute("flashError"); %>
            </c:if>

            <div class="cgv-toolbar">
                <div class="cgv-pills">
                    <a href="?role=all"  class="cgv-pill ${empty param.role || param.role eq 'all' ? 'active' : ''}">All Users</a>
                    <a href="?role=admin"   class="cgv-pill ${param.role eq 'admin'   ? 'active' : ''}">Admin</a>
                    <a href="?role=manager" class="cgv-pill ${param.role eq 'manager' ? 'active' : ''}">Manager</a>
                    <a href="?role=staff"   class="cgv-pill ${param.role eq 'staff'   ? 'active' : ''}">Staff</a>
                </div>
                <c:if test="${isAdmin}">
                <a href="?action=add" class="btn--cgv">
                    <svg width="10" height="10" viewBox="0 0 12 12" fill="none"
                         stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                        <line x1="6" y1="1" x2="6" y2="11"/><line x1="1" y1="6" x2="11" y2="6"/>
                    </svg>
                    Add User
                </a>
                <a href="${pageContext.request.contextPath}/manager/audit-log" class="btn--cgv-outline">
                    Audit Log
                </a>
                <a href="${pageContext.request.contextPath}/manager/deletion-requests" class="btn--cgv-outline">
                    Delete Requests
                </a>
                <a href="${pageContext.request.contextPath}/manager/users/export?q=${param.q}&role=${param.role}" class="btn--cgv-outline">
                    Export CSV
                </a>
                </c:if>
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

                <c:if test="${isAdmin}">
                <form id="bulkForm" method="post">
                <div id="bulkBar" class="bulk-bar">
                    <span class="count"><span id="selectedCount">0</span> user(s) selected</span>
                    <button type="submit" name="action" value="bulkBlock" class="btn--cgv-outline" onclick="return confirm('Block selected users?')">Block</button>
                    <button type="submit" name="action" value="bulkUnblock" class="btn--cgv-outline" onclick="return confirm('Unblock selected users?')">Unblock</button>
                    <select name="bulkRole" class="cgv-select" style="height:32px;font-size:12px;max-width:140px;">
                        <option value="">Change role to…</option>
                        <option value="2">Customer</option>
                        <option value="3">Employee</option>
                        <option value="4">Manager</option>
                    </select>
                    <button type="submit" name="action" value="bulkRole" class="btn--cgv-outline" onclick="return confirm('Change role for selected users?')">Apply Role</button>
                </div>
                </c:if>

                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <c:if test="${isAdmin}"><th style="width:36px;"><input type="checkbox" id="selectAll" onchange="toggleAll()"></th></c:if>
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
                                        <c:if test="${isAdmin}"><td><input type="checkbox" name="selectedIds" value="${u.accountId}" class="rowCheckbox" onchange="updateBulkBar()"></td></c:if>
                                        <td style="color:rgba(94,63,58,0.5);font-size:12px;">${st.index + 1}</td>
                                        <td style="font-weight:500;">${u.profile.fullName}</td>
                                        <td style="color:rgba(94,63,58,0.7);">${u.email}</td>
                                        <td>
                                            <span class="cgv-badge ${u.roleName eq 'Admin' ? 'danger' : u.roleName eq 'Manager' ? 'upcoming' : 'inactive'}">
                                                ${u.roleName}
                                            </span>
                                        </td>
                                        <td>
                                            <span class="cgv-badge ${not u.isBlocked ? 'active' : 'inactive'}">
                                                ${not u.isBlocked ? 'Active' : 'Inactive'}
                                            </span>
                                        </td>
                                        <td style="color:rgba(94,63,58,0.6);font-size:13px;">${u.createdAt}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${isAdmin}">
                                                <div style="display:flex;gap:8px;">
                                                    <a href="?action=edit&id=${u.accountId}" class="btn--cgv-outline">Edit</a>
                                                    <form method="post" style="display:inline;">
                                                        <input type="hidden" name="action" value="toggleBlock">
                                                        <input type="hidden" name="id" value="${u.accountId}">
                                                        <button type="submit" class="btn--cgv-outline"
                                                                onclick="return confirm('${not u.isBlocked ? 'Block' : 'Unblock'} this user?')">
                                                            ${not u.isBlocked ? 'Block' : 'Unblock'}
                                                        </button>
                                                    </form>
                                                </div>
                                                </c:when>
                                                <c:otherwise>
                                                <span style="color:rgba(94,63,58,0.4);font-size:12px;">—</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="8" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">No users found.</td></tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>

                <c:if test="${isAdmin}">
                </form>
                </c:if>

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
                                    <div class="cgv-event-title">${a.profile.fullName} (${a.roleName})</div>
                                    <div class="cgv-event-desc">${a.email} — joined ${a.createdAt}</div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div>
                                <div class="cgv-event-title">No recent activity</div>
                                <div class="cgv-event-desc">New user registrations will appear here.</div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </aside>
    </div>
</div>

<script>
function toggleAll() {
    var checked = document.getElementById('selectAll').checked;
    document.querySelectorAll('.rowCheckbox').forEach(function(cb) { cb.checked = checked; });
    updateBulkBar();
}
function updateBulkBar() {
    var checked = document.querySelectorAll('.rowCheckbox:checked').length;
    var bar = document.getElementById('bulkBar');
    document.getElementById('selectedCount').textContent = checked;
    if (checked > 0) { bar.classList.add('visible'); } else { bar.classList.remove('visible'); }
}
</script>
</body>
</html>
