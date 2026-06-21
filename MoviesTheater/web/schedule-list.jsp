<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    request.setAttribute("activeNav", "schedules");
    String flashSuccess = (String) session.getAttribute("flashSuccess");
    String flashError = (String) session.getAttribute("flashError");
    if (flashSuccess != null) { request.setAttribute("flashSuccess", flashSuccess); session.removeAttribute("flashSuccess"); }
    if (flashError != null) { request.setAttribute("flashError", flashError); session.removeAttribute("flashError"); }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Schedule Management — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="WEB-INF/manager/_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Schedules</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <div class="cgv-header-divider"></div>
                <div class="cgv-user-wrap">
                    <div class="cgv-avatar">MG</div>
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
                <div style="background:#d4edda;color:#155724;padding:12px 16px;border-radius:8px;margin-bottom:16px;">${flashSuccess}</div>
            </c:if>
            <c:if test="${not empty flashError}">
                <div style="background:#f8d7da;color:#721c24;padding:12px 16px;border-radius:8px;margin-bottom:16px;">${flashError}</div>
            </c:if>

            <div class="cgv-toolbar">
                <div class="cgv-pills">
                    <span class="cgv-pill active">All Schedules</span>
                </div>
                <a href="ScheduleController?action=showAddForm<c:if test='${not empty selectedMovieId}'>&movieId=${selectedMovieId}</c:if>" class="btn--cgv" style="margin-left:auto;">
                    + Add Schedule
                </a>
            </div>

            <div class="cgv-data-wrap">
                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Movie ID</th>
                            <th>Room ID</th>
                            <th>Price</th>
                            <th>Date</th>
                            <th>Start</th>
                            <th>End</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty scheduleList}">
                                <c:forEach var="s" items="${scheduleList}" varStatus="st">
                                    <tr>
                                        <td style="color:rgba(94,63,58,0.5);font-size:12px;">${st.index + 1}</td>
                                        <td style="font-weight:600;">${s.movieID}</td>
                                        <td>${s.roomID}</td>
                                        <td>${s.baseTicketPrice}</td>
                                        <td>${s.showDate}</td>
                                        <td>${s.startTime}</td>
                                        <td>${s.endTime}</td>
                                        <td>
                                            <span class="cgv-badge ${s.status eq 'Scheduled' ? 'active' : s.status eq 'Cancelled' ? 'danger' : 'inactive'}">
                                                ${s.status}
                                            </span>
                                        </td>
                                        <td>
                                            <div style="display:flex;gap:8px;">
                                                <a href="ScheduleController?action=edit&id=${s.scheduleID}&page=${currentPage}"
                                                   class="btn--cgv-outline">Edit</a>
                                                <a href="ScheduleController?action=delete&id=${s.scheduleID}&page=${currentPage}"
                                                   class="btn--cgv-outline"
                                                   style="color:var(--cgv-red);border-color:var(--cgv-red);"
                                                   onclick="return confirm('Delete schedule ${s.scheduleID}?')">
                                                    Delete
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="9" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">
                                        No schedules found.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>

                <div class="cgv-pager">
                    <span>Page ${currentPage} of ${totalPages}</span>
                    <div class="cgv-pager-pages">
                        <c:forEach begin="1" end="${totalPages}" var="p">
                            <button class="cgv-pager-btn ${p eq currentPage ? 'active' : ''}"
                                    onclick="location.href = 'ScheduleController?page=${p}<c:if test='${not empty selectedMovieId}'>&movieId=${selectedMovieId}</c:if>'">${p}</button>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>

        <aside class="cgv-aside">
            <div class="cgv-aside-divider">
                <div class="cgv-aside-heading">SUMMARY</div>
                <div class="cgv-stats-group">
                    <div>
                        <div class="cgv-stat-num">${not empty scheduleList ? scheduleList.size() : '0'}</div>
                        <div class="cgv-stat-key">ON THIS PAGE</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num amber">${not empty totalPages ? totalPages : '1'}</div>
                        <div class="cgv-stat-key">TOTAL PAGES</div>
                    </div>
                </div>
            </div>
        </aside>

    </div>
</div>
</body>
</html>
