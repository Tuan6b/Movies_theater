package com.cinema.controller;

import com.cinema.dao.tbMovie;
import com.cinema.model.clsMovie;
import java.io.IOException;
import java.sql.Date;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MovieController extends HttpServlet {

    private final tbMovie movieDAO = new tbMovie();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        try {
            if ("add".equals(action)) {
                // Đẩy list thể loại ra JSP để render Checkbox
                request.setAttribute("genreList", new com.cinema.dao.GenreDAO().getAllGenres());
                request.getRequestDispatcher("add_movie.jsp").forward(request, response);

            } else if ("edit".equals(action)) {
                int movieId = Integer.parseInt(request.getParameter("id"));
                clsMovie movie = movieDAO.getMovieById(movieId);
                request.setAttribute("movie", movie);

                // Đẩy list thể loại và các thể loại phim đang có sẵn ra JSP
                request.setAttribute("genreList", new com.cinema.dao.GenreDAO().getAllGenres());
                request.setAttribute("selectedGenres", movieDAO.getGenreIdsByMovie(movieId));
                
                request.getRequestDispatcher("edit_movie.jsp").forward(request, response);
            } else {
                String filter = request.getParameter("filter");
                if (filter == null || filter.isEmpty()) {
                    filter = "upcoming";
                }
                
                List<clsMovie> movieList = movieDAO.getMoviesByFilter(filter);
                request.setAttribute("movieList", movieList);
                request.setAttribute("currentFilter", filter);
                request.getRequestDispatcher("manage_movie.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi tải trang: " + e.getMessage());
            request.getRequestDispatcher("manage_movie.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        try {
            if ("toggleStatus".equals(action)) {
                // UC-16: Toggle enable - disable movie
                int movieId = Integer.parseInt(request.getParameter("movieId"));
                
                clsMovie existingMovie = movieDAO.getMovieById(movieId);
                if (existingMovie != null && existingMovie.isActive()) {
                    if (movieDAO.hasSchedule(movieId)) {
                        request.getSession().setAttribute("error", "Lỗi: Chỉ phim chưa lên lịch mới có thể bị ẩn!");
                        response.sendRedirect(request.getContextPath() + "/MovieController");
                        return;
                    }
                }
                
                boolean isSuccess = movieDAO.toggleMovieStatus(movieId);

                if (isSuccess) {
                    request.getSession().setAttribute("success", "Đã thay đổi trạng thái hiển thị của phim!");
                } else {
                    request.getSession().setAttribute("error", "Lỗi: Không thể thay đổi trạng thái phim.");
                }
                response.sendRedirect(request.getContextPath() + "/MovieController");

            } else if ("add".equals(action) || "edit".equals(action)) {
                // use the same data for add and edit movie
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
                boolean isActive = request.getParameter("isActive") != null; // Có tick là true, không tick là false
                String[] genreIds = request.getParameterValues("genreIds");

                clsMovie movie = new clsMovie(0, movieName, description, duration, releaseDate,
                        poster, trailer, language, subtitle, director,
                        cast, country, ageRestriction, isActive);

                                boolean isSuccess;
                if ("add".equals(action)) {
                    // Dùng hàm mới trả về ID
                    int newId = movieDAO.insertMovieAndGetId(movie);
                    if (newId > 0) {
                        movieDAO.updateMovieGenres(newId, genreIds); // Lưu Thể loại
                        isSuccess = true;
                        request.getSession().setAttribute("success", "Đã thêm phim mới thành công!");
                    } else {
                        isSuccess = false;
                    }
                } else {
                    int movieIdToUpdate = Integer.parseInt(request.getParameter("movieId"));
                    movie.setMovieId(movieIdToUpdate);
                    
                    clsMovie existingMovie = movieDAO.getMovieById(movieIdToUpdate);
                    if (existingMovie != null) {
                        movie.setMovieName(existingMovie.getMovieName());
                        movie.setDuration(existingMovie.getDuration());
                        movie.setPoster(existingMovie.getPoster());
                        movie.setReleaseDate(existingMovie.getReleaseDate());
                    }
                    
                    isSuccess = movieDAO.updateMovie(movie);
                    if (isSuccess) {
                        movieDAO.updateMovieGenres(movie.getMovieId(), genreIds); // Cập nhật Thể loại
                        request.getSession().setAttribute("success", "Đã cập nhật thông tin phim thành công!");
                    }
                }

                if (isSuccess) {
                    response.sendRedirect(request.getContextPath() + "/MovieController");
                } else {
                    request.setAttribute("error", "Lỗi DB: Không thể lưu thông tin phim.");
                    request.setAttribute("movie", movie); // Giữ lại thông tin đang nhập dở
                    String targetJSP = "add".equals(action) ? "add_movie.jsp" : "edit_movie.jsp";
                    request.getRequestDispatcher(targetJSP).forward(request, response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi dữ liệu: " + e.getMessage());
            String targetJSP = "add".equals(action) ? "add_movie.jsp" : "edit_movie.jsp";
            request.getRequestDispatcher(targetJSP).forward(request, response);
        }
    }
}
