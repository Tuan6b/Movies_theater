<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
            <c:if test="${editLocked}">
                <div class="cgv-alert" style="background:#fff3cd;border:1px solid #ffc107;color:#856404;padding:12px 16px;border-radius:8px;margin-bottom:16px;">
                    This schedule has ticket bookings and is view-only. Use <strong>Delete</strong> to cancel it.
                </div>
            </c:if>
            <c:if test="${priceRoomLocked}">
                <div class="cgv-alert" style="background:#fff3cd;border:1px solid #ffc107;color:#856404;padding:12px 16px;border-radius:8px;margin-bottom:16px;">
                    Room and Price are locked because this schedule has ticket bookings.
                </div>
            </c:if>
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
                        <select class="cgv-select" name="roomId" required
                            ${editLocked or priceRoomLocked ? 'disabled' : ''}>
                            <option value="">-- Select Room --</option>
                            <c:forEach var="r" items="${rooms}">
                                <option value="${r.roomId}" ${r.roomId eq schedule.roomID ? 'selected' : ''}>${r.roomNumber}</option>
                            </c:forEach>
                        </select>
                        <c:if test="${editLocked or priceRoomLocked}">
                            <input type="hidden" name="roomId" value="${schedule.roomID}">
                        </c:if>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Base Ticket Price</label>
                        <fmt:formatNumber value="${schedule.baseTicketPrice}" pattern="#" var="formattedBasePrice" />
                        <input class="cgv-input" type="number" name="baseTicketPrice"
                               step="1000" min="0" value="${formattedBasePrice}" required
                               ${editLocked or priceRoomLocked ? 'disabled' : ''}>
                        <c:if test="${editLocked or priceRoomLocked}">
                            <input type="hidden" name="baseTicketPrice" value="${formattedBasePrice}">
                        </c:if>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Show Date</label>
                        <input class="cgv-input" type="date" name="showDate"
                               value="${schedule.showDate}" required
                               ${editLocked ? 'disabled' : ''}>
                        <c:if test="${editLocked}">
                            <input type="hidden" name="showDate" value="${schedule.showDate}">
                        </c:if>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Start Time</label>
                        <input class="cgv-input" type="time" name="startTime"
                               value="${schedule.startTime}" required
                               ${editLocked ? 'disabled' : ''}>
                        <c:if test="${editLocked}">
                            <input type="hidden" name="startTime" value="${schedule.startTime}">
                        </c:if>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Status</label>
                        <select class="cgv-select" name="status" ${editLocked ? 'disabled' : ''}>
                            <option value="Scheduled" ${schedule.status eq 'Scheduled' ? 'selected' : ''}>Scheduled</option>
                            <option value="Cancelled" ${schedule.status eq 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                        </select>
                        <c:if test="${editLocked}">
                            <input type="hidden" name="status" value="${schedule.status}">
                        </c:if>
                    </div>

                    <div style="display:flex;gap:12px;margin-top:24px;">
                        <c:choose>
                            <c:when test="${editLocked}">
                                <a href="${pageContext.request.contextPath}/ScheduleController?movieId=${schedule.movieID}"
                                   class="btn--cgv">Back to Schedules</a>
                            </c:when>
                            <c:otherwise>
                                <button type="submit" class="btn--cgv">Save Changes</button>
                                <a href="${pageContext.request.contextPath}/ScheduleController?movieId=${schedule.movieID}"
                                   class="btn--cgv-outline">Cancel</a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </form>
            </div>
            </c:if>

        </div>
    </div>
</div>
</body>
</html>
