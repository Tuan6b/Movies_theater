<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<% request.setAttribute("activeNav", "users"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Audit Log — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .audit-type { font-size:11px; font-weight:700; letter-spacing:0.5px; padding:2px 10px; border-radius:999px; display:inline-block; }
        .audit-type.block { background:#fef2f2; color:#b91c1c; }
        .audit-type.unblock { background:#f0fdf4; color:#15803d; }
        .audit-type.role { background:#eff6ff; color:#1d4ed8; }
        .audit-type.delete { background:#fdf2f8; color:#be185d; }
    </style>
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">
    <header class="cgv-header">
        <h1 class="cgv-header-title">Audit Log</h1>
        <div class="cgv-header-right">
            <div class="cgv-user-wrap">
                <div class="cgv-avatar">MG</div>
                <span class="cgv-user-name">${sessionScope.account.profile.fullName}</span>
            </div>
        </div>
    </header>

    <div class="cgv-page">
        <div class="cgv-table-wrap">
            <div class="cgv-data-toolbar">
                <form method="get" style="display:flex;gap:10px;align-items:center;flex:1;flex-wrap:wrap;">
                    <input class="cgv-input" style="max-width:300px;height:38px;" type="text" name="q" placeholder="Search action, email, name…" value="${param.q}">
                    <button type="submit" class="btn--cgv-outline">Search</button>
                    <a href="?page=1" class="btn--cgv-outline">Clear</a>
                </form>
            </div>

            <table class="cgv-dt">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>User</th>
                        <th>Action</th>
                        <th>Description</th>
                        <th>IP</th>
                        <th>Date</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty logs}">
                            <c:forEach var="log" items="${logs}" varStatus="st">
                                <tr>
                                    <td style="color:rgba(94,63,58,0.5);font-size:12px;">${st.index + 1 + (currentPage-1)*20}</td>
                                    <td>
                                        <div style="font-weight:500;">${log.fullName}</div>
                                        <div style="font-size:12px;color:rgba(94,63,58,0.5);">${log.accountEmail}</div>
                                    </td>
                                    <td>
                                        <span class="audit-type ${log.actionType.contains('BLOCK') ? 'block' : log.actionType.contains('UNBLOCK') ? 'unblock' : log.actionType.contains('ROLE') ? 'role' : log.actionType.contains('DELETE') ? 'delete' : ''}">${log.actionType}</span>
                                    </td>
                                    <td style="max-width:300px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${log.description}</td>
                                    <td style="font-size:12px;color:rgba(94,63,58,0.5);">${log.ipAddress}</td>
                                    <td style="font-size:13px;color:rgba(94,63,58,0.6);">${log.createdAt}</td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr><td colspan="6" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">No audit logs found.</td></tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>

            <div class="cgv-pager">
                <span>Showing ${not empty logs ? logs.size() : 0} of ${not empty totalLogs ? totalLogs : 0} logs</span>
                <div class="cgv-pager-pages">
                    <c:forEach begin="1" end="${not empty totalPages ? totalPages : 1}" var="p">
                        <button class="cgv-pager-btn ${p eq currentPage ? 'active' : ''}"
                                onclick="location.href='?page=${p}&q=${param.q}'">${p}</button>
                    </c:forEach>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
