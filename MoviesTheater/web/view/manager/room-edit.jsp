<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cinema.model.Room" %>
<%
    Room room = (Room) request.getAttribute("room");
    String origPage = (String) request.getAttribute("currentPage");
    if (origPage == null) origPage = request.getParameter("page");
    if (origPage == null || origPage.isEmpty()) origPage = "1";
    String origFilter = request.getParameter("filter");
    if (origFilter == null || origFilter.isEmpty()) origFilter = "active";
    request.setAttribute("origPage", origPage);
    request.setAttribute("origFilter", origFilter);
    request.setAttribute("activeNav", "rooms");
%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Edit Room — CGV Admin</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager/room-edit.css">
    </head>
    <body class="cgv-body">

        <%@ include file="/view/manager/_sidebar.jsp" %>

        <div class="cgv-main">

            <header class="cgv-header">
                <h1 class="cgv-header-title">Edit Room</h1>
                <div class="cgv-header-right">
                    <div class="cgv-header-actions">
                        <a href="${pageContext.request.contextPath}/RoomServlet?page=${origPage}"
                           class="btn--cgv-outline re-btn-outline">
                            ← Back to Rooms
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
                <div class="cgv-list-wrap re-wrap">

                    <c:if test="${param.error eq 'room_number_exists'}">
                        <div class="cgv-alert cgv-alert-danger">Room number already exists.</div>
                    </c:if>
                    <c:if test="${param.error eq 'invalid_dimensions'}">
                        <div class="cgv-alert cgv-alert-danger">Maximum allowed size is 10 rows and 10 seats per row.</div>
                    </c:if>
                    <c:if test="${param.error eq 'has_schedules'}">
                        <div class="cgv-alert cgv-alert-danger">This room has existing schedules — all fields are locked.</div>
                    </c:if>

                    <c:if test="${hasSchedules}">
                        <div class="cgv-alert cgv-alert-warning re-alert-warning">
                            This room has existing schedules. Seat layout (rows/seats) cannot be changed.
                        </div>
                    </c:if>

                    <div class="re-box">
                        <div class="re-title">
                            ROOM DETAILS
                        </div>

                        <form action="${pageContext.request.contextPath}/RoomServlet?page=${origPage}&filter=${origFilter}" method="post">
                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="roomId" value="${room.roomId}">

                            <div class="cgv-field">
                                <label class="cgv-label">Room Number</label>
                                <input class="cgv-input" type="text" name="roomNumber"
                                       value="${room.roomNumber}" required>
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Room Type</label>
                                <select class="cgv-select" name="roomType">
                                    <option value="2D"   ${room.roomType eq '2D'   ? 'selected' : ''}>2D</option>
                                    <option value="3D"   ${room.roomType eq '3D'   ? 'selected' : ''}>3D</option>
                                    <option value="IMAX" ${room.roomType eq 'IMAX' ? 'selected' : ''}>IMAX</option>
                                    <option value="4DX"  ${room.roomType eq '4DX'  ? 'selected' : ''}>4DX</option>
                                </select>
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Number of Rows</label>
                                <input class="cgv-input" type="number" name="numberOfRows"
                                       value="${room.numberOfRows}" min="1" max="10" required
                                       ${hasSchedules ? 'readonly class="re-input-disabled"' : ''}>
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Seats per Row</label>
                                <input class="cgv-input" type="number" name="seatsPerRow"
                                       value="${room.seatsPerRow}" min="1" max="10" required
                                       ${hasSchedules ? 'readonly class="re-input-disabled"' : ''}>
                            </div>

                            <div class="cgv-field re-field-row">
                                <input type="checkbox" name="active" id="activeCheck"
                                       ${room.active ? 'checked' : ''}
                                       class="re-check-input">
                                <label class="cgv-label re-check-label" for="activeCheck">
                                    Active (uncheck to deactivate)
                                </label>
                            </div>

                            <div class="re-actions">
                                <button type="submit" class="btn--cgv">Save Changes</button>
                                 <a href="${pageContext.request.contextPath}/RoomServlet?page=${origPage}&filter=${origFilter}"
                                                                       class="btn--cgv-outline">Cancel</a>
                            </div>
                        </form>
                    </div>

                </div>
            </div>
        </div>
    </body>
</html>
