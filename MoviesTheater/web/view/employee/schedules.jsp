<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "schedules"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Show Schedule — CGV Staff</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Schedule &amp; Counter Ticket Management</h1>
        <div class="cgv-header-right">
            <%@ include file="_notifications.jsp" %>
            <div class="cgv-user-wrap">
                <div class="cgv-avatar">EM</div>
                <span class="cgv-user-name">${sessionScope.account.fullName}</span>
            </div>
        </div>
    </header>

    <div class="cgv-page">
        <div class="cgv-table-wrap" style="flex: 1;">
            
            <c:if test="${not empty requestScope.flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${requestScope.flashSuccess}</div>
            </c:if>
            <c:if test="${not empty requestScope.flashError}">
                <div class="cgv-alert cgv-alert-danger">${requestScope.flashError}</div>
            </c:if>

            <div class="cgv-data-wrap">
                <div class="cgv-data-toolbar" style="padding: 20px; background: #fafafb; border-bottom: 1px solid var(--cgv-border); border-radius: 12px 12px 0 0;">
                    <form method="get" action="${pageContext.request.contextPath}/employee/schedules" style="display:flex;gap:12px;align-items:center;flex-wrap:wrap;">
                        <div style="display:flex; flex-direction:column; gap:4px;">
                            <label style="font-size:11px; font-weight:700; color:rgba(94,63,58,0.5);">SHOW DATE</label>
                            <input class="cgv-input" style="width:180px;height:38px;" type="date" name="date" value="${selectedDate}">
                        </div>

                        <div style="display:flex; flex-direction:column; gap:4px;">
                            <label style="font-size:11px; font-weight:700; color:rgba(94,63,58,0.5);">MOVIE</label>
                            <select class="cgv-select" style="width:240px;height:38px;" name="movieId">
                                <option value="">All movies</option>
                                <c:forEach var="m" items="${movieList}">
                                    <option value="${m.movieId}" ${selectedMovieId eq m.movieId ? 'selected' : ''}>${m.movieName}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div style="display:flex; flex-direction:column; gap:4px;">
                            <label style="font-size:11px; font-weight:700; color:rgba(94,63,58,0.5);">ROOM</label>
                            <select class="cgv-select" style="width:160px;height:38px;" name="roomId">
                                <option value="">All rooms</option>
                                <c:forEach var="r" items="${roomList}">
                                    <option value="${r.roomId}" ${selectedRoomId eq r.roomId ? 'selected' : ''}>${r.roomNumber} (${r.roomType})</option>
                                </c:forEach>
                            </select>
                        </div>

                        <button type="submit" class="btn--cgv" style="height:38px; align-self:flex-end;">Filter</button>
                    </form>
                </div>

                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Movie</th>
                            <th>Room</th>
                            <th>Start Time</th>
                            <th>End Time</th>
                            <th>Tickets Sold</th>
                            <th>Base Price</th>
                            <th>Status</th>
                            <th style="text-align:right;">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty scheduleList}">
                                <c:forEach var="s" items="${scheduleList}" varStatus="st">
                                    <tr>
                                        <td style="color:rgba(94,63,58,0.5);font-size:12px;">${st.index + 1}</td>
                                        <td style="font-weight:700; color:var(--cgv-dark);">${s.movie.movieName}</td>
                                        <td><span class="cgv-pill" style="font-weight:600;">${s.room.roomNumber} (${s.room.roomType})</span></td>
                                        <td style="font-weight:700; color:var(--cgv-primary);">${s.startTime}</td>
                                        <td style="color:rgba(94,63,58,0.6);">${s.endTime}</td>
                                        <td>
                                            <strong style="color:var(--cgv-dark);">${seatsSoldList[st.index]}</strong> / ${s.room.capacity} seats
                                        </td>
                                        <td style="font-weight:600; color:var(--cgv-dark);">${s.baseTicketPrice} VND</td>
                                        <td>
                                            <span class="cgv-badge ${s.status eq 'Scheduled' ? 'active' : 'inactive'}">
                                                ${s.status}
                                            </span>
                                        </td>
                                        <td style="text-align:right;">
                                            <div style="display:inline-flex; gap:8px; justify-content:flex-end;">
                                                <a href="${pageContext.request.contextPath}/employee/tickets?scheduleId=${s.scheduleId}" class="btn--cgv-outline" style="font-size:12px; padding:6px 12px;">
                                                    View Tickets
                                                </a>
                                                <a href="${pageContext.request.contextPath}/employee/book?scheduleId=${s.scheduleId}" class="btn--cgv" style="font-size:12px; padding:6px 12px; background:var(--cgv-primary);">
                                                    Counter Sale
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="9" style="text-align:center;padding:64px;color:rgba(94,63,58,0.4);">
                                        No showtimes found for the selected date.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
</body>
</html>
