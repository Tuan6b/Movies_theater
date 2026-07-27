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
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager/schedule-list.css">
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
                <div class="sl-alert-success">${flashSuccess}</div>
            </c:if>
            <c:if test="${not empty flashError}">
                <div class="sl-alert-error">${flashError}</div>
            </c:if>
            <c:if test="${not empty flashErrorList}">
                <div class="sl-alert-warning">
                    <c:forEach var="err" items="${flashErrorList}">
                        <div class="sl-err-msg">${err}</div>
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
                <a href="ScheduleController?action=showAddForm<c:if test='${not empty selectedMovieId}'>&movieId=${selectedMovieId}</c:if>" class="btn--cgv sl-btn-right">
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
                                        <td class="sl-td-index">${st.index + 1}</td>
                                        <td class="sl-td-bold">
                                            <c:choose>
                                                <c:when test="${not empty movieNameMap[s.movieID]}">${movieNameMap[s.movieID]}</c:when>
                                                <c:otherwise>Movie ${s.movieID}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${not empty roomNameMap[s.roomID] ? roomNameMap[s.roomID] : s.roomID}</td>
                                        <td><fmt:formatNumber value="${s.baseTicketPrice}" type="number" maxFractionDigits="0"/> VND</td>
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
                                            <div class="sl-actions-wrap">
                                                <c:choose>
                                                    <c:when test="${s.status eq 'Scheduled'}">
                                                        <a href="ScheduleController?action=edit&id=${s.scheduleID}&page=${currentPage}"
                                                           class="btn--cgv-outline">Edit</a>
                                                        <a href="ScheduleController?action=delete&id=${s.scheduleID}&page=${currentPage}"
                                                           class="btn--cgv-outline sl-btn-danger"
                                                           onclick="return confirmDelete(${s.scheduleID})">
                                                            Delete
                                                        </a>
                                                    </c:when>
                                                    <c:when test="${s.status eq 'Cancelled'}">
                                                        <a href="ScheduleController?action=edit&id=${s.scheduleID}&page=${currentPage}"
                                                           class="btn--cgv-outline">Edit</a>
                                                        <a href="ScheduleController?action=delete&id=${s.scheduleID}&page=${currentPage}"
                                                           class="btn--cgv-outline sl-btn-danger"
                                                           onclick="return confirmDelete(${s.scheduleID})">
                                                            Delete
                                                        </a>
                                                    </c:when>
                                                    <c:when test="${s.status eq 'Finished'}">
                                                        <span class="sl-sep">-</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="sl-sep">Ongoing</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="9" class="sl-empty-row">
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

<script src="${pageContext.request.contextPath}/js/manager/schedule-list.js"></script>
<script>
    const scheduleIdsWithTickets = new Set([
        <c:forEach items="${schedulesWithTickets}" var="sid" varStatus="st">
            ${sid}${st.last ? '' : ','}
        </c:forEach>
    ]);
</script>
</body>
</html>
