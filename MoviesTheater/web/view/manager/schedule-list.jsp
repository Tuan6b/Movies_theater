<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    request.setAttribute("activeNav", "schedules");
    String flashSuccess = (String) session.getAttribute("flashSuccess");
    String flashError = (String) session.getAttribute("flashError");
    java.util.List<String> flashErrorList = (java.util.List<String>) session.getAttribute("flashErrorList");
    if (flashSuccess != null) { request.setAttribute("flashSuccess", flashSuccess); session.removeAttribute("flashSuccess"); }
    if (flashError != null) { request.setAttribute("flashError", flashError); session.removeAttribute("flashError"); }
    if (flashErrorList != null) { request.setAttribute("flashErrorList", flashErrorList); session.removeAttribute("flashErrorList"); }
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

<%@ include file="/view/manager/_sidebar.jsp" %>

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
                <div style="background:#f8d7da;color:#721c24;padding:12px 16px;border-radius:8px;margin-bottom:8px;">${flashError}</div>
            </c:if>
            <c:if test="${not empty flashErrorList}">
                <div style="background:#fff3cd;color:#856404;padding:10px 16px;border-radius:8px;margin-bottom:16px;font-size:13px;">
                    <c:forEach var="err" items="${flashErrorList}">
                        <div style="margin:2px 0;">${err}</div>
                    </c:forEach>
                </div>
            </c:if>

            <div class="cgv-toolbar">
                <div class="cgv-pills">
                    <c:forEach var="m" items="${movieList}">
                        <a href="ScheduleController?movieId=${m.movieId}"
                           class="cgv-pill ${m.movieId eq selectedMovieId ? 'active' : ''}">
                            ${m.movieName}
                        </a>
                    </c:forEach>
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
                            <th>Movie</th>
                            <th>Room</th>
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
                                        <td style="font-weight:600;">
                                            <c:choose>
                                                <c:when test="${not empty movieNameMap[s.movieID]}">${movieNameMap[s.movieID]}</c:when>
                                                <c:otherwise>Movie ${s.movieID}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${not empty roomNameMap[s.roomID] ? roomNameMap[s.roomID] : s.roomID}</td>
                                        <td><fmt:formatNumber value="${s.baseTicketPrice}" type="number" maxFractionDigits="0"/> VNĐ</td>
                                        <td>
                                            <fmt:parseDate value="${s.showDate}" pattern="yyyy-MM-dd" var="parsedDate" type="date"/>
                                            <fmt:formatDate value="${parsedDate}" pattern="dd/MM/yyyy"/>
                                        </td>
                                        <td>${fn:substring(s.startTime, 0, 5)}</td>
                                        <td>${fn:substring(s.endTime, 0, 5)}</td>
                                        <td>
                                            <span class="cgv-badge ${s.status eq 'Scheduled' ? 'active' : s.status eq 'Cancelled' ? 'danger' : 'inactive'}">
                                                ${s.status}
                                            </span>
                                        </td>
                                        <td>
                                            <div style="display:flex;gap:8px;">
                                                <c:if test="${s.status eq 'Scheduled'}">
                                                    <a href="ScheduleController?action=edit&id=${s.scheduleID}&page=${currentPage}"
                                                       class="btn--cgv-outline">Edit</a>
                                                    <a href="ScheduleController?action=delete&id=${s.scheduleID}&page=${currentPage}"
                                                       class="btn--cgv-outline"
                                                       style="color:var(--cgv-red);border-color:var(--cgv-red);"
                                                       onclick="return confirm('Delete this schedule?')">
                                                        Delete
                                                    </a>
                                                </c:if>
                                                <c:if test="${s.status eq 'Cancelled' or s.status eq 'Finished'}">
                                                    <a href="ScheduleController?action=delete&id=${s.scheduleID}&page=${currentPage}"
                                                       class="btn--cgv-outline"
                                                       style="color:var(--cgv-red);border-color:var(--cgv-red);"
                                                       onclick="return confirm('Delete this schedule?')">
                                                        Delete
                                                    </a>
                                                </c:if>
                                                <c:if test="${s.status eq 'Ongoing'}">
                                                    <span style="color:rgba(94,63,58,0.35);font-size:12px;padding:4px 0;">Ongoing</span>
                                                </c:if>
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


    </div>
</div>
</body>
</html>
