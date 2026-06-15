<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "employees"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Employees — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Employees</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
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
        <div class="cgv-table-wrap">

            <c:if test="${not empty flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${flashSuccess}</div>
            </c:if>
            <c:if test="${not empty flashError}">
                <div class="cgv-alert cgv-alert-danger">${flashError}</div>
            </c:if>

            <div class="cgv-toolbar">
                <div class="cgv-pills">
                    <span class="cgv-pill active">All Employees</span>
                </div>
                <a href="${pageContext.request.contextPath}/manager/employees?action=add" class="btn--cgv">
                    <svg width="10" height="10" viewBox="0 0 12 12" fill="none"
                         stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                        <line x1="6" y1="1" x2="6" y2="11"/><line x1="1" y1="6" x2="11" y2="6"/>
                    </svg>
                    Add Employee
                </a>
            </div>

            <div class="cgv-data-wrap">
                <div class="cgv-data-toolbar">
                    <form method="get" style="display:flex;gap:10px;align-items:center;">
                        <input class="cgv-input" style="max-width:280px;height:38px;"
                               type="text" name="keyword" placeholder="Search by name or email..."
                               value="${keyword}">
                        <button type="submit" class="btn--cgv-outline">Search</button>
                        <c:if test="${not empty keyword}">
                            <a href="${pageContext.request.contextPath}/manager/employees"
                               class="btn--cgv-outline">Clear</a>
                        </c:if>
                    </form>
                </div>

                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Full Name</th>
                            <th>Email</th>
                            <th>Phone</th>
                            <th>Status</th>
                            <th>Created At</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty employees}">
                                <c:forEach var="emp" items="${employees}" varStatus="st">
                                    <tr>
                                        <td style="color:rgba(94,63,58,0.5);font-size:12px;">${st.index + 1}</td>
                                        <td style="font-weight:500;">
                                            <c:choose>
                                                <c:when test="${not empty emp.fullName}">${emp.fullName}</c:when>
                                                <c:otherwise><em style="color:rgba(94,63,58,0.4);">No name</em></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${emp.email}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty emp.phoneNumber}">${emp.phoneNumber}</c:when>
                                                <c:otherwise>—</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${emp.isBlocked}">
                                                    <span class="cgv-badge danger">Inactive</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="cgv-badge active">Active</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-size:13px;">${emp.createdAt}</td>
                                        <td>
                                            <div style="display:flex;gap:8px;">
                                                <a href="${pageContext.request.contextPath}/manager/employees?action=edit&id=${emp.accountId}"
                                                   class="btn--cgv-outline">Edit</a>
                                                <c:choose>
                                                    <c:when test="${emp.isBlocked}">
                                                        <form method="post" style="display:inline;">
                                                            <input type="hidden" name="action" value="toggle">
                                                            <input type="hidden" name="accountId" value="${emp.accountId}">
                                                            <input type="hidden" name="blocked" value="false">
                                                            <button type="submit" class="btn--cgv-outline"
                                                                    style="color:green;border-color:green;"
                                                                    onclick="return confirm('Activate employee ${emp.fullName}?')">Activate</button>
                                                        </form>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <form method="post" style="display:inline;">
                                                            <input type="hidden" name="action" value="toggle">
                                                            <input type="hidden" name="accountId" value="${emp.accountId}">
                                                            <input type="hidden" name="blocked" value="true">
                                                            <button type="submit" class="btn--cgv-outline"
                                                                    style="color:var(--cgv-red);border-color:var(--cgv-red);"
                                                                    onclick="return confirm('Deactivate employee ${emp.fullName}?')">Deactivate</button>
                                                        </form>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">
                                        No employees found.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>

                <div class="cgv-pager">
                    <span>
                        Showing ${not empty employees ? employees.size() : 0}
                        of ${not empty totalItems ? totalItems : 0} employees
                    </span>
                    <div class="cgv-pager-pages">
                        <c:forEach begin="1" end="${not empty totalPages ? totalPages : 1}" var="pg">
                            <button class="cgv-pager-btn ${pg eq currentPage ? 'active' : ''}"
                                    onclick="location.href='?page=${pg}&keyword=${keyword}'">${pg}</button>
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
                        <div class="cgv-stat-num">${not empty totalItems ? totalItems : '0'}</div>
                        <div class="cgv-stat-key">TOTAL</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num">${not empty employees ? employees.size() : '0'}</div>
                        <div class="cgv-stat-key">SHOWING</div>
                    </div>
                </div>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
