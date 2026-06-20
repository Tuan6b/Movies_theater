<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "schedules"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Add Schedule — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="WEB-INF/manager/_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Add Schedule</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <a href="${pageContext.request.contextPath}/ScheduleController"
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

            <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:32px;">
                <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:24px;">
                    SCHEDULE DETAILS
                </div>

                <form action="ScheduleController" method="post">
                    <input type="hidden" name="action" value="add">

                    <c:if test="${empty movies or empty rooms}">
                        <div style="background:#f8d7da;color:#721c24;padding:12px 16px;border-radius:8px;margin-bottom:16px;">
                            Please access this form from the Schedule list page.
                        </div>
                    </c:if>

                    <div class="cgv-field">
                        <label class="cgv-label">Movie</label>
                        <select class="cgv-select" name="movieId" required>
                            <option value="">-- Select Movie --</option>
                            <c:forEach var="m" items="${movies}">
                                <option value="${m.movieId}">${m.movieName}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Room</label>
                        <select class="cgv-select" name="roomId" required>
                            <option value="">-- Select Room --</option>
                            <c:forEach var="r" items="${rooms}">
                                <option value="${r.roomId}">${r.roomNumber}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Show Date</label>
                        <input class="cgv-input" type="date" name="showDate" required>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Start Time</label>
                        <input class="cgv-input" type="time" name="startTime" required>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">End Time</label>
                        <input class="cgv-input" type="time" name="endTime" required>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Status</label>
                        <select class="cgv-select" name="status">
                            <option value="Scheduled">Scheduled</option>
                            <option value="Ongoing">Ongoing</option>
                            <option value="Finished">Finished</option>
                            <option value="Cancelled">Cancelled</option>
                        </select>
                    </div>

                    <div style="display:flex;gap:12px;margin-top:24px;">
                        <button type="submit" class="btn--cgv">Add Schedule</button>
                        <a href="${pageContext.request.contextPath}/ScheduleController"
                           class="btn--cgv-outline">Cancel</a>
                    </div>
                </form>
            </div>

        </div>
    </div>
</div>
</body>
</html>
