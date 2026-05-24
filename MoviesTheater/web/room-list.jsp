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

        <link rel="stylesheet" href="css/bootstrap.min.css">
    </head>

    <body>

        <div class="container mt-5">

            <h1 class="mb-4 text-center">
                Room Management
            </h1>

            <div class="card mb-5">

                <div class="card-header">
                    Add New Room
                </div>

                <div class="card-body">

                    <form action="RoomServlet" method="post">

                        <input type="hidden"
                               name="action"
                               value="add">

                        <div class="row">

                            <div class="col-md-4 mb-3">

                                <label class="form-label">
                                    Room Number
                                </label>

                                <input type="text"
                                       name="roomNumber"
                                       class="form-control"
                                       required>

                            </div>

                            <div class="col-md-4 mb-3">

                                <label class="form-label">
                                    Room Type
                                </label>

                                <select name="roomType"
                                        class="form-select">

                                    <option value="2D">2D</option>
                                    <option value="3D">3D</option>
                                    <option value="IMAX">IMAX</option>
                                    <option value="VIP">VIP</option>

                                </select>

                            </div>

                            <div class="col-md-4 mb-3">

                                <label class="form-label">
                                    Capacity
                                </label>

                                <input type="number"
                                       name="capacity"
                                       class="form-control"
                                       required>

                            </div>

                        </div>

                        <button type="submit"
                                class="btn btn-primary">

                            Add Room

                        </button>

                    </form>

                </div>

            </div>

            <div class="card">

                <div class="card-header">
                    Room List
                </div>

                <div class="card-body">

                    <table class="table table-bordered table-hover">

                        <thead class="table-dark">

                            <tr>

                                <th>ID</th>
                                <th>Room Number</th>
                                <th>Room Type</th>
                                <th>Capacity</th>
                                <th>Status</th>
                                <th>Action</th>

                            </tr>

                        </thead>

                        <tbody>

                            <%
                                List<Room> roomList
                                        = (List<Room>) request.getAttribute("roomList");

                                if (roomList != null) {

                                    for (Room room : roomList) {
                            %>

                            <tr>

                                <td>
                                    <%= room.getRoomId()%>
                                </td>

                                <td>
                                    <%= room.getRoomNumber()%>
                                </td>

                                <td>
                                    <%= room.getRoomType()%>
                                </td>

                                <td>
                                    <%= room.getCapacity()%>
                                </td>

                                <td>

                                    <%
                                        if (room.isActive()) {
                                    %>

                                    <span class="badge bg-success">
                                        Active
                                    </span>

                                    <%
                                    } else {
                                    %>

                                    <span class="badge bg-danger">
                                        Inactive
                                    </span>

                                    <%
                                        }
                                    %>

                                </td>

                                <td>

                                    <a href="RoomServlet?action=edit&id=<%= room.getRoomId()%>"
                                       class="btn btn-warning btn-sm">

                                        Edit

                                    </a>

                                    <a href="RoomServlet?action=delete&id=<%= room.getRoomId()%>"
                                       class="btn btn-danger btn-sm"
                                       onclick="return confirm('Delete this room?')">

                                        Delete

                                    </a>

                                </td>

                            </tr>

                            <%
                                    }
                                }
                            %>

                        </tbody>

                    </table>

                </div>

            </div>

        </div>

        <script src="js/bootstrap.bundle.min.js"></script>

    </body>
</html>
