<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<% request.setAttribute("activeNav", "users"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý người dùng — CGV System Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .search-bar { display: flex; gap: 12px; margin-bottom: 20px; }
        .search-bar input { flex:1; padding:10px 14px; border:1px solid var(--cgv-border); border-radius:8px; font-size:14px; outline:none; }
        .search-bar button { padding:10px 24px; background:var(--cgv-red); color:#fff; border:none; border-radius:8px; font-weight:600; cursor:pointer; }
        .role-badge { display:inline-block; padding:2px 10px; border-radius:12px; font-size:11px; font-weight:700; }
        .role-customer { background:#e8f5e9; color:#2e7d32; }
        .role-employee { background:#fff3e0; color:#e65100; }
        .role-manager { background:#e3f2fd; color:#1565c0; }
        .role-admin { background:#fce4ec; color:#c62828; }
        .blocked { opacity:0.6; background:#fef2f2; }
        .inline-form { display:inline; }
        .inline-form select, .inline-form button { padding:4px 8px; font-size:12px; border-radius:4px; border:1px solid var(--cgv-border); }
        .inline-form button { background:var(--cgv-red); color:#fff; border:none; cursor:pointer; }
        .section-title { font-size:18px; font-weight:700; margin-bottom:16px; padding-bottom:8px; border-bottom:2px solid var(--cgv-red); }
        .section-title.customers { color:#2e7d32; border-color:#2e7d32; }
        .section-title.staff { color:#1565c0; border-color:#1565c0; }
        table { width:100%; border-collapse:collapse; font-size:14px; margin-bottom:40px; }
        table thead tr { background:#fafafa; border-bottom:2px solid var(--cgv-border); }
        table th { padding:12px; text-align:left; }
        table td { padding:12px; }
        table tbody tr { border-bottom:1px solid var(--cgv-border); }
    </style>
</head>
<body class="cgv-body">
<%@ include file="_sidebar.jsp" %>
<div class="cgv-main">
    <header class="cgv-header">
        <h1 class="cgv-header-title">Quản lý người dùng</h1>
        <div class="cgv-header-right">
            <div class="cgv-user-wrap">
                <div class="cgv-avatar">SA</div>
                <span class="cgv-user-name">${sessionScope.account.fullName}</span>
            </div>
        </div>
    </header>
    <div class="cgv-page">
        <div class="cgv-list-wrap">

            <c:if test="${not empty roleStats}">
            <div class="stat-cards" style="display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:12px;margin-bottom:24px;">
                <c:forEach var="entry" items="${roleStats}">
                    <div class="stat-card" style="background:#fff;border:1px solid var(--cgv-border);border-radius:8px;padding:16px;text-align:center;">
                        <div class="num" style="font-size:24px;font-weight:700;">${entry.value}</div>
                        <div class="label" style="font-size:11px;color:var(--cgv-text-muted);text-transform:uppercase;margin-top:4px;">${entry.key}</div>
                    </div>
                </c:forEach>
            </div>
            </c:if>

            <h2 class="section-title customers">Customers</h2>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Email</th>
                        <th>Tên</th>
                        <th>Vai trò</th>
                        <th>Trạng thái</th>
                        <th>Ngày tạo</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="u" items="${customers}">
                    <tr style="${u.isBlocked ? 'opacity:0.6;background:#fef2f2;' : ''}">
                        <td>${u.accountId}</td>
                        <td>${u.email}</td>
                        <td>${u.fullName}</td>
                        <td><span class="role-badge role-customer">Customer</span></td>
                        <td>
                            <c:choose>
                                <c:when test="${u.isBlocked}"><span style="color:#b91c1c;font-weight:600;">Bị khóa</span></c:when>
                                <c:otherwise><span style="color:#16a34a;font-weight:600;">Hoạt động</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>${u.createdAt}</td>
                        <td>
                            <form class="inline-form" method="post" action="${pageContext.request.contextPath}/admin/users" style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;">
                                <input type="hidden" name="userId" value="${u.accountId}">
                                <c:choose>
                                    <c:when test="${u.isBlocked}">
                                        <button type="submit" name="action" value="unblock" style="background:#16a34a;color:#fff;border:none;padding:4px 10px;border-radius:4px;cursor:pointer;font-size:12px;">Mở khóa</button>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="submit" name="action" value="block" style="background:#b91c1c;color:#fff;border:none;padding:4px 10px;border-radius:4px;cursor:pointer;font-size:12px;" onclick="return confirm('Khóa tài khoản ${u.email}?')">Khóa</button>
                                    </c:otherwise>
                                </c:choose>
                                <select name="roleId" style="padding:4px 6px;font-size:12px;border:1px solid var(--cgv-border);border-radius:4px;">
                                    <option value="2" ${u.roleId == 2 ? 'selected' : ''}>Customer</option>
                                    <option value="3" ${u.roleId == 3 ? 'selected' : ''}>Employee</option>
                                    <option value="4" ${u.roleId == 4 ? 'selected' : ''}>Manager</option>
                                    <option value="5" ${u.roleId == 5 ? 'selected' : ''}>Admin</option>
                                </select>
                                <button type="submit" name="action" value="role" style="background:var(--cgv-red);color:#fff;border:none;padding:4px 10px;border-radius:4px;cursor:pointer;font-size:12px;">Đổi</button>
                            </form>
                        </td>
                    </tr>
                    </c:forEach>
                    <c:if test="${empty customers}">
                    <tr><td colspan="7" style="padding:40px;text-align:center;color:var(--cgv-text-muted);">Không có khách hàng nào.</td></tr>
                    </c:if>
                </tbody>
            </table>

            <h2 class="section-title staff">Staff / Quản trị viên</h2>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Email</th>
                        <th>Tên</th>
                        <th>Vai trò</th>
                        <th>Trạng thái</th>
                        <th>Ngày tạo</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="u" items="${staff}">
                    <tr style="${u.isBlocked ? 'opacity:0.6;background:#fef2f2;' : ''}">
                        <td>${u.accountId}</td>
                        <td>${u.email}</td>
                        <td>${u.fullName}</td>
                        <td>
                            <c:choose>
                                <c:when test="${u.roleId == 3}"><span class="role-badge role-employee">Employee</span></c:when>
                                <c:when test="${u.roleId == 4}"><span class="role-badge role-manager">Manager</span></c:when>
                                <c:when test="${u.roleId == 5}"><span class="role-badge role-admin">Admin</span></c:when>
                                <c:otherwise>Unknown</c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${u.isBlocked}"><span style="color:#b91c1c;font-weight:600;">Bị khóa</span></c:when>
                                <c:otherwise><span style="color:#16a34a;font-weight:600;">Hoạt động</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>${u.createdAt}</td>
                        <td>
                            <form class="inline-form" method="post" action="${pageContext.request.contextPath}/admin/users" style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;">
                                <input type="hidden" name="userId" value="${u.accountId}">
                                <c:choose>
                                    <c:when test="${u.isBlocked}">
                                        <button type="submit" name="action" value="unblock" style="background:#16a34a;color:#fff;border:none;padding:4px 10px;border-radius:4px;cursor:pointer;font-size:12px;">Mở khóa</button>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="submit" name="action" value="block" style="background:#b91c1c;color:#fff;border:none;padding:4px 10px;border-radius:4px;cursor:pointer;font-size:12px;" onclick="return confirm('Khóa tài khoản ${u.email}?')">Khóa</button>
                                    </c:otherwise>
                                </c:choose>
                                <select name="roleId" style="padding:4px 6px;font-size:12px;border:1px solid var(--cgv-border);border-radius:4px;">
                                    <option value="2" ${u.roleId == 2 ? 'selected' : ''}>Customer</option>
                                    <option value="3" ${u.roleId == 3 ? 'selected' : ''}>Employee</option>
                                    <option value="4" ${u.roleId == 4 ? 'selected' : ''}>Manager</option>
                                    <option value="5" ${u.roleId == 5 ? 'selected' : ''}>Admin</option>
                                </select>
                                <button type="submit" name="action" value="role" style="background:var(--cgv-red);color:#fff;border:none;padding:4px 10px;border-radius:4px;cursor:pointer;font-size:12px;">Đổi</button>
                            </form>
                        </td>
                    </tr>
                    </c:forEach>
                    <c:if test="${empty staff}">
                    <tr><td colspan="7" style="padding:40px;text-align:center;color:var(--cgv-text-muted);">Không có nhân viên nào.</td></tr>
                    </c:if>
                </tbody>
            </table>

        </div>
    </div>
</div>
</body>
</html>
