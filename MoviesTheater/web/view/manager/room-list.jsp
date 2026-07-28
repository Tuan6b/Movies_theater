<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "rooms"); %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Room Management — CGV Admin</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
        
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager/room-list.css">
    </head>
    <body class="cgv-body">

        <%@ include file="/view/manager/_sidebar.jsp" %>

        <div class="cgv-main">

            <header class="cgv-header">
                <h1 class="cgv-header-title">Phòng chiếu</h1>
                <div class="cgv-header-right">
                    <div class="cgv-header-actions">
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

                <div class="cgv-table-wrap">



                    <c:if test="${param.error eq 'room_number_exists'}">
                        <div class="cgv-alert cgv-alert-danger">Room number already exists. Please use a different room number.</div>
                    </c:if>

                    <c:if test="${param.error eq 'room_has_schedules'}">
                        <div class="cgv-alert cgv-alert-danger">Cannot deactivate room — it has existing schedules.</div>
                    </c:if>

                    <div class="cgv-toolbar">
                        <div class="cgv-pills">
                            <a href="RoomServlet?filter=active"
                               class="cgv-pill ${currentFilter eq 'active' ? 'active' : ''}">Active</a>
                            <a href="RoomServlet?filter=inactive"
                               class="cgv-pill ${currentFilter eq 'inactive' ? 'active' : ''}">Inactive</a>
                        </div>
                        <a href="${pageContext.request.contextPath}/view/manager/room-add.jsp?page=${currentPage}&filter=${currentFilter}" class="btn--cgv rl-btn-right">
                            + Add Room
                        </a>
                    </div>

                    <div class="cgv-data-wrap">
                        <table class="cgv-dt">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Room Number</th>
                                    <th>Type</th>
                                    <th>Capacity</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty roomList}">
                                        <c:forEach var="room" items="${roomList}" varStatus="st">
                                            <tr>
                                                <td class="rl-td-index">${st.index + 1}</td>
                                                <td class="rl-td-bold">${room.roomNumber}</td>
                                                <td>
                                                    <span class="cgv-badge type-${room.roomType}">${room.roomType}</span>
                                                </td>
                                                <td>${room.capacity}</td>
                                                <td>
                                                    <div class="rl-actions-wrap">
                                                        <a href="RoomServlet?action=edit&id=${room.roomId}&page=${currentPage}&filter=${currentFilter}"
                                                           class="btn--cgv-outline">Edit</a>
                                                        <a href="${pageContext.request.contextPath}/SeatController?roomId=${room.roomId}"
                                                           class="btn--cgv-outline">
                                                            Seats
                                                        </a>
                                                        <c:if test="${room.active}">
                                                        <a href="RoomServlet?action=delete&id=${room.roomId}&page=${currentPage}&filter=${currentFilter}"
                                                           class="btn--cgv-outline rl-btn-danger"
                                                           onclick="return confirm('Deactivate room ${room.roomNumber}?')">
                                                            Deactivate
                                                        </a>
                                                        </c:if>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="5" class="rl-empty-row">
                                                No rooms found.
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>

                        <div class="cgv-pager">
                            <span>Page ${currentPage} of ${totalPages}</span>
                            <div class="cgv-pager-pages">
                                <c:forEach begin="1" end="${totalPages}" var="p">
                                    <button class="cgv-pager-btn ${p eq currentPage ? 'active' : ''}"
                                            onclick="location.href = 'RoomServlet?page=${p}&filter=${currentFilter}'">${p}</button>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </div>


            </div>
        </div>
    </body>
</html>
