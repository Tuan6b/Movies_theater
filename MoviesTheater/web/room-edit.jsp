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
    </head>
    <body class="cgv-body">

        <%@ include file="WEB-INF/manager/_sidebar.jsp" %>

        <div class="cgv-main">

            <header class="cgv-header">
                <h1 class="cgv-header-title">Edit Room</h1>
                <div class="cgv-header-right">
                    <div class="cgv-header-actions">
                        <a href="${pageContext.request.contextPath}/RoomServlet?page=${origPage}"
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

            <div class="cgv-page">
                <div class="cgv-list-wrap" style="max-width:640px;">

                    <c:if test="${param.error eq 'room_number_exists'}">
                        <div class="cgv-alert cgv-alert-danger">Room number already exists.</div>
                    </c:if>
                    <c:if test="${param.error eq 'has_schedules'}">
                        <div class="cgv-alert cgv-alert-danger">Cannot change seat layout — room has existing schedules.</div>
                    </c:if>
                    <c:if test="${param.error eq 'cannot_change_layout'}">
                        <div class="cgv-alert cgv-alert-danger">Cannot change seat layout — room has existing bookings.</div>
                    </c:if>

                    <c:if test="${hasSchedules}">
                        <div class="cgv-alert cgv-alert-warning" style="background:#fff3cd;border:1px solid #ffc107;color:#856404;padding:12px 16px;border-radius:8px;margin-bottom:16px;font-size:13px;">
                            This room has existing schedules. Seat layout (rows/seats) cannot be changed.
                        </div>
                    </c:if>

                    <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:32px;">
                        <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:24px;">
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
                                       value="${room.numberOfRows}" min="1" required
                                       ${hasSchedules ? 'readonly style="background:#f5f5f5;cursor:not-allowed;"' : ''}>
                            </div>

                            <div class="cgv-field">
                                <label class="cgv-label">Seats per Row</label>
                                <input class="cgv-input" type="number" name="seatsPerRow"
                                       value="${room.seatsPerRow}" min="1" required
                                       ${hasSchedules ? 'readonly style="background:#f5f5f5;cursor:not-allowed;"' : ''}>
                            </div>

                            <div class="cgv-field" style="flex-direction:row;align-items:center;gap:12px;">
                                <input type="checkbox" name="active" id="activeCheck"
                                       ${room.active ? 'checked' : ''}
                                       style="width:18px;height:18px;accent-color:var(--cgv-red);cursor:pointer;">
                                <label class="cgv-label" for="activeCheck" style="margin-bottom:0;cursor:pointer;">
                                    Active (uncheck to deactivate)
                                </label>
                            </div>

                            <div style="display:flex;gap:12px;margin-top:24px;">
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