<%-- 
    Document   : room-list
    Created on : May 24, 2026, 10:11:30 PM
    Author     : Tuan Phong Nguyen
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.cinema.model.Room"%>
<!DOCTYPE html>
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
                    <option value="VIP">VIP</option>
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
