<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<% request.setAttribute("activeNav", "logs"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>System Logs — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .filter-bar {
            display: flex;
            gap: 10px;
            align-items: center;
            flex-wrap: wrap;
        }
        .badge-action {
            display: inline-block;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 1px;
            text-transform: uppercase;
            font-family: monospace;
            background: #f1f5f9;
            color: #475569;
        }
        .badge-LOGIN_SUCCESS  { background: #d1fae5; color: #065f46; }
        .badge-LOGIN_FAILED   { background: #fee2e2; color: #991b1b; }
        .badge-LOGIN_BLOCKED  { background: #fef3c7; color: #92400e; }
        .badge-BOOK_TICKET    { background: #dbeafe; color: #1e40af; }
        .badge-CHECKIN        { background: #ede9fe; color: #5b21b6; }
        .badge-CONFIG_UPDATE  { background: #fce7f3; color: #9d174d; }
        .log-desc {
            max-width: 360px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            font-size: 13px;
            color: rgba(94,63,58,0.7);
        }
        .pagination { display: flex; gap: 6px; margin-top: 20px; justify-content: center; flex-wrap: wrap; }
        .pagination a, .pagination span {
            display: inline-flex; align-items: center; justify-content: center;
            width: 36px; height: 36px; border-radius: 6px;
            font-size: 13px; font-weight: 600; text-decoration: none;
            border: 1px solid var(--cgv-border);
            color: var(--cgv-dark);
        }
        .pagination span.current { background: var(--cgv-red); color: #fff; border-color: var(--cgv-red); }
        .pagination a:hover { border-color: var(--cgv-red); color: var(--cgv-red); }
    </style>
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">System Logs (UC51)</h1>
        <div class="cgv-header-right">
            <div class="cgv-user-wrap">
                <div class="cgv-avatar">SA</div>
                <span class="cgv-user-name">
                    <c:choose>
                        <c:when test="${not empty sessionScope.account}">${sessionScope.account.fullName}</c:when>
                        <c:otherwise>Admin</c:otherwise>
                    </c:choose>
                </span>
            </div>
        </div>
    </header>

    <div class="cgv-page" style="flex-direction:column;">
        <div class="cgv-list-wrap">

            <%-- Filter bar --%>
            <div class="cgv-data-wrap" style="padding:16px 20px;">
                <form method="get" action="${pageContext.request.contextPath}/admin/logs" class="filter-bar">
                    <select class="cgv-select" name="action" style="height:36px; min-width:180px;">
                        <option value="">-- Tất cả hành động --</option>
                        <c:forEach var="at" items="${actionTypes}">
                            <option value="${at}" ${actionFilter eq at ? 'selected' : ''}>${at}</option>
                        </c:forEach>
                    </select>
                    <input class="cgv-input" type="text" name="q" value="${searchQuery}"
                           placeholder="Tìm email hoặc mô tả..." style="height:36px; min-width:240px;">
                    <button type="submit" class="btn--cgv" style="height:36px; padding:0 16px;">Lọc</button>
                    <a href="${pageContext.request.contextPath}/admin/logs" class="btn--cgv-outline" style="height:36px; display:inline-flex; align-items:center; padding:0 16px;">Xóa lọc</a>
                    <span style="margin-left:auto; font-size:12px; color:rgba(94,63,58,0.5);">
                        Tổng: <strong>${totalLogs}</strong> bản ghi
                    </span>
                </form>
            </div>

            <%-- Logs table --%>
            <div class="cgv-data-wrap">
                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th style="width:60px;">#</th>
                            <th>Hành động</th>
                            <th>Người dùng</th>
                            <th>Mô tả</th>
                            <th>IP</th>
                            <th>Thời gian</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty logs}">
                                <c:forEach var="log" items="${logs}">
                                    <tr>
                                        <td style="color:rgba(94,63,58,0.4); font-size:12px;">${log.logId}</td>
                                        <td>
                                            <span class="badge-action badge-${log.actionType}">
                                                ${log.actionType}
                                            </span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty log.fullName}">
                                                    <strong>${log.fullName}</strong><br>
                                                    <span style="font-size:12px; color:rgba(94,63,58,0.5);">${log.email}</span>
                                                </c:when>
                                                <c:when test="${not empty log.email}">
                                                    <span>${log.email}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color:rgba(94,63,58,0.35);">—</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div class="log-desc" title="${log.description}">${log.description}</div>
                                        </td>
                                        <td style="font-family:monospace; font-size:12px; color:rgba(94,63,58,0.5);">
                                            ${log.ipAddress}
                                        </td>
                                        <td style="font-size:12px; color:rgba(94,63,58,0.6); white-space:nowrap;">
                                            <fmt:formatDate value="${log.createdAt}" pattern="dd/MM/yyyy HH:mm:ss"/>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" style="text-align:center; padding:64px; color:rgba(94,63,58,0.4);">
                                        Chưa có bản ghi log nào.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <%-- Pagination --%>
            <c:if test="${totalPages gt 1}">
                <div class="pagination">
                    <c:if test="${currentPage gt 1}">
                        <a href="?page=${currentPage - 1}&action=${actionFilter}&q=${searchQuery}">‹</a>
                    </c:if>
                    <c:forEach begin="1" end="${totalPages}" var="p">
                        <c:choose>
                            <c:when test="${p eq currentPage}">
                                <span class="current">${p}</span>
                            </c:when>
                            <c:when test="${p le 3 || p ge totalPages - 2 || (p ge currentPage - 1 && p le currentPage + 1)}">
                                <a href="?page=${p}&action=${actionFilter}&q=${searchQuery}">${p}</a>
                            </c:when>
                            <c:when test="${p eq 4 || p eq totalPages - 3}">
                                <span style="border:none; width:auto; padding:0 4px;">…</span>
                            </c:when>
                        </c:choose>
                    </c:forEach>
                    <c:if test="${currentPage lt totalPages}">
                        <a href="?page=${currentPage + 1}&action=${actionFilter}&q=${searchQuery}">›</a>
                    </c:if>
                </div>
            </c:if>

        </div>
    </div>
</div>
</body>
</html>
