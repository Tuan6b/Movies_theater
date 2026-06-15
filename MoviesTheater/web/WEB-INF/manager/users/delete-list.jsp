<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "users"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Deletion Requests — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">
    <header class="cgv-header">
        <h1 class="cgv-header-title">Deletion Requests</h1>
        <div class="cgv-header-right">
            <div class="cgv-user-wrap">
                <div class="cgv-avatar">MG</div>
                <span class="cgv-user-name">${sessionScope.account.profile.fullName}</span>
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
                    <a href="?status=all" class="cgv-pill ${empty param.status || param.status eq 'all' ? 'active' : ''}">All</a>
                    <a href="?status=Pending" class="cgv-pill ${param.status eq 'Pending' ? 'active' : ''}">Pending</a>
                    <a href="?status=Approved" class="cgv-pill ${param.status eq 'Approved' ? 'active' : ''}">Approved</a>
                    <a href="?status=Rejected" class="cgv-pill ${param.status eq 'Rejected' ? 'active' : ''}">Rejected</a>
                </div>
            </div>

            <table class="cgv-dt">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>User</th>
                        <th>Reason</th>
                        <th>Status</th>
                        <th>Submitted</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty requests}">
                            <c:forEach var="r" items="${requests}" varStatus="st">
                                <tr>
                                    <td style="color:rgba(94,63,58,0.5);font-size:12px;">${st.index + 1}</td>
                                    <td>
                                        <div style="font-weight:500;">${r.fullName}</div>
                                        <div style="font-size:12px;color:rgba(94,63,58,0.5);">${r.accountEmail}</div>
                                    </td>
                                    <td style="max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${r.reason}</td>
                                    <td>
                                        <span class="cgv-badge ${r.status eq 'Pending' ? 'upcoming' : r.status eq 'Approved' ? 'active' : 'inactive'}">${r.status}</span>
                                    </td>
                                    <td style="font-size:13px;color:rgba(94,63,58,0.6);">${r.createdAt}</td>
                                    <td>
                                        <a href="?action=review&id=${r.requestId}" class="btn--cgv-outline">Review</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr><td colspan="6" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">No deletion requests found.</td></tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>

            <div class="cgv-pager">
                <span>Showing ${not empty requests ? requests.size() : 0} of ${not empty total ? total : 0} requests</span>
                <div class="cgv-pager-pages">
                    <c:forEach begin="1" end="${not empty totalPages ? totalPages : 1}" var="p">
                        <button class="cgv-pager-btn ${p eq currentPage ? 'active' : ''}"
                                onclick="location.href='?page=${p}&status=${param.status}'">${p}</button>
                    </c:forEach>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
