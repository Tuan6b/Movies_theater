<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "users"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Edit User — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .edit-form { max-width: 520px; margin: 40px auto; background: #fff; border-radius: 16px; padding: 36px 32px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
        .edit-form h2 { font-size: 20px; margin-bottom: 24px; }
        .edit-field { margin-bottom: 18px; }
        .edit-field label { display: block; font-size: 13px; font-weight: 600; color: var(--cgv-text-muted); margin-bottom: 4px; }
        .edit-field .value { font-size: 15px; padding: 8px 0; color: var(--cgv-text); }
        .edit-actions { display: flex; gap: 12px; margin-top: 28px; }
        .cgv-badge-blocked { background: #fef2f2; color: #b91c1c; padding: 2px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
        .cgv-badge-active { background: #f0fdf4; color: #15803d; padding: 2px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
    </style>
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">
    <header class="cgv-header">
        <h1 class="cgv-header-title">Edit User</h1>
        <div class="cgv-header-right">
            <div class="cgv-user-wrap">
                <div class="cgv-avatar">MG</div>
                <span class="cgv-user-name">${sessionScope.account.profile.fullName}</span>
            </div>
        </div>
    </header>

    <div class="cgv-page">
        <div class="edit-form">
            <h2>${editAccount.profile.fullName}</h2>

            <c:if test="${not empty flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${flashSuccess}</div>
                <% session.removeAttribute("flashSuccess"); %>
            </c:if>
            <c:if test="${not empty flashError}">
                <div class="cgv-alert cgv-alert-danger">${flashError}</div>
                <% session.removeAttribute("flashError"); %>
            </c:if>

            <div class="edit-field">
                <label>Email</label>
                <div class="value">${editAccount.email}</div>
            </div>
            <div class="edit-field">
                <label>Full Name</label>
                <div class="value">${editAccount.profile.fullName}</div>
            </div>
            <div class="edit-field">
                <label>Current Role</label>
                <div class="value">
                    <span class="cgv-badge ${editAccount.roleName eq 'Admin' ? 'danger' : editAccount.roleName eq 'Manager' ? 'upcoming' : 'inactive'}">
                        ${editAccount.roleName}
                    </span>
                </div>
            </div>
            <div class="edit-field">
                <label>Status</label>
                <div class="value">
                    <span class="${editAccount.isBlocked ? 'cgv-badge-blocked' : 'cgv-badge-active'}">
                        ${editAccount.isBlocked ? 'Blocked' : 'Active'}
                    </span>
                </div>
            </div>

            <c:if test="${sessionScope.account.roleId eq 5}">
                <hr style="border:none;border-top:1px solid #eee;margin:24px 0;">
                <h3 style="font-size:16px;margin-bottom:16px;">Admin Actions</h3>

                <form action="${pageContext.request.contextPath}/manager/users" method="post" style="margin-bottom:16px;">
                    <input type="hidden" name="id" value="${editAccount.accountId}">
                    <div class="edit-field">
                        <label for="newRole">Assign Role</label>
                        <select id="newRole" name="newRole" class="cgv-select" style="width:100%;height:38px;">
                            <option value="2" ${editAccount.roleId eq 2 ? 'selected' : ''}>Customer</option>
                            <option value="3" ${editAccount.roleId eq 3 ? 'selected' : ''}>Employee</option>
                            <option value="4" ${editAccount.roleId eq 4 ? 'selected' : ''}>Manager</option>
                        </select>
                    </div>
                    <button type="submit" name="action" value="updateRole" class="btn--cgv">Update Role</button>
                </form>

                <form action="${pageContext.request.contextPath}/manager/users" method="post">
                    <input type="hidden" name="id" value="${editAccount.accountId}">
                    <input type="hidden" name="action" value="toggleBlock">
                    <button type="submit" class="btn--cgv-outline" onclick="return confirm('${editAccount.isBlocked ? 'Unblock' : 'Block'} this user?')">
                        ${editAccount.isBlocked ? 'Unblock' : 'Block'} Account
                    </button>
                </form>
            </c:if>

            <div class="edit-actions">
                <a href="${pageContext.request.contextPath}/manager/users" class="btn--cgv-outline">&larr; Back to Users</a>
            </div>
        </div>
    </div>
</div>
</body>
</html>
