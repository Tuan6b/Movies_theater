package com.cinema.controller;

import com.cinema.dao.tbMovie;
import com.cinema.model.Account;
import com.cinema.model.clsMovie;
import java.io.IOException;
import java.sql.Date;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class MovieController extends HttpServlet {

    private final tbMovie movieDAO = new tbMovie();

    /**
     * Kiểm tra quyền Manager (roleId >= 4).
     * Trả về true nếu KHÔNG có quyền (cần chặn lại).
     */
    private boolean isNotManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return true;
        }
        Account account = (Account) session.getAttribute("account");
        if (account.getRoleId() < 4) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return true;
        }
        return false;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (isNotManager(request, response)) return;

        String action = request.getParameter("action");

        try {
            // Đẩy các danh mục Enum sang JSP để render các thẻ select (Dropdown)
            request.setAttribute("languages", com.cinema.model.LanguageEnum.values());
            request.setAttribute("subtitles", com.cinema.model.SubtitleEnum.values());
            request.setAttribute("countries", com.cinema.model.CountryEnum.values());
            request.setAttribute("ageRestrictions", com.cinema.model.AgeRestrictionEnum.values());

            if ("add".equals(action)) {
                // Đẩy list thể loại ra JSP để render Checkbox
                request.setAttribute("genreList", new com.cinema.dao.GenreDAO().getAllGenres());
                request.getRequestDispatcher("/view/manager/add_movie.jsp").forward(request, response);

            } else if ("edit".equals(action)) {
                String idStr = request.getParameter("id");
                if (idStr == null || idStr.trim().isEmpty()) {
                    request.setAttribute("error", "Lỗi: ID phim không được để trống!");
                    request.getRequestDispatcher("/view/manager/manage_movie.jsp").forward(request, response);
                    return;
                }
                
                int movieId;
                try {
                    movieId = Integer.parseInt(idStr);
                } catch (NumberFormatException e) {
                    request.setAttribute("error", "Lỗi: ID phim không hợp lệ!");
                    request.getRequestDispatcher("/view/manager/manage_movie.jsp").forward(request, response);
                    return;
                }

                clsMovie movie = movieDAO.getMovieById(movieId);
                if (movie == null) {
                    request.setAttribute("error", "Lỗi: Không tìm thấy phim với ID đã cho!");
                    request.getRequestDispatcher("/view/manager/manage_movie.jsp").forward(request, response);
                    return;
                }
                request.setAttribute("movie", movie);

                // Đẩy list thể loại và các thể loại phim đang có sẵn ra JSP
                request.setAttribute("genreList", new com.cinema.dao.GenreDAO().getAllGenres());
                request.setAttribute("selectedGenres", movieDAO.getGenreIdsByMovie(movieId));
                
                request.getRequestDispatcher("/view/manager/edit_movie.jsp").forward(request, response);
            } else {
                String filter = request.getParameter("filter");
                if (filter == null || filter.isEmpty()) {
                    filter = "upcoming";
                }
                
                List<clsMovie> movieList = movieDAO.getMoviesByFilter(filter);
                request.setAttribute("movieList", movieList);
                request.setAttribute("currentFilter", filter);
                request.setAttribute("movieStats", movieDAO.getMovieStatistics());
                request.getRequestDispatcher("/view/manager/manage_movie.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi tải trang: " + e.getMessage());
            request.getRequestDispatcher("/view/manager/manage_movie.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (isNotManager(request, response)) return;

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        try {
            if ("toggleStatus".equals(action)) {
                // UC-16: Toggle enable - disable movie
                String movieIdStr = request.getParameter("movieId");
                if (movieIdStr == null || movieIdStr.trim().isEmpty()) {
                    request.getSession().setAttribute("error", "Lỗi: ID phim không hợp lệ.");
                    response.sendRedirect(request.getContextPath() + "/MovieController");
                    return;
                }
                
                int movieId;
                try {
                    movieId = Integer.parseInt(movieIdStr);
                } catch (NumberFormatException e) {
                    request.getSession().setAttribute("error", "Lỗi: ID phim không đúng định dạng.");
                    response.sendRedirect(request.getContextPath() + "/MovieController");
                    return;
                }
                
                boolean isSuccess = movieDAO.toggleMovieStatus(movieId);

                if (isSuccess) {
                    request.getSession().setAttribute("success", "Đã thay đổi trạng thái hiển thị của phim!");
                } else {
                    request.getSession().setAttribute("error", "Lỗi: Không thể thay đổi trạng thái phim.");
                }
                response.sendRedirect(request.getContextPath() + "/MovieController");

            } else if ("add".equals(action) || "edit".equals(action)) {
                
                if ("add".equals(action)) {
                    // Logic validation for early release was removed because releaseDate is now handled via schedule.
                }

                // use the same data for add and edit movie
                String movieName = request.getParameter("movieName");
                String budget = request.getParameter("budget");
                String globalBoxOffice = request.getParameter("globalBoxOffice");
                
                int weeklyRevenueRank = 0;
                int ticketsSoldMilestone = 0;
                
                if ("edit".equals(action)) {
                    try {
                        int movieId = Integer.parseInt(request.getParameter("id"));
                        clsMovie oldMovie = movieDAO.getMovieById(movieId);
                        if (oldMovie != null) {
                            weeklyRevenueRank = oldMovie.getWeeklyRevenueRank();
                            ticketsSoldMilestone = oldMovie.getTicketsSoldMilestone();
                        }
                    } catch (Exception e) {
                        System.out.println("Lỗi lấy thông tin phim cũ: " + e.getMessage());
                    }
                }
                int duration = 0;
                int ageRestriction = 0;
                try {
                    duration = Integer.parseInt(request.getParameter("duration"));
                    ageRestriction = Integer.parseInt(request.getParameter("ageRestriction"));
                } catch (NumberFormatException e) {
                    request.setAttribute("error", "Lỗi: Thời lượng hoặc Giới hạn độ tuổi không hợp lệ!");
                    String targetJSP = "add".equals(action) ? "/view/manager/add_movie.jsp" : "/view/manager/edit_movie.jsp";
                    request.getRequestDispatcher(targetJSP).forward(request, response);
                    return;
                }
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

                java.sql.Date releaseDate = null;
                try {
                    String rdParam = request.getParameter("releaseDate");
                    if (rdParam != null && !rdParam.trim().isEmpty()) {
                        releaseDate = java.sql.Date.valueOf(rdParam);
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid release date format: " + request.getParameter("releaseDate"));
                }

                clsMovie movie = new clsMovie(0, movieName, description, duration, null, releaseDate,
                        budget, globalBoxOffice, weeklyRevenueRank, ticketsSoldMilestone,
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
                    int movieIdToUpdate;
                    try {
                        movieIdToUpdate = Integer.parseInt(request.getParameter("movieId"));
                    } catch (NumberFormatException e) {
                        request.setAttribute("error", "Lỗi: ID phim không hợp lệ!");
                        request.getRequestDispatcher("/view/manager/edit_movie.jsp").forward(request, response);
                        return;
                    }
                    movie.setMovieId(movieIdToUpdate);
                    
                    clsMovie existingMovie = movieDAO.getMovieById(movieIdToUpdate);
                    if (existingMovie != null) {
                        movie.setMovieName(existingMovie.getMovieName());
                        movie.setDuration(existingMovie.getDuration());
                        movie.setPoster(existingMovie.getPoster());
                        movie.setDateAdded(existingMovie.getDateAdded());
                        movie.setActive(existingMovie.isActive()); // KHÓA trạng thái hiển thị
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
                    String targetJSP = "add".equals(action) ? "/view/manager/add_movie.jsp" : "/view/manager/edit_movie.jsp";
                    request.getRequestDispatcher(targetJSP).forward(request, response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi dữ liệu: " + e.getMessage());
            String targetJSP = "add".equals(action) ? "/view/manager/add_movie.jsp" : "/view/manager/edit_movie.jsp";
            request.getRequestDispatcher(targetJSP).forward(request, response);
        }
    }
}
