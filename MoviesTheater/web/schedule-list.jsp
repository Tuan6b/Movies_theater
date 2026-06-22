<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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

            <!-- Dashboard Statistics -->
            <div class="cgv-stats-dashboard fade-in" style="display: flex; gap: 16px; margin-bottom: 24px; padding: 16px; background: #fff; border: 1px solid #e1d8d8; border-radius: 8px;">
                <div class="cgv-stat-card" style="flex: 1; text-align: center; border-right: 1px solid #e1d8d8;">
                    <div class="cgv-stat-title" style="font-size: 12px; color: #5e3f3a; font-weight: 600; text-transform: uppercase;">Tổng số suất</div>
                    <div class="cgv-stat-value" style="font-size: 24px; font-weight: 700; color: #2b2b2b; margin-top: 4px;">${not empty scheduleStats ? scheduleStats['Total'] : 0}</div>
                </div>
                <div class="cgv-stat-card" style="flex: 1; text-align: center; border-right: 1px solid #e1d8d8;">
                    <div class="cgv-stat-title" style="font-size: 12px; color: #5e3f3a; font-weight: 600; text-transform: uppercase;">Đang chiếu</div>
                    <div class="cgv-stat-value" style="font-size: 24px; font-weight: 700; color: #2b2b2b; margin-top: 4px;">${not empty scheduleStats and not empty scheduleStats['Ongoing'] ? scheduleStats['Ongoing'] : 0}</div>
                </div>
                <div class="cgv-stat-card" style="flex: 1; text-align: center; border-right: 1px solid #e1d8d8;">
                    <div class="cgv-stat-title" style="font-size: 12px; color: #5e3f3a; font-weight: 600; text-transform: uppercase;">Chưa chiếu</div>
                    <div class="cgv-stat-value" style="font-size: 24px; font-weight: 700; color: #2b2b2b; margin-top: 4px;">${not empty scheduleStats and not empty scheduleStats['Scheduled'] ? scheduleStats['Scheduled'] : 0}</div>
                </div>
                <div class="cgv-stat-card" style="flex: 1; text-align: center;">
                    <div class="cgv-stat-title" style="font-size: 12px; color: #5e3f3a; font-weight: 600; text-transform: uppercase;">Đã hoàn thành</div>
                    <div class="cgv-stat-value" style="font-size: 24px; font-weight: 700; color: #2b2b2b; margin-top: 4px;">${not empty scheduleStats and not empty scheduleStats['Finished'] ? scheduleStats['Finished'] : 0}</div>
                </div>
            </div>

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
                            <th>Movie</th>
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
                                        <td style="font-weight:600;">
                                            <c:choose>
                                                <c:when test="${not empty movieNames[s.movieID]}">${movieNames[s.movieID]}</c:when>
                                                <c:otherwise>Movie ${s.movieID}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${s.roomID}</td>
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
                                                <c:if test="${s.status ne 'Ongoing' and s.status ne 'Finished'}">
                                                    <a href="ScheduleController?action=edit&id=${s.scheduleID}&page=${currentPage}"
                                                       class="btn--cgv-outline">Edit</a>
                                                    <a href="ScheduleController?action=delete&id=${s.scheduleID}&page=${currentPage}"
                                                       class="btn--cgv-outline"
                                                       style="color:var(--cgv-red);border-color:var(--cgv-red);"
                                                       onclick="return confirm('Delete schedule ${s.scheduleID}?')">
                                                        Delete
                                                    </a>
                                                </c:if>
                                                <c:if test="${s.status eq 'Ongoing'}">
                                                    <span style="color:rgba(94,63,58,0.35);font-size:12px;padding:4px 0;">Ongoing</span>
                                                </c:if>
                                                <c:if test="${s.status eq 'Finished'}">
                                                    <span style="color:rgba(94,63,58,0.35);font-size:12px;padding:4px 0;">Finished</span>
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
