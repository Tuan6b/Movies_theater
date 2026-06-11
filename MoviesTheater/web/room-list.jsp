<%-- 
    Document   : room-list
    Created on : May 24, 2026, 10:11:30 PM
    Author     : Tuan Phong Nguyen
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.cinema.model.Room"%>
<!DOCTYPE html>
<<<<<<< Updated upstream
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Room Management</title>
    </head>

    <body>

        <% if ("capacity_invalid".equals(request.getParameter("error"))) { %>
        <script>alert('Capacity must be greater than 0!');</script>
        <% } %>

        <h1>Room Management</h1>

        <%
            Integer currentPage = (Integer) request.getAttribute("currentPage");
            Integer totalPages = (Integer) request.getAttribute("totalPages");

            if (currentPage == null) currentPage = 1;
            if (totalPages == null) totalPages = 1;
        %>

        <h2>Add New Room</h2>

        <form action="RoomServlet?page=<%= currentPage %>" method="post">

            <input type="hidden" name="action" value="add">

            <div>
                <label>Room Number</label><br>
                <input type="text" name="roomNumber" required>
            </div>

            <div>
                <label>Room Type</label><br>
                <select name="roomType">
                    <option value="2D">2D</option>
                    <option value="3D">3D</option>
                    <option value="IMAX">IMAX</option>
                    <option value="4DX">4DX</option>
                </select>
            </div>

            <div>
                <label>Capacity</label><br>
                <input type="number" name="capacity" required>
            </div>

            <button type="submit">Add Room</button>

        </form>

        <hr>

        <h2>Room List</h2>

        <table border="1" cellpadding="5" cellspacing="0">

            <thead>
                <tr>
                    <th>Room Number</th>
                    <th>Room Type</th>
                    <th>Capacity</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
            </thead>

            <tbody>
                <%
                    List<Room> roomList =
                        (List<Room>) request.getAttribute("roomList");

                    if (roomList != null) {
                        for (Room room : roomList) {
                %>

                <tr>
                    <td><%= room.getRoomNumber() %></td>
                    <td><%= room.getRoomType() %></td>
                    <td><%= room.getCapacity() %></td>
                    <td>
                        <%= room.isActive() ? "Active" : "Inactive" %>
                    </td>
                    <td>
                        <a href="RoomServlet?action=edit&id=<%= room.getRoomId() %>&page=<%= currentPage %>">
                            Edit
                        </a>
                        |
                        <a href="RoomServlet?action=delete&id=<%= room.getRoomId() %>&page=<%= currentPage %>"
                           onclick="return confirm('Deactivate this room?')">
                            Deactivate
                        </a>
                    </td>
                </tr>

                <%
                        }
                    }
                %>
            </tbody>

        </table>

        <br>
        <%
            if (totalPages > 1) {
        %>
        <div>
            <% if (currentPage > 1) { %>
            <a href="RoomServlet?action=list&page=<%= currentPage - 1 %>">Previous</a> |
            <% } %>

            <% 
                for (int i = 1; i <= totalPages; i++) { 
                    if (i == currentPage) { 
            %>
            <b>[<%= i %>]</b>
            <%      } else { %>
            <a href="RoomServlet?action=list&page=<%= i %>"><%= i %></a>
            <% 
                    }
                    if (i < totalPages) { 
            %>
            |
            <% 
                    }
                } 
            %>

            <% if (currentPage < totalPages) { %>
            | <a href="RoomServlet?action=list&page=<%= currentPage + 1 %>">Next</a>
            <% } %>
        </div>
        <% 
            } 
        %>

    </body>
</html>
=======
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Room Management — CGV Admin</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    </head>
    <body class="cgv-body">

        <%@ include file="WEB-INF/manager/_sidebar.jsp" %>

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
                                    <c:when test="${not empty sessionScope.LOGIN_USER}">${sessionScope.LOGIN_USER.fullName}</c:when>
                                    <c:otherwise>Manager</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                    </div>
                </div>
            </header>

            <div class="cgv-page">

                <div class="cgv-table-wrap">

                    <c:if test="${param.error eq 'capacity_invalid'}">
                        <div class="cgv-alert cgv-alert-danger">Capacity must be greater than 0.</div>
                    </c:if>

                    <c:if test="${param.error eq 'room_number_exists'}">
                        <div class="cgv-alert cgv-alert-danger">Room number already exists. Please use a different room number.</div>
                    </c:if>

                    <div class="cgv-toolbar">
                        <div class="cgv-pills">
                            <a href="RoomServlet" class="cgv-pill active">All Rooms</a>
                        </div>
                        <a href="room-add.jsp" class="btn--cgv" style="margin-left:auto;">
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
                                    <th>Status</th>
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
                                                    <span class="cgv-badge inactive">${room.roomType}</span>
                                                </td>
                                                <td>${room.capacity}</td>
                                                <td>
                                                    <span class="cgv-badge ${room.active ? 'active' : 'inactive'}">
                                                        ${room.active ? 'Active' : 'Inactive'}
                                                    </span>
                                                </td>
                                                <td>
                                                    <div style="display:flex;gap:8px;">
                                                        <a href="RoomServlet?action=edit&id=${room.roomId}&page=${currentPage}"
                                                           class="btn--cgv-outline">Edit</a>
                                                        <a href="${pageContext.request.contextPath}/SeatController?roomId=${room.roomId}"
                                                           class="btn--cgv-outline">
                                                            Seats
                                                        </a>
                                                        <a href="RoomServlet?action=delete&id=${room.roomId}&page=${currentPage}"
                                                           class="btn--cgv-outline"
                                                           style="color:var(--cgv-red);border-color:var(--cgv-red);"
                                                           onclick="return confirm('Deactivate room ${room.roomNumber}?')">
                                                            Deactivate
                                                        </a>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="6" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">
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
                                            onclick="location.href = 'RoomServlet?page=${p}'">${p}</button>
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
>>>>>>> Stashed changes
