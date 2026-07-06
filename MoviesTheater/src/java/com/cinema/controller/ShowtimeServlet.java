/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.cinema.controller;

import com.cinema.dao.tbMovie;
import com.cinema.dao.tbSchedule;
import com.cinema.model.clsMovie;
import com.cinema.model.clsSchedule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

/**
 * Controller to handle viewing Movie Showtime Schedules (UC22).
 * Follows the NetBeans template style and processRequest pattern.
 *
 * @author TBinh
 */
public class ShowtimeServlet extends HttpServlet {

    private final tbMovie movieDAO = new tbMovie();
    private final tbSchedule scheduleDAO = new tbSchedule();

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String method = request.getMethod();

        if ("GET".equalsIgnoreCase(method)) {
            // === GET: View movie showtimes by movie and date ===
            String movieIdStr = request.getParameter("movieId");
            
            if (movieIdStr == null || movieIdStr.trim().isEmpty()) {
                // If no movieId is supplied, redirect to the index homepage
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }

            try {
                int movieId = Integer.parseInt(movieIdStr);
                clsMovie movie = movieDAO.getMovieById(movieId);

                if (movie == null) {
                    request.setAttribute("ERROR", "Movie not found.");
                    request.getRequestDispatcher("/view/common/Error.jsp").forward(request, response);
                    return;
                }

                // Get all dates that have scheduled showtimes for this movie
                List<Date> availableDates = scheduleDAO.getAvailableDatesForMovie(movieId);
                String selectedDateStr = request.getParameter("date");

                if (selectedDateStr == null || selectedDateStr.trim().isEmpty()) {
                    if (availableDates != null && !availableDates.isEmpty()) {
                        // Default to the first available show date
                        selectedDateStr = availableDates.get(0).toString();
                    } else {
                        // Fallback to today's date if no showtimes exist
                        selectedDateStr = new Date(System.currentTimeMillis()).toString();
                    }
                }

                // Fetch showtimes for the selected date
                List<clsSchedule> schedules = scheduleDAO.getSchedulesByMovieAndDate(movieId, selectedDateStr);

                request.setAttribute("movie", movie);
                request.setAttribute("availableDates", availableDates);
                request.setAttribute("selectedDate", selectedDateStr);
                request.setAttribute("schedules", schedules);

                request.getRequestDispatcher("/view/customer/showtimes.jsp").forward(request, response);

            } catch (NumberFormatException e) {
                request.setAttribute("ERROR", "Invalid Movie ID format.");
                request.getRequestDispatcher("/view/common/Error.jsp").forward(request, response);
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("ERROR", "An unexpected error occurred while fetching showtimes.");
                request.getRequestDispatcher("/view/common/Error.jsp").forward(request, response);
            }

        } else if ("POST".equalsIgnoreCase(method)) {
            request.setCharacterEncoding("UTF-8");
            // POST is not supported for schedule viewing
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
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
        return "Showtime Schedule Controller";
    }// </editor-fold>

}
