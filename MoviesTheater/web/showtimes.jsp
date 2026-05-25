<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.cinema.model.clsMovie,com.cinema.model.clsSchedule" %>
<%@ page import="java.util.*,java.sql.Date,java.text.SimpleDateFormat" %>

<%
    clsMovie movie = (clsMovie) request.getAttribute("movie");
    List<Date> availableDates = (List<Date>) request.getAttribute("availableDates");
    List<clsSchedule> schedules = (List<clsSchedule>) request.getAttribute("schedules");
    String selectedDate = (String) request.getAttribute("selectedDate");

    SimpleDateFormat dayFormat = new SimpleDateFormat("E, dd/MM/yyyy");
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
%>

<!DOCTYPE html>
<html>
<head>
    <title>
        Showtimes - <%= movie != null ? movie.getMovieName() : "CGV Cinema" %>
    </title>
</head>

<body>

<h1>CGV Cinema - Showtime Schedule</h1>

<a href="${pageContext.request.contextPath}/">
    Back to Home page
</a>

<hr/>

<% if (movie == null) { %>

    <p style="color:red;">Movie details could not be found.</p>

<% } else { %>

    <!-- Movie Information -->
    <h2>Movie Information</h2>

    <table border="1" cellpadding="8" cellspacing="0">
        <tr>

            <!-- Poster -->
            <td width="200">
                <% if (movie.getPoster() != null && !movie.getPoster().isEmpty()) { %>

                    <img src="<%= movie.getPoster() %>"
                         alt="<%= movie.getMovieName() %>"
                         width="180"/>

                <% } else { %>

                    <strong>No Poster Available</strong>

                <% } %>
            </td>

            <!-- Movie Details -->
            <td>
                <h3><%= movie.getMovieName() %></h3>

                <p><strong>Duration:</strong> <%= movie.getDuration() %> minutes</p>

                <p><strong>Language:</strong> <%= movie.getLanguage() %></p>

                <p>
                    <strong>Subtitle:</strong>
                    <%= movie.getSubtitle() != null ? movie.getSubtitle() : "None" %>
                </p>

                <p>
                    <strong>Director:</strong>
                    <%= movie.getDirector() != null ? movie.getDirector() : "N/A" %>
                </p>

                <p>
                    <strong>Cast:</strong>
                    <%= movie.getCast() != null ? movie.getCast() : "N/A" %>
                </p>

                <p>
                    <strong>Country:</strong>
                    <%= movie.getCountry() != null ? movie.getCountry() : "N/A" %>
                </p>

                <p>
                    <strong>Age Restriction:</strong>
                    <%= movie.getAgeRestriction() > 0
                            ? "C" + movie.getAgeRestriction()
                            : "P (General Audience)" %>
                </p>

                <p>
                    <strong>Description:</strong>
                    <%= movie.getDescription() != null
                            ? movie.getDescription()
                            : "Description coming soon." %>
                </p>
            </td>

        </tr>
    </table>

    <!-- Date Selection -->
    <h2>Select Date</h2>

    <p>Click on a date to see the showtime schedules for that day:</p>

    <ul>

        <% if (availableDates != null && !availableDates.isEmpty()) {

            for (Date d : availableDates) {

                boolean active = d.toString().equals(selectedDate);
        %>

            <li>

                <% if (active) { %>

                    <strong>
                        <%= dayFormat.format(d) %> (Selected)
                    </strong>

                <% } else { %>

                    <a href="${pageContext.request.contextPath}/showtimes?movieId=<%= movie.getMovieId() %>&date=<%= d %>">

                        <%= dayFormat.format(d) %>

                    </a>

                <% } %>

            </li>

        <%  }

        } else { %>

            <li>No showtimes dates scheduled for this movie.</li>

        <% } %>

    </ul>

    <!-- Showtime -->
    <h2>Showtimes for <%= selectedDate %></h2>

    <% if (schedules != null && !schedules.isEmpty()) { %>

        <table border="1" cellpadding="10" cellspacing="0">

            <thead>
                <tr>
                    <th>Room Number</th>
                    <th>Screen Type</th>
                    <th>Seating Capacity</th>
                    <th>Available Showtime Suat Chieu (Click to Book)</th>
                </tr>
            </thead>

            <tbody>

            <% for (clsSchedule s : schedules) { %>

                <tr>

                    <td>
                        <strong>
                            Room <%= s.getRoom().getRoomNumber() %>
                        </strong>
                    </td>

                    <td>
                        <%= s.getRoom().getRoomType() %>
                    </td>

                    <td>
                        <%= s.getRoom().getCapacity() %> seats
                    </td>

                    <td>

                        <strong>
                            [<%= timeFormat.format(s.getStartTime()) %>]
                        </strong>

                        <br/>

                        <small style="color:gray;">
                            Base Price:
                            <%= String.format("%,.0f", s.getBaseTicketPrice()) %> VND
                        </small>

                    </td>

                </tr>

            <% } %>

            </tbody>

        </table>

    <% } else { %>

        <p style="color:gray;">
            No showtimes available for the selected date.
        </p>

    <% } %>

<% } %>

<hr/>

<p>&copy; 2026 CGV Cinema.</p>

</body>
</html>