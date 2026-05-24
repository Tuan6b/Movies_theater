<%-- 
    Document   : room-edit
    Created on : May 24, 2026, 10:29:51 PM
    Author     : Tuan Phong Nguyen
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.cinema.model.Room"%>
<%
    Room room = (Room) request.getAttribute("room");
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type"
              content="text/html; charset=UTF-8">

        <title>Edit Room</title>

        <link rel="stylesheet"
              href="css/bootstrap.min.css">
    </head>

    <body>

        <div class="container mt-5">

            <div class="row justify-content-center">

                <div class="col-md-8">

                    <div class="card">

                        <div class="card-header">
                            Edit Room
                        </div>

                        <div class="card-body">

                            <form action="RoomServlet"
                                  method="post">

                                <input type="hidden"
                                       name="action"
                                       value="update">

                                <input type="hidden"
                                       name="roomId"
                                       value="<%= room.getRoomId()%>">

                                <div class="mb-3">

                                    <label class="form-label">
                                        Room Number
                                    </label>

                                    <input type="text"
                                           name="roomNumber"
                                           class="form-control"
                                           value="<%= room.getRoomNumber()%>"
                                           required>

                                </div>

                                <div class="mb-3">

                                    <label class="form-label">
                                        Room Type
                                    </label>

                                    <select name="roomType"
                                            class="form-select">

                                        <option value="2D"
                                                <%= room.getRoomType().equals("2D")
                                                        ? "selected"
                                                        : ""%>>

                                            2D

                                        </option>

                                        <option value="3D"
                                                <%= room.getRoomType().equals("3D")
                                                        ? "selected"
                                                        : ""%>>

                                            3D

                                        </option>

                                        <option value="IMAX"
                                                <%= room.getRoomType().equals("IMAX")
                                                        ? "selected"
                                                        : ""%>>

                                            IMAX

                                        </option>

                                        <option value="VIP"
                                                <%= room.getRoomType().equals("VIP")
                                                        ? "selected"
                                                        : ""%>>

                                            VIP

                                        </option>

                                    </select>

                                </div>

                                <div class="mb-3">

                                    <label class="form-label">
                                        Capacity
                                    </label>

                                    <input type="number"
                                           name="capacity"
                                           class="form-control"
                                           value="<%= room.getCapacity()%>"
                                           required>

                                </div>

                                <div class="form-check mb-4">

                                    <input class="form-check-input"
                                           type="checkbox"
                                           name="active"
                                           <%= room.isActive()
                                                   ? "checked"
                                                   : ""%>>

                                    <label class="form-check-label">
                                        Active
                                    </label>

                                </div>

                                <button type="submit"
                                        class="btn btn-primary">

                                    Update Room

                                </button>

                                <a href="RoomServlet"
                                   class="btn btn-secondary">

                                    Back

                                </a>

                            </form>

                        </div>

                    </div>

                </div>

            </div>

        </div>

        <script src="js/bootstrap.bundle.min.js"></script>

    </body>
</html>
