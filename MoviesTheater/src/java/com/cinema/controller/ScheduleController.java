package com.cinema.controller;

import com.cinema.dao.RoomDAO;
import com.cinema.dao.ScheduleDAO;
import com.cinema.dao.tbMovie;
import com.cinema.model.Schedule;
import com.cinema.model.clsMovie;
import com.cinema.model.Room;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ScheduleController extends HttpServlet {

    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final tbMovie movieDAO = new tbMovie();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "add":
                addSchedule(request, response);
                break;
            case "showAddForm":
                showAddForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "update":
                updateSchedule(request, response);
                break;
            case "delete":
                deleteSchedule(request, response);
                break;
            default:
                listSchedules(request, response);
                break;
        }
    }

    private void listSchedules(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = 1;
        int recordsPerPage = 10;

        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                page = Integer.parseInt(pageParam);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        List<clsMovie> allMovies = movieDAO.getAllActiveMovies();

        String movieIdParam = request.getParameter("movieId");
        Integer movieId = null;
        if (movieIdParam != null && !movieIdParam.isEmpty()) {
            try {
                movieId = Integer.parseInt(movieIdParam);
            } catch (NumberFormatException e) {
            }
        }
        if (movieId == null && !allMovies.isEmpty()) {
            movieId = allMovies.get(0).getMovieId();
        }

        int offset = (page - 1) * recordsPerPage;
        List<Schedule> scheduleList;
        int totalRecords;

        if (movieId != null) {
            scheduleList = scheduleDAO.getSchedulesByMovieIdAndPage(movieId, offset, recordsPerPage);
            totalRecords = scheduleDAO.getTotalSchedulesCountByMovieId(movieId);
            request.setAttribute("selectedMovieId", movieId);
        } else {
            scheduleList = scheduleDAO.getSchedulesByPage(offset, recordsPerPage);
            totalRecords = scheduleDAO.getTotalSchedulesCount();
        }

        int totalPages = (int) Math.ceil((double) totalRecords / recordsPerPage);

        LocalDateTime now = LocalDateTime.now();
        for (Schedule s : scheduleList) {
            if ("Cancelled".equals(s.getStatus())) continue;
            String startStr = s.getShowDate() + "T" + s.getStartTime();
            if (!startStr.contains("T") || startStr.length() < 16) continue;
            String endDt = (s.getEndDate() != null ? s.getEndDate() : s.getShowDate()) + "T" + s.getEndTime();
            if (!endDt.contains("T") || endDt.length() < 16) continue;
            try {
                LocalDateTime start = LocalDateTime.parse(startStr);
                LocalDateTime end = LocalDateTime.parse(endDt);
                if (now.isAfter(end)) {
                    s.setStatus("Finished");
                } else if (now.isAfter(start) && now.isBefore(end)) {
                    s.setStatus("Ongoing");
                } else {
                    s.setStatus("Scheduled");
                }
            } catch (Exception e) {
                System.out.println("Skip schedule " + s.getScheduleID() + " due to parse error: " + startStr + " / " + endDt);
            }
        }

        request.setAttribute("movieList", allMovies);
        request.setAttribute("scheduleList", scheduleList);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        java.util.Map<Integer, String> roomNameMap = new java.util.HashMap<>();
        java.util.Map<Integer, String> movieNameMap = new java.util.HashMap<>();
        List<Room> allRoomList = roomDAO.getAllRooms();
        for (Room r : allRoomList) roomNameMap.put(r.getRoomId(), r.getRoomNumber());
        for (clsMovie m : allMovies) movieNameMap.put(m.getMovieId(), m.getMovieName());
        request.setAttribute("roomNameMap", roomNameMap);
        request.setAttribute("movieNameMap", movieNameMap);

        request.getRequestDispatcher("/view/manager/schedule-list.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String movieIdParam = request.getParameter("movieId");
        int movieId = 0;
        if (movieIdParam != null && !movieIdParam.isEmpty()) {
            movieId = Integer.parseInt(movieIdParam);
        }
        clsMovie movie = movieDAO.getMovieById(movieId);
        if (movie == null) {
            List<clsMovie> allMovies = movieDAO.getAllActiveMovies();
            if (!allMovies.isEmpty()) {
                movie = allMovies.get(0);
            }
        }
        List<Room> rooms = roomDAO.getAllRooms();

        request.setAttribute("movie", movie);
        request.setAttribute("rooms", rooms);
        request.getRequestDispatcher("/view/manager/schedule-add.jsp").forward(request, response);
    }

    private void addSchedule(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int movieId = 0;
        try {
            movieId = Integer.parseInt(request.getParameter("movieId"));
            String[] roomIdArr = request.getParameterValues("roomIds");
            String showDate = request.getParameter("showDate");
            if (showDate != null && LocalDate.parse(showDate).isBefore(LocalDate.now())) {
                request.getSession().setAttribute("flashError", "Cannot create a schedule with a past date.");
                response.sendRedirect("ScheduleController?action=showAddForm&movieId=" + movieId);
                return;
            }
            String status = request.getParameter("status");
            double baseTicketPrice = Double.parseDouble(request.getParameter("baseTicketPrice"));

            if (roomIdArr == null || roomIdArr.length == 0) {
                request.getSession().setAttribute("flashError", "Please select at least one room.");
                response.sendRedirect("ScheduleController?movieId=" + movieId);
                return;
            }

            clsMovie movie = movieDAO.getMovieById(movieId);
            if (movie == null) {
                request.getSession().setAttribute("flashError", "Movie not found.");
                response.sendRedirect("ScheduleController?movieId=" + movieId);
                return;
            }

            int totalMinutes = movie.getDuration() + 15;
            int created = 0;
            int skipped = 0;
            List<String> errors = new ArrayList<>();

            List<Room> allRooms = roomDAO.getAllRooms();
            java.util.Map<Integer, String> roomMap = new java.util.HashMap<>();
            for (Room r : allRooms) roomMap.put(r.getRoomId(), r.getRoomNumber());

            for (String roomIdStr : roomIdArr) {
                int roomId = Integer.parseInt(roomIdStr);
                String roomLabel = roomMap.getOrDefault(roomId, "Room " + roomId);

                String[] startTimes = request.getParameterValues("startTime_" + roomId);
                if (startTimes == null || startTimes.length == 0) {
                    errors.add(roomLabel + " — no start times, skipped.");
                    skipped++;
                    continue;
                }

                for (String startTime : startTimes) {
                    String cleanStart = startTime;
                    if (cleanStart == null || cleanStart.trim().isEmpty()) continue;
                    if (cleanStart.contains(".")) cleanStart = cleanStart.substring(0, cleanStart.indexOf('.'));
                    if (cleanStart.length() > 5) cleanStart = cleanStart.substring(0, 5);

                    LocalDateTime startDT = LocalDateTime.parse(showDate + "T" + cleanStart + ":00");
                    if (startDT.isBefore(LocalDateTime.now())) {
                        errors.add(roomLabel + " @ " + cleanStart + " — time has already passed, skipped.");
                        skipped++;
                        continue;
                    }
                    LocalDateTime endDT = startDT.plusMinutes(totalMinutes);
                    String endDate = endDT.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String endTime = endDT.format(DateTimeFormatter.ofPattern("HH:mm"));

                    String startDateTime = showDate + " " + cleanStart;
                    String endDateTime = endDate + " " + endTime;

                    if (scheduleDAO.hasOverlappingSchedule(roomId, startDateTime, endDateTime, -1)) {
                        errors.add(roomLabel + " @ " + cleanStart + " — time conflict, skipped.");
                        skipped++;
                        continue;
                    }

                    Schedule s = new Schedule(0, movieId, roomId, baseTicketPrice,
                            showDate, cleanStart, endTime, endDate, status);
                    if (scheduleDAO.addSchedule(s)) {
                        created++;
                    } else {
                        errors.add(roomLabel + " @ " + cleanStart + " — DB error, skipped.");
                        skipped++;
                    }
                }
            }

            StringBuilder msg = new StringBuilder();
            if (created > 0) {
                msg.append(created).append(" schedule(s) created.");
            }
            if (skipped > 0) {
                if (msg.length() > 0) msg.append(" ");
                msg.append(skipped).append(" skipped.");
            }
            if (!errors.isEmpty()) {
                request.getSession().setAttribute("flashErrorList", errors);
                request.getSession().setAttribute("flashError", msg.toString());
            } else if (created > 0) {
                request.getSession().setAttribute("flashSuccess", msg.toString());
            } else {
                request.getSession().setAttribute("flashError", "No schedules were created.");
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("flashError", "Invalid number format.");
        } catch (Exception e) {
            request.getSession().setAttribute("flashError", "Error: " + e.getMessage());
        }

        response.sendRedirect("ScheduleController?movieId=" + movieId);
    }

    /** Check if a schedule can be edited (only Scheduled). */
    private boolean isEditable(Schedule s) {
        if ("Cancelled".equals(s.getStatus())) return false;
        try {
            String startDt = s.getShowDate() + "T" + s.getStartTime();
            String endDt = (s.getEndDate() != null ? s.getEndDate() : s.getShowDate()) + "T" + s.getEndTime();
            LocalDateTime start = LocalDateTime.parse(startDt);
            LocalDateTime end = LocalDateTime.parse(endDt);
            LocalDateTime now = LocalDateTime.now();
            if (!now.isBefore(start) && now.isBefore(end)) return false; // Ongoing
            if (!now.isBefore(end)) return false; // Finished
        } catch (Exception e) { /* allow on parse failure */ }
        return true;
    }

    /** Check if a schedule can be deleted (not Ongoing). */
    private boolean isDeletable(Schedule s) {
        if ("Cancelled".equals(s.getStatus())) return true;
        try {
            String startDt = s.getShowDate() + "T" + s.getStartTime();
            String endDt = (s.getEndDate() != null ? s.getEndDate() : s.getShowDate()) + "T" + s.getEndTime();
            LocalDateTime start = LocalDateTime.parse(startDt);
            LocalDateTime end = LocalDateTime.parse(endDt);
            LocalDateTime now = LocalDateTime.now();
            if (!now.isBefore(start) && now.isBefore(end)) return false; // Ongoing only
        } catch (Exception e) { /* allow on parse failure */ }
        return true;
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        Schedule schedule = scheduleDAO.getScheduleById(id);

        if (schedule != null && !isEditable(schedule)) {
            request.getSession().setAttribute("flashError",
                "Only scheduled schedules can be edited.");
            response.sendRedirect("ScheduleController?movieId=" + schedule.getMovieID());
            return;
        }

        List<Room> rooms = roomDAO.getAllRooms();
        String movieName = "";
        if (schedule != null) {
            clsMovie m = movieDAO.getMovieById(schedule.getMovieID());
            if (m != null) movieName = m.getMovieName();
        }

        request.setAttribute("schedule", schedule);
        request.setAttribute("rooms", rooms);
        request.setAttribute("editMovieName", movieName);

        request.getRequestDispatcher("/view/manager/schedule-edit.jsp").forward(request, response);
    }

    private void updateSchedule(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int movieId = 0;
        try {
            int id = Integer.parseInt(request.getParameter("scheduleId"));
            Schedule existing = scheduleDAO.getScheduleById(id);
            if (existing == null) {
                request.getSession().setAttribute("flashError", "Schedule not found.");
                response.sendRedirect("ScheduleController");
                return;
            }
            if (!isEditable(existing)) {
                request.getSession().setAttribute("flashError",
                    "Only scheduled schedules can be edited.");
                response.sendRedirect("ScheduleController?movieId=" + existing.getMovieID());
                return;
            }
            movieId = Integer.parseInt(request.getParameter("movieId"));
            int roomId = Integer.parseInt(request.getParameter("roomId"));
            String showDate = request.getParameter("showDate");
            if (showDate != null && LocalDate.parse(showDate).isBefore(LocalDate.now())) {
                request.getSession().setAttribute("flashError", "Cannot set a schedule date in the past.");
                response.sendRedirect("ScheduleController?movieId=" + movieId);
                return;
            }
            String startTime = request.getParameter("startTime");
            String status = request.getParameter("status");
            double baseTicketPrice = Double.parseDouble(request.getParameter("baseTicketPrice"));

            clsMovie movie = movieDAO.getMovieById(movieId);
            if (movie == null) {
                request.getSession().setAttribute("flashError", "Movie not found.");
                response.sendRedirect("ScheduleController?movieId=" + movieId);
                return;
            }
            int totalMinutes = movie.getDuration() + 15;
            LocalDateTime startDT = LocalDateTime.parse(showDate + "T" + startTime + ":00");
            if (startDT.isBefore(LocalDateTime.now())) {
                request.getSession().setAttribute("flashError",
                    "Cannot set a start time in the past.");
                response.sendRedirect("ScheduleController?movieId=" + movieId);
                return;
            }
            LocalDateTime endDT = startDT.plusMinutes(totalMinutes);
            String endDate = endDT.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endTime = endDT.format(DateTimeFormatter.ofPattern("HH:mm"));

            String startDateTime = showDate + " " + startTime;
            String endDateTime = endDate + " " + endTime;
            if (scheduleDAO.hasOverlappingSchedule(roomId, startDateTime, endDateTime, id)) {
                request.getSession().setAttribute("flashError",
                    "This room already has a schedule during this time period.");
                response.sendRedirect("ScheduleController?movieId=" + movieId);
                return;
            }

            Schedule s = new Schedule(id, movieId, roomId, baseTicketPrice, showDate, startTime, endTime, endDate, status);
            boolean ok = scheduleDAO.updateSchedule(s);

            if (ok) {
                request.getSession().setAttribute("flashSuccess", "Schedule updated successfully.");
            } else {
                request.getSession().setAttribute("flashError", "Failed to update schedule.");
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("flashError", "Invalid number format.");
        } catch (Exception e) {
            request.getSession().setAttribute("flashError", "Error: " + e.getMessage());
        }

        response.sendRedirect("ScheduleController?movieId=" + movieId);
    }

    private void deleteSchedule(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int movieId = 0;
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Schedule schedule = scheduleDAO.getScheduleById(id);
            if (schedule == null) {
                request.getSession().setAttribute("flashError", "Schedule not found.");
                response.sendRedirect("ScheduleController");
                return;
            }
            movieId = schedule.getMovieID();
            if (!isDeletable(schedule)) {
                request.getSession().setAttribute("flashError",
                    "Cannot delete an ongoing schedule.");
                response.sendRedirect("ScheduleController?movieId=" + movieId);
                return;
            }
            boolean ok = scheduleDAO.deleteSchedule(id);

            if (ok) {
                request.getSession().setAttribute("flashSuccess", "Schedule deleted successfully.");
            } else {
                scheduleDAO.cancelSchedule(id);
                request.getSession().setAttribute("flashSuccess", "Schedule has been cancelled (had tickets, cannot be fully deleted).");
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("flashError", "Invalid schedule ID.");
        } catch (Exception e) {
            request.getSession().setAttribute("flashError", "Error: " + e.getMessage());
        }

        response.sendRedirect("ScheduleController?movieId=" + movieId);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
