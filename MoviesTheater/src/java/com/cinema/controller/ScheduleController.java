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

        int offset = (page - 1) * recordsPerPage;
        List<Schedule> scheduleList = scheduleDAO.getSchedulesByPage(offset, recordsPerPage);
        int totalRecords = scheduleDAO.getTotalSchedulesCount();
        int totalPages = (int) Math.ceil((double) totalRecords / recordsPerPage);

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
            String endTime = request.getParameter("endTime");
            String status = request.getParameter("status");

            double baseTicketPrice = Double.parseDouble(request.getParameter("baseTicketPrice"));
            Schedule s = new Schedule(0, movieId, roomId, baseTicketPrice, showDate, startTime, endTime, status);
            boolean ok = scheduleDAO.addSchedule(s);

            if (ok) {
                request.getSession().setAttribute("flashSuccess", "Schedule added successfully.");
            } else {
                request.getSession().setAttribute("flashError", "Failed to add schedule.");
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("flashError", "Invalid number format for Movie ID or Room ID.");
        } catch (Exception e) {
            request.getSession().setAttribute("flashError", "Error: " + e.getMessage());
        }

        response.sendRedirect("ScheduleController");
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        Schedule schedule = scheduleDAO.getScheduleById(id);
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
            int movieId = Integer.parseInt(request.getParameter("movieId"));
            int roomId = Integer.parseInt(request.getParameter("roomId"));
            String showDate = request.getParameter("showDate");
            String startTime = request.getParameter("startTime");
            String endTime = request.getParameter("endTime");
            String status = request.getParameter("status");

            double baseTicketPrice = Double.parseDouble(request.getParameter("baseTicketPrice"));
            Schedule s = new Schedule(id, movieId, roomId, baseTicketPrice, showDate, startTime, endTime, status);
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
