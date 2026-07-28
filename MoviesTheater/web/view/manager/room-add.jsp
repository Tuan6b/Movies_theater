<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "rooms"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Add Room — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager/room-add.css">
    </head>
<body class="cgv-body">

<%@ include file="/view/manager/_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Add New Room</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <a href="${pageContext.request.contextPath}/RoomServlet"
                   class="btn--cgv-outline ra-btn-outline">
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
        <div class="cgv-list-wrap ra-wrap">

            <c:if test="${param.error eq 'room_number_exists'}">
                <div class="cgv-alert cgv-alert-danger">Room number already exists.</div>
            </c:if>
            <c:if test="${param.error eq 'invalid_dimensions'}">
                <div class="cgv-alert cgv-alert-danger">Maximum allowed size is 10 rows and 10 seats per row.</div>
            </c:if>


            <div class="ra-box">
                <div class="ra-title">
                    ROOM DETAILS
                </div>

                <form action="${pageContext.request.contextPath}/RoomServlet" method="post">
                    <input type="hidden" name="action" value="add">
                    <input type="hidden" name="page" value="${param.page}">
                    <input type="hidden" name="filter" value="${param.filter}">

                    <div class="cgv-field">
                        <label class="cgv-label">Room Number</label>
                        <input class="cgv-input" type="text" name="roomNumber"
                               placeholder="e.g. R01" required>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Room Type</label>
                        <select class="cgv-select" name="roomType">
                                    <option value="2D">2D</option>
                                    <option value="3D">3D</option>
                                    <option value="IMAX">IMAX</option>
                                    <option value="4DX">4DX</option>
                                </select>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Number of Rows</label>
                        <input class="cgv-input" type="number" name="numberOfRows"
                               min="1" max="10" placeholder="e.g. 8" required>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Seats per Row</label>
                        <input class="cgv-input" type="number" name="seatsPerRow"
                               min="1" max="10" placeholder="e.g. 10" required>
                    </div>

                    <div class="ra-actions">
                        <button type="submit" class="btn--cgv">Add Room</button>
                        <a href="${pageContext.request.contextPath}/RoomServlet"
                           class="btn--cgv-outline">Cancel</a>
                    </div>
                </form>
            </div>

        </div>
    </div>
</div>
</body>
</html>
