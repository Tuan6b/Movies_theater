<%-- 
    Document   : room-edit
    Created on : May 24, 2026, 10:29:51 PM
    Author     : Tuan Phong Nguyen
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.cinema.model.Room"%>
<%
    Room room = (Room) request.getAttribute("room");
    String origPage = (String) request.getAttribute("currentPage");
    if (origPage == null) {
        origPage = request.getParameter("page");
    }
    if (origPage == null || origPage.isEmpty()) origPage = "1";
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type"
              content="text/html; charset=UTF-8">

        <title>Edit Room</title>
    </head>

    <body>

        <% if ("capacity_invalid".equals(request.getParameter("error"))) { %>
        <script>alert('Capacity must be greater than 0!');</script>
        <% } %>

        <h1>Edit Room</h1>

        <form action="RoomServlet?page=<%= origPage %>" method="post">

<<<<<<< Updated upstream
            <input type="hidden" name="action" value="update">
=======
            <c:if test="${param.error eq 'room_number_exists'}">
                <div class="cgv-alert cgv-alert-danger">Room number already exists.</div>
            </c:if>

            <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:32px;">
                <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:24px;">
                    ROOM DETAILS
                </div>
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
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

            <a href="RoomServlet?page=<%= origPage %>">Back</a>

        </form>

    </body>
</html>
=======
        </div>
    </div>
</div>
</body>
</html>
>>>>>>> Stashed changes
