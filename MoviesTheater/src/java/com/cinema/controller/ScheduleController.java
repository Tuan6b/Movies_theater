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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        
        String movieIdParam = request.getParameter("movieId");
        Integer movieId = null;
        if (movieIdParam != null && !movieIdParam.isEmpty()) {
            try {
                movieId = Integer.parseInt(movieIdParam);
            } catch (NumberFormatException e) {
            }
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

        request.setAttribute("scheduleList", scheduleList);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        request.getRequestDispatcher("schedule-list.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<clsMovie> movies = movieDAO.getAllActiveMovies();
        List<Room> rooms = roomDAO.getAllRooms();
        request.setAttribute("movies", movies);
        request.setAttribute("rooms", rooms);
        request.getRequestDispatcher("schedule-add.jsp").forward(request, response);
    }

    private void addSchedule(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int movieId = Integer.parseInt(request.getParameter("movieId"));
            int roomId = Integer.parseInt(request.getParameter("roomId"));
            String showDate = request.getParameter("showDate");
            String startTime = request.getParameter("startTime");
            String status = request.getParameter("status");
            double baseTicketPrice = Double.parseDouble(request.getParameter("baseTicketPrice"));

            System.out.println("=== ADD SCHEDULE ===");
            System.out.println("movieId=" + movieId + ", roomId=" + roomId);
            System.out.println("showDate=" + showDate + ", startTime=" + startTime);
            System.out.println("baseTicketPrice=" + baseTicketPrice + ", status=" + status);

            clsMovie movie = movieDAO.getMovieById(movieId);
            if (movie == null) {
                request.getSession().setAttribute("flashError", "Movie not found.");
                response.sendRedirect("ScheduleController");
                return;
            }
            System.out.println("movie duration=" + movie.getDuration());
            int totalMinutes = movie.getDuration() + 15;
            LocalDateTime startDT = LocalDateTime.parse(showDate + "T" + startTime + ":00");
            LocalDateTime endDT = startDT.plusMinutes(totalMinutes);
            String endDate = endDT.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endTime = endDT.format(DateTimeFormatter.ofPattern("HH:mm"));
            String startTimeOnly = startTime;
            if (startTimeOnly.contains(".")) startTimeOnly = startTimeOnly.substring(0, startTimeOnly.indexOf('.'));
            if (startTimeOnly.length() > 5) startTimeOnly = startTimeOnly.substring(0, 5);
            System.out.println("calculated endDate=" + endDate + " endTime=" + endTime);

            String startDateTime = showDate + " " + startTimeOnly;
            String endDateTime = endDate + " " + endTime;
            if (scheduleDAO.hasOverlappingSchedule(roomId, startDateTime, endDateTime, -1)) {
                request.getSession().setAttribute("flashError",
                    "This room already has a schedule during this time period.");
                response.sendRedirect("ScheduleController");
                return;
            }

            Schedule s = new Schedule(0, movieId, roomId, baseTicketPrice, showDate, startTimeOnly, endTime, endDate, status);
            boolean ok = scheduleDAO.addSchedule(s);

            if (ok) {
                request.getSession().setAttribute("flashSuccess", "Schedule added successfully.");
            } else {
                request.getSession().setAttribute("flashError", "Failed to add schedule. Check server logs.");
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("flashError", "Invalid number format.");
        } catch (Exception e) {
            request.getSession().setAttribute("flashError", "Error: " + e.getMessage());
        }

        response.sendRedirect("ScheduleController");
    }

    /** Check if a schedule can be edited/deleted based on its computed status. */
    private boolean isEditable(Schedule s) {
        if ("Cancelled".equals(s.getStatus())) return true;
        try {
            String startDt = s.getShowDate() + "T" + s.getStartTime();
            LocalDateTime start = LocalDateTime.parse(startDt);
            if (!LocalDateTime.now().isBefore(start)) return false;
        } catch (Exception e) { /* allow on parse failure */ }
        return true;
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        Schedule schedule = scheduleDAO.getScheduleById(id);

        if (schedule != null && !isEditable(schedule)) {
            request.getSession().setAttribute("flashError",
                "Cannot edit an ongoing or finished schedule.");
            response.sendRedirect("ScheduleController");
            return;
        }

        List<clsMovie> movies = movieDAO.getAllActiveMovies();
        List<Room> rooms = roomDAO.getAllRooms();

        String currentPage = request.getParameter("page");
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("schedule", schedule);
        request.setAttribute("movies", movies);
        request.setAttribute("rooms", rooms);

        request.getRequestDispatcher("schedule-edit.jsp").forward(request, response);
    }

    private void updateSchedule(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("scheduleId"));
            Schedule existing = scheduleDAO.getScheduleById(id);
            if (existing != null && !isEditable(existing)) {
                request.getSession().setAttribute("flashError",
                    "Cannot edit an ongoing or finished schedule.");
                response.sendRedirect("ScheduleController?page=" + request.getParameter("page"));
                return;
            }
            int movieId = Integer.parseInt(request.getParameter("movieId"));
            int roomId = Integer.parseInt(request.getParameter("roomId"));
            String showDate = request.getParameter("showDate");
            String startTime = request.getParameter("startTime");
            String status = request.getParameter("status");
            double baseTicketPrice = Double.parseDouble(request.getParameter("baseTicketPrice"));

            clsMovie movie = movieDAO.getMovieById(movieId);
            if (movie == null) {
                request.getSession().setAttribute("flashError", "Movie not found.");
                response.sendRedirect("ScheduleController?page=" + request.getParameter("page"));
                return;
            }
            int totalMinutes = movie.getDuration() + 15;
            LocalDateTime startDT = LocalDateTime.parse(showDate + "T" + startTime + ":00");
            LocalDateTime endDT = startDT.plusMinutes(totalMinutes);
            String endDate = endDT.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endTime = endDT.format(DateTimeFormatter.ofPattern("HH:mm"));

            String startDateTime = showDate + " " + startTime;
            String endDateTime = endDate + " " + endTime;
            if (scheduleDAO.hasOverlappingSchedule(roomId, startDateTime, endDateTime, id)) {
                request.getSession().setAttribute("flashError",
                    "This room already has a schedule during this time period.");
                response.sendRedirect("ScheduleController?page=" + request.getParameter("page"));
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

        String page = request.getParameter("page");
        if (page == null || page.isEmpty()) page = "1";
        response.sendRedirect("ScheduleController?page=" + page);
    }

    private void deleteSchedule(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Schedule schedule = scheduleDAO.getScheduleById(id);
            if (schedule != null && !isEditable(schedule)) {
                request.getSession().setAttribute("flashError",
                    "Cannot delete an ongoing or finished schedule.");
                response.sendRedirect("ScheduleController?page=" + request.getParameter("page"));
                return;
            }
            boolean ok = scheduleDAO.deleteSchedule(id);

            if (ok) {
                request.getSession().setAttribute("flashSuccess", "Schedule deleted successfully.");
            } else {
                request.getSession().setAttribute("flashError", "Failed to delete schedule.");
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("flashError", "Invalid schedule ID.");
        } catch (Exception e) {
            request.getSession().setAttribute("flashError", "Error: " + e.getMessage());
        }

        String page = request.getParameter("page");
        if (page == null || page.isEmpty()) page = "1";
        response.sendRedirect("ScheduleController?page=" + page);
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
