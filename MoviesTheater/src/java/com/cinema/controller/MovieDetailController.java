package com.cinema.controller;

import com.cinema.dao.tbMovie;
import com.cinema.dao.tbSchedule;
import com.cinema.model.clsMovie;
import com.cinema.model.clsSchedule;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

@WebServlet(name = "MovieDetailController", urlPatterns = {"/MovieDetailController", "/movie-detail"})
public class MovieDetailController extends HttpServlet {

    private final tbMovie movieDAO = new tbMovie();
    private final tbSchedule scheduleDAO = new tbSchedule();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int movieId = Integer.parseInt(request.getParameter("id"));
            clsMovie movie = movieDAO.getMovieById(movieId);
            
            if (movie == null || !movie.isActive()) {
                response.sendRedirect(request.getContextPath() + "/HomeController");
                return;
            }

            List<Date> availableDates = scheduleDAO.getAvailableDatesForMovie(movieId);
            
            String selectedDate = request.getParameter("date");
            if ((selectedDate == null || selectedDate.trim().isEmpty()) && !availableDates.isEmpty()) {
                selectedDate = availableDates.get(0).toString();
            }

            List<clsSchedule> schedules = null;
            if (selectedDate != null && !selectedDate.trim().isEmpty()) {
                schedules = scheduleDAO.getSchedulesByMovieAndDate(movieId, selectedDate);
            }

            request.setAttribute("movie", movie);
            request.setAttribute("availableDates", availableDates);
            request.setAttribute("selectedDate", selectedDate);
            request.setAttribute("schedules", schedules);

            request.getRequestDispatcher("/movie-detail.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/HomeController");
        }
    }
}

