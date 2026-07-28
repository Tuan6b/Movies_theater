<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "schedules"); %>
<% request.setAttribute("todayDate", java.time.LocalDate.now().toString()); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Add Schedule — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager/schedule-add.css">
    </head>
<body class="cgv-body">

<%@ include file="/view/manager/_sidebar.jsp" %>

<div class="cgv-main">
    <header class="cgv-header">
        <h1 class="cgv-header-title">Add Schedule</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <a href="${pageContext.request.contextPath}/ScheduleController?movieId=${movie.movieId}"
                   class="btn--cgv-outline sa-btn-outline">
                    ← Back to Schedules
                </a>
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
        <div class="cgv-list-wrap sa-wrap">

            <div class="sa-box">
                <div class="sa-title">
                    SCHEDULE DETAILS
                </div>

                <c:if test="${empty movie}">
                    <div class="sa-alert">No movie selected. Please go back and choose a movie.</div>
                    <a href="${pageContext.request.contextPath}/ScheduleController" class="btn--cgv">← Back to Schedules</a>
                </c:if>

                <c:if test="${not empty movie}">
                <c:set var="totalMinutes" value="${movie.duration + 15}" />
                <form action="${pageContext.request.contextPath}/ScheduleController" method="post">
                    <input type="hidden" name="action" value="add">
                    <input type="hidden" name="movieId" value="${movie.movieId}">
                    <input type="hidden" name="status" value="Scheduled">

                    <c:if test="${empty rooms}">
                        <div class="sa-alert">
                            No rooms available.
                        </div>
                    </c:if>

                    <div class="cgv-field">
                        <label class="cgv-label">Movie</label>
                        <div class="sa-label">
                            ${movie.movieName}
                            <span class="sa-sublabel">
                                (${movie.duration} min + 15 min cleanup = ${totalMinutes} min per show)
                            </span>
                        </div>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Base Ticket Price</label>
                        <input class="cgv-input" type="number" name="baseTicketPrice"
                               step="1000" min="0" required>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Show Date</label>
                        <input class="cgv-input" type="date" name="showDate" required
                               min="${todayDate}">
                    </div>

                    <!-- Rooms — each with its own time slots -->
                    <div class="cgv-field">
                        <label class="cgv-label">Rooms & Start Times</label>
                        <c:forEach var="r" items="${rooms}" varStatus="loop">
                            <div class="room-card" id="card_${loop.index}">
                                <label class="sa-check-label">
                                    <input type="checkbox" name="roomIds" value="${r.roomId}"
                                           data-room-name="${r.roomNumber}"
                                           onchange="toggleRoom(${loop.index}, this.checked)">
                                    <span class="sa-room-num">${r.roomNumber}</span>
                                </label>
                                <div class="room-times sa-room-times" id="times_${loop.index}">
                                    <div id="slots_${loop.index}"></div>
                                    <button type="button" class="btn--cgv-outline"
                                            onclick="addTime(${loop.index}, ${r.roomId})"
                                            style="font-size:13px;padding:6px 14px;margin-top:4px;">
                                        + Add showtime
                                    </button>
                                </div>
                            </div>
                        </c:forEach>
                    </div>

                    <div class="sa-actions">
                        <button type="submit" class="btn--cgv">Add Schedules</button>
                        <a href="${pageContext.request.contextPath}/ScheduleController?movieId=${movie.movieId}"
                           class="btn--cgv-outline">Cancel</a>
                    </div>
                </form>
                </c:if>
            </div>

        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/manager/schedule-add.js"></script>
<script>
var TOTAL_MINUTES = ${not empty movie ? totalMinutes : 0};
</script>

</body>
</html>
