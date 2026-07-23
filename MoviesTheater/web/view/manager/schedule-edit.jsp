<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "schedules"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Edit Schedule — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="/view/manager/_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Edit Schedule</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <a href="${pageContext.request.contextPath}/ScheduleController?movieId=${schedule.movieID}"
                   class="btn--cgv-outline" style="margin-right:8px;">
                    ← Back to Schedules
                </a>
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
        <div class="cgv-list-wrap" style="max-width:640px;">

            <c:if test="${empty schedule}">
                <div class="cgv-alert cgv-alert-danger">Schedule not found.</div>
                <a href="${pageContext.request.contextPath}/ScheduleController" class="btn--cgv">Back to List</a>
            </c:if>

            <c:if test="${not empty schedule}">
            <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:32px;">
                <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:24px;">
                    SCHEDULE DETAILS
                </div>

                <form action="${pageContext.request.contextPath}/ScheduleController?page=${currentPage}" method="post">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="scheduleId" value="${schedule.scheduleID}">

                    <input type="hidden" name="movieId" value="${schedule.movieID}">
                    <div class="cgv-field">
                        <label class="cgv-label">Movie</label>
                        <div style="padding:8px 0;font-weight:600;">${not empty editMovieName ? editMovieName : 'Movie #' + schedule.movieID}</div>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Room</label>
                        <select class="cgv-select" name="roomId" required>
                            <option value="">-- Select Room --</option>
                            <c:forEach var="r" items="${rooms}">
                                <option value="${r.roomId}" ${r.roomId eq schedule.roomID ? 'selected' : ''}>${r.roomNumber}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Base Ticket Price</label>
                        <input class="cgv-input" type="number" name="baseTicketPrice"
                               step="0.01" min="0" value="${schedule.baseTicketPrice}" required>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Show Date</label>
                        <input class="cgv-input" type="date" name="showDate"
                               value="${schedule.showDate}" required>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Start Time</label>
                        <input class="cgv-input" type="time" name="startTime"
                               value="${schedule.startTime}" required>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Status</label>
                        <select class="cgv-select" name="status">
                            <option value="Scheduled" ${schedule.status eq 'Scheduled' ? 'selected' : ''}>Scheduled</option>
                            <option value="Cancelled" ${schedule.status eq 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                        </select>
                    </div>

                    <div style="display:flex;gap:12px;margin-top:24px;">
                        <button type="submit" class="btn--cgv">Save Changes</button>
                        <a href="${pageContext.request.contextPath}/ScheduleController?movieId=${schedule.movieID}"
                           class="btn--cgv-outline">Cancel</a>
                    </div>
                </form>
            </div>
            </c:if>

        </div>
    </div>
</div>
</body>
</html>
