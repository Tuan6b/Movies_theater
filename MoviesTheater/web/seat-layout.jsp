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
    <style>
        .screen-bar {
            width: 60%;
            height: 36px;
            background: var(--md-on-surface);
            color: var(--md-surface);
            display: flex;
            justify-content: center;
            align-items: center;
            border-radius: var(--r-lg);
            font-size: 13px;
            font-weight: 600;
            letter-spacing: 4px;
            text-transform: uppercase;
            margin: 0 auto 40px;
        }
        .seat-grid {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 10px;
        }
        .seat-row {
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .seat-row-label {
            width: 24px;
            font-size: 12px;
            font-weight: 600;
            color: var(--md-on-surface-variant);
            text-align: center;
        }
        .seat-cell {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 4px;
        }
        .seat-block {
            width: 52px;
            height: 52px;
            display: flex;
            justify-content: center;
            align-items: center;
            border-radius: var(--r-md);
            color: #fff;
            font-size: 11px;
            font-weight: 700;
            cursor: default;
            transition: box-shadow var(--dur-base);
        }
        .seat-normal { background: #3498db; }
        .seat-vip { background: #f39c12; }
        .seat-couple { background: #9b59b6; }
        .seat-inactive { background: #b0b0b0; }
        .seat-controls {
            display: flex;
            gap: 4px;
            align-items: center;
        }
        .seat-controls select {
            font-size: 11px;
            padding: 2px 4px;
            border: 1px solid var(--md-outline-variant);
            border-radius: var(--r-sm);
            background: var(--md-surface);
            color: var(--md-on-surface);
            cursor: pointer;
        }
        .seat-controls button {
            font-size: 10px;
            padding: 2px 8px;
            border: none;
            border-radius: var(--r-sm);
            background: var(--md-primary);
            color: var(--md-on-primary);
            cursor: pointer;
            font-weight: 600;
        }
        .seat-controls button:hover {
            opacity: 0.85;
        }
        .layout-wrap {
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 32px;
        }
    </style>
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
                   class="btn--cgv-outline" style="margin-right:8px;">
                    ← Back to Rooms
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

    <div class="cgv-page layout-wrap">

        <div class="screen-bar">SCREEN</div>

        <c:choose>
            <c:when test="${empty seatList}">
                <div style="text-align:center;padding:48px;color:var(--md-on-surface-variant);">
                    No seats found for this room.
                </div>
            </c:when>
            <c:otherwise>
                <div class="seat-grid">
                    <c:set var="currentRow" value="" />
                    <c:forEach var="seat" items="${seatList}">
                        <c:if test="${currentRow ne seat.rowChar}">
                            <c:if test="${currentRow ne ''}"></div></c:if>
                            <div class="seat-row">
                            <div class="seat-row-label">${seat.rowChar}</div>
                            <c:set var="currentRow" value="${seat.rowChar}" />
                        </c:if>
                        <div class="seat-cell">
                            <form action="SeatController" method="post" style="display:flex;flex-direction:column;align-items:center;gap:4px;">
                                <input type="hidden" name="action" value="update">
                                <input type="hidden" name="seatId" value="${seat.seatId}">
                                <input type="hidden" name="roomId" value="${seat.roomId}">
                                <div class="seat-block
                                    ${seat.seatType eq 'VIP' ? 'seat-vip' : ''}
                                    ${seat.seatType eq 'Couple' ? 'seat-couple' : ''}
                                    ${seat.seatType eq 'Normal' ? 'seat-normal' : ''}
                                    ${!seat.active ? 'seat-inactive' : ''}">
                                    ${seat.rowChar}${seat.colNumber}
                                </div>
                                <div class="seat-controls">
                                    <select name="seatType">
                                        <option value="Normal" ${seat.seatType eq 'Normal' ? 'selected' : ''}>Normal</option>
                                        <option value="VIP" ${seat.seatType eq 'VIP' ? 'selected' : ''}>VIP</option>
                                        <option value="Couple" ${seat.seatType eq 'Couple' ? 'selected' : ''}>Couple</option>
                                    </select>
                                    <button type="submit">Save</button>
                                </div>
                            </form>
                        </div>
                    </c:forEach>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

    </div>
</div>
</body>
</html>