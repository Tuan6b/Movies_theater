<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cinema.model.Room" %>
<%
    Room room = (Room) request.getAttribute("room");
    request.setAttribute("activeNav", "rooms");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Seat Layout — ${room.roomNumber} — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/seat-layout.css">
</head>
<body class="cgv-body">

<%@ include file="WEB-INF/manager/_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">
            Seat Layout — ${room.roomNumber}
            <span style="font-size:13px;font-weight:400;color:var(--md-on-surface-variant);margin-left:12px;">
                ${room.roomType} · ${room.capacity} seats
            </span>
        </h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <a href="${pageContext.request.contextPath}/RoomServlet"
                   class="btn--cgv-outline">← Back to Rooms</a>
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

    <div class="cgv-page layout-wrap">

        <div class="screen-bar">SCREEN</div>

        <c:if test="${param.error eq 'has_schedules'}">
            <div class="cgv-alert cgv-alert-danger" style="margin-bottom:16px;">Cannot change seat types — room has existing schedules.</div>
        </c:if>

        <c:if test="${hasSchedules}">
            <div style="background:#fff3cd;border:1px solid #ffc107;color:#856404;padding:12px 16px;border-radius:8px;margin-bottom:16px;font-size:13px;">
                This room has existing schedules. Seat types cannot be changed.
            </div>
        </c:if>

        <c:choose>
            <c:when test="${empty seatsByRow}">
                <div style="text-align:center;padding:48px;color:var(--md-on-surface-variant);">
                    No seats found for this room.
                </div>
            </c:when>
            <c:otherwise>
                <div class="seat-grid">
                    <c:forEach var="entry" items="${seatsByRow}">
                        <c:set var="firstSeat" value="${entry.value[0]}" />
                        <div class="seat-row-wrap">
                            <div class="seat-row-label">${entry.key}</div>
                            <div class="seat-blocks">
                                <c:forEach var="seat" items="${entry.value}">
                                    <div class="seat-block
                                        ${seat.seatType eq 'VIP' ? 'seat-vip' : ''}
                                        ${seat.seatType eq 'Couple' ? 'seat-couple' : ''}
                                        ${seat.seatType eq 'Normal' ? 'seat-normal' : ''}
                                        ${!seat.active ? 'seat-inactive' : ''}"
                                        title="${seat.rowChar}${seat.colNumber}">
                                        ${seat.rowChar}${seat.colNumber}
                                    </div>
                                </c:forEach>
                            </div>
                            <form action="SeatController" method="post" class="row-controls">
                                <input type="hidden" name="action" value="updateRow">
                                <input type="hidden" name="roomId" value="${room.roomId}">
                                <input type="hidden" name="rowChar" value="${entry.key}">
                                <select name="seatType" ${hasSchedules ? 'disabled style="background:#f5f5f5;cursor:not-allowed;"' : ''}>
                                    <option value="Normal" ${firstSeat.seatType eq 'Normal' ? 'selected' : ''}>Normal</option>
                                    <option value="VIP" ${firstSeat.seatType eq 'VIP' ? 'selected' : ''}>VIP</option>
                                    <option value="Couple" ${firstSeat.seatType eq 'Couple' ? 'selected' : ''}>Couple</option>
                                </select>
                                <button type="submit" ${hasSchedules ? 'disabled style="opacity:0.5;cursor:not-allowed;"' : ''}>Save</button>
                            </form>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>

    </div>
</div>
</body>
</html>