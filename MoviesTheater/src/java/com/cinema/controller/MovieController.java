package com.cinema.controller;

import com.cinema.dao.tbMovie;
import com.cinema.model.clsMovie;
import java.io.IOException;
import java.sql.Date;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author vjphoalac
 */
public class MovieController extends HttpServlet {

    private final tbMovie movieDAO = new tbMovie();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        try {
            if ("add".equals(action)) {
                request.getRequestDispatcher("add_movie.jsp").forward(request, response);
            } else {
                // permanently display movie list (instead of edit and soft delete movie
                List<clsMovie> movieList = movieDAO.getAllMoviesAdmin();
                request.setAttribute("movieList", movieList);
                request.getRequestDispatcher("manage_movie.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi tải trang: " + e.getMessage());
            request.getRequestDispatcher("Error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        
        try {
            if ("add".equals(action)) {
                // Get data from Add Movie form
                String movieName = request.getParameter("movieName");
                Date releaseDate = Date.valueOf(request.getParameter("releaseDate"));
                int duration = Integer.parseInt(request.getParameter("duration"));
                int ageRestriction = Integer.parseInt(request.getParameter("ageRestriction"));
                String language = request.getParameter("language");
                String subtitle = request.getParameter("subtitle");
                String director = request.getParameter("director");
                String country = request.getParameter("country");
                String cast = request.getParameter("cast");
                String poster = request.getParameter("poster");
                String trailer = request.getParameter("trailer");
                String description = request.getParameter("description");
                boolean isActive = request.getParameter("isActive") != null; // Checkbox

                clsMovie newMovie = new clsMovie(0, movieName, description, duration, releaseDate, 
                                              poster, trailer, language, subtitle, director, 
                                              cast, country, ageRestriction, isActive);

                // Insert movie
                boolean isSuccess = movieDAO.insertMovie(newMovie);

                // Return result
                if (isSuccess) {
                    request.getSession().setAttribute("success", "Đã thêm phim mới thành công!");
                    response.sendRedirect(request.getContextPath() + "/MovieController");
                } else {
                    request.setAttribute("error", "Không thể lưu phim vào hệ thống.");
                    request.getRequestDispatcher("add_movie.jsp").forward(request, response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi thao tác hoặc sai định dạng dữ liệu: " + e.getMessage());
            request.getRequestDispatcher("add_movie.jsp").forward(request, response);
        }
    }
}
