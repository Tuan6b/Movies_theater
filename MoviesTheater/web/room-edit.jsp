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
    </head>

    <body>

        <h1>Edit Room</h1>

        <form action="RoomServlet" method="post">

            <input type="hidden" name="action" value="update">

            <input type="hidden"
                   name="roomId"
                   value="<%= room.getRoomId() %>">

            <div>
                <label>Room Number</label><br>
                <input type="text"
                       name="roomNumber"
                       value="<%= room.getRoomNumber() %>"
                       required>
            </div>

            <br>

            <div>
                <label>Room Type</label><br>
                <select name="roomType">

                    <option value="2D"
                        <%= room.getRoomType().equals("2D") ? "selected" : "" %>>
                        2D
                    </option>

                    <option value="3D"
                        <%= room.getRoomType().equals("3D") ? "selected" : "" %>>
                        3D
                    </option>

                    <option value="IMAX"
                        <%= room.getRoomType().equals("IMAX") ? "selected" : "" %>>
                        IMAX
                    </option>

                    <option value="VIP"
                        <%= room.getRoomType().equals("VIP") ? "selected" : "" %>>
                        VIP
                    </option>

                </select>
            </div>

            <br>

            <div>
                <label>Capacity</label><br>
                <input type="number"
                       name="capacity"
                       value="<%= room.getCapacity() %>"
                       required>
            </div>

            <br>

            <div>
                <label>
                    <input type="checkbox"
                           name="active"
                        <%= room.isActive() ? "checked" : "" %>>
                    Active
                </label>
            </div>

            <br>

            <button type="submit">Update Room</button>

            <a href="RoomServlet">Back</a>

        </form>

    </body>
</html>
