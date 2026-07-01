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
import jakarta.servlet.http.HttpSession;

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
            
            // Kiểm tra trạng thái kích hoạt (Active) của phim
            if (movie == null || !movie.isIsActive()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Phim không tồn tại hoặc đã ngừng chiếu.");
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

            //Rating
            com.cinema.dao.MovieReviewDAO reviewDAO = new com.cinema.dao.MovieReviewDAO();
            List<com.cinema.model.clsMovieReview> reviews = reviewDAO.getReviewsByMovieId(movieId);

            int totalReviews = reviews.size();
            double avgRating = 0;
            int[] starCounts = new int[6];

            if (totalReviews > 0) {
                double sum = 0;
                for (com.cinema.model.clsMovieReview r : reviews) {
                    sum += r.getRatingValue();
                    if (r.getRatingValue() >= 1 && r.getRatingValue() <= 5) {
                        starCounts[r.getRatingValue()]++;
                    }
                }
                avgRating = Math.round((sum / totalReviews) * 10.0) / 10.0;
            }

            request.setAttribute("reviews", reviews);
            request.setAttribute("totalReviews", totalReviews);
            request.setAttribute("avgRating", avgRating);
            request.setAttribute("starCounts", starCounts);

            // Kiểm tra quyền đánh giá của User hiện tại (UC19 & UC20)
            HttpSession session = request.getSession();
            com.cinema.model.Account account = (com.cinema.model.Account) session.getAttribute("account");
            if (account != null) {
                boolean canReview = (reviewDAO.getCheckedInTicketId(account.getAccountId(), movieId) != -1);
                request.setAttribute("canReview", canReview);
                
                // Kéo review của user (nếu đã từng đánh giá)
                com.cinema.model.clsMovieReview userReview = reviewDAO.getReviewByAccountAndMovie(account.getAccountId(), movieId);
                request.setAttribute("userReview", userReview);
            }

            request.getRequestDispatcher("/movie-detail.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/HomeController");
        }
    }
}

