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
        <style>
            .cgv-badge.type-2D   { background: #d3e1fb; color: #0d3d86; }
            .cgv-badge.type-3D   { background: #d7f3dd; color: #0d5023; }
            .cgv-badge.type-IMAX { background: #ffe8c7; color: #8a5100; }
            .cgv-badge.type-4DX  { background: #ffdad4; color: #ba1a1a; }
        </style>
    </head>
    <body class="cgv-body">

        <%@ include file="/view/manager/_sidebar.jsp" %>

        <div class="cgv-main">

            <header class="cgv-header">
                <h1 class="cgv-header-title">Screening Rooms</h1>
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
                        <a href="${pageContext.request.contextPath}/view/manager/room-add.jsp?page=${currentPage}&filter=${currentFilter}" class="btn--cgv" style="margin-left:auto;">
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
                                                <td style="color:rgba(94,63,58,0.5);font-size:12px;">${st.index + 1}</td>
                                                <td style="font-weight:600;">${room.roomNumber}</td>
                                                <td>
                                                    <span class="cgv-badge type-${room.roomType}">${room.roomType}</span>
                                                </td>
                                                <td>${room.capacity}</td>
                                                <td>
                                                    <div style="display:flex;gap:8px;">
                                                        <a href="RoomServlet?action=edit&id=${room.roomId}&page=${currentPage}&filter=${currentFilter}"
                                                           class="btn--cgv-outline">Edit</a>
                                                        <a href="${pageContext.request.contextPath}/SeatController?roomId=${room.roomId}"
                                                           class="btn--cgv-outline">
                                                            Seats
                                                        </a>
                                                        <c:if test="${room.active}">
                                                        <a href="RoomServlet?action=delete&id=${room.roomId}&page=${currentPage}&filter=${currentFilter}"
                                                           class="btn--cgv-outline"
                                                           style="color:var(--cgv-red);border-color:var(--cgv-red);"
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
                                            <td colspan="5" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">
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

                <aside class="cgv-aside">
                    <div class="cgv-aside-divider">
                        <div class="cgv-aside-heading">SUMMARY</div>
                        <div class="cgv-stats-group">
                            <div>
                                <div class="cgv-stat-num">${not empty roomList ? roomList.size() : '0'}</div>
                                <div class="cgv-stat-key">ON THIS PAGE</div>
                            </div>
                            <div>
                                <div class="cgv-stat-num amber">${not empty totalPages ? totalPages : '1'}</div>
                                <div class="cgv-stat-key">TOTAL PAGES</div>
                            </div>
                        </div>
                    </div>
                </aside>

            </div>
        </div>
    </body>
</html>
