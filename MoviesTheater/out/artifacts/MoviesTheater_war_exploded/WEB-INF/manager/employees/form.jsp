<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "employees"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${pageTitle} — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">${pageTitle}</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <a href="${pageContext.request.contextPath}/manager/employees"
                   class="btn--cgv-outline" style="margin-right:8px;">
                    ← Back to Employees
                </a>
                <div class="cgv-header-divider"></div>
                <div class="cgv-user-wrap">
                    <div class="cgv-avatar">MG</div>
                    <span class="cgv-user-name">
                        <c:choose>
                            <c:when test="${not empty sessionScope.account}">${sessionScope.account.fullName}</c:when>
                            <c:otherwise>Manager</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>
        </div>
    </header>

    <div class="cgv-page">
        <div class="cgv-list-wrap" style="max-width:600px;">

            <c:if test="${not empty errorMsg}">
                <div class="cgv-alert cgv-alert-danger">${errorMsg}</div>
            </c:if>

            <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:32px;">
                <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:24px;">
                    EMPLOYEE DETAILS
                </div>

                <form method="post" action="${pageContext.request.contextPath}/manager/employees">
                    <input type="hidden" name="action" value="${formAction}">
                    <c:if test="${formAction eq 'update'}">
                        <input type="hidden" name="accountId" value="${employee.accountId}">
                    </c:if>

                    <%-- Full Name --%>
                    <div class="cgv-field">
                        <label class="cgv-label">
                            Full Name <span style="color:var(--cgv-red)">*</span>
                        </label>
                        <input class="cgv-input" type="text" name="fullName"
                               value="${employee.fullName}" placeholder="Enter full name">
                        <c:if test="${not empty errors['fullName']}">
                            <div style="font-size:12px;color:var(--cgv-red);margin-top:4px;">${errors['fullName']}</div>
                        </c:if>
                    </div>

                    <%-- Email --%>
                    <div class="cgv-field">
                        <label class="cgv-label">
                            Email <span style="color:var(--cgv-red)">*</span>
                        </label>
                        <input class="cgv-input" type="email" name="email"
                               value="${employee.email}" placeholder="Enter email address">
                        <c:if test="${not empty errors['email']}">
                            <div style="font-size:12px;color:var(--cgv-red);margin-top:4px;">${errors['email']}</div>
                        </c:if>
                    </div>

                    <%-- Phone Number --%>
                    <div class="cgv-field">
                        <label class="cgv-label">Phone Number</label>
                        <input class="cgv-input" type="text" name="phoneNumber"
                               value="${employee.phoneNumber}" placeholder="Enter phone number">
                    </div>

                    <%-- Password --%>
                    <div class="cgv-field">
                        <label class="cgv-label">
                            Password
                            <c:choose>
                                <c:when test="${formAction eq 'create'}">
                                    <span style="color:var(--cgv-red)">*</span>
                                </c:when>
                                <c:otherwise>
                                    <span style="color:rgba(94,63,58,0.5);font-weight:400;font-size:12px;">(leave blank to keep current)</span>
                                </c:otherwise>
                            </c:choose>
                        </label>
                        <input class="cgv-input" type="password" name="password"
                               placeholder="${formAction eq 'create' ? 'Set initial password' : 'New password (optional)'}">
                        <c:if test="${not empty errors['password']}">
                            <div style="font-size:12px;color:var(--cgv-red);margin-top:4px;">${errors['password']}</div>
                        </c:if>
                    </div>

                    <div style="display:flex;gap:12px;margin-top:24px;padding-top:20px;border-top:1px solid var(--cgv-border);">
                        <button type="submit" class="btn--cgv">Save Employee</button>
                        <a href="${pageContext.request.contextPath}/manager/employees"
                           class="btn--cgv-outline">Cancel</a>
                    </div>

                </form>
            </div>
        </div>
    </div>
</div>
</body>
</html>
