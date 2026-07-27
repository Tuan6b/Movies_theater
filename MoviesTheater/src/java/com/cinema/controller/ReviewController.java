package com.cinema.controller;

import com.cinema.dao.MovieReviewDAO;
import com.cinema.model.Account;
import com.cinema.model.clsMovieReview;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ReviewController", urlPatterns = {"/ReviewController"})
public class ReviewController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("account");
        
        String action = request.getParameter("action");
        String movieIdStr = request.getParameter("movieId");
        
        if (account == null || movieIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }

        int movieId;
        try {
            movieId = Integer.parseInt(movieIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/HomeController");
            return;
        }

        int accountId = account.getAccountId();
        MovieReviewDAO dao = new MovieReviewDAO();

        try {
            if ("add".equals(action)) {
                int ticketId = dao.getCheckedInTicketId(accountId, movieId);
                if (ticketId != -1) {
                    int rating = Integer.parseInt(request.getParameter("rating"));
                    String comment = request.getParameter("comment");
                    clsMovieReview review = new clsMovieReview();
                    review.setMovieId(movieId);
                    review.setAccountId(accountId);
                    review.setTicketId(ticketId);
                    review.setRatingValue(rating);
                    review.setComment(comment);
                    dao.addReview(review);
                }
            } else if ("update".equals(action)) {
                int reviewId = Integer.parseInt(request.getParameter("reviewId"));
                int rating = Integer.parseInt(request.getParameter("rating"));
                String comment = request.getParameter("comment");
                boolean success = dao.updateReview(reviewId, accountId, rating, comment);
                if (!success) {
                    session.setAttribute("flashError", "Hết hạn sửa! Phim đã ngừng chiếu.");
                }
            } else if ("delete".equals(action)) {
                int reviewId = Integer.parseInt(request.getParameter("reviewId"));
                boolean success = dao.deleteReview(reviewId, accountId);
                if (!success) {
                    session.setAttribute("flashError", "Hết hạn xóa! Phim đã ngừng chiếu.");
                }
            }
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Dữ liệu không hợp lệ!");
        }

        response.sendRedirect("MovieDetailController?id=" + movieId);
    }
}