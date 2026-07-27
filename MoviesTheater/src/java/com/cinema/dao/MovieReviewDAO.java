package com.cinema.dao;

import com.cinema.model.clsMovieReview;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MovieReviewDAO {

    public List<clsMovieReview> getReviewsByMovieId(int movieId) {
        List<clsMovieReview> list = new ArrayList<>();
        String sql = "SELECT r.*, u.FullName, u.AvatarURL "
                + "FROM MovieReview r "
                + "LEFT JOIN UserProfile u ON r.AccountID = u.AccountID "
                + "WHERE r.MovieID = ? "
                + "ORDER BY r.CreatedAt DESC";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clsMovieReview review = new clsMovieReview();
                    review.setReviewId(rs.getInt("ReviewID"));
                    review.setMovieId(rs.getInt("MovieID"));
                    review.setAccountId(rs.getInt("AccountID"));
                    review.setTicketId(rs.getInt("TicketID"));
                    review.setRatingValue(rs.getInt("RatingValue"));
                    review.setComment(rs.getString("Comment"));
                    review.setCreatedAt(rs.getTimestamp("CreatedAt"));

                    review.setReviewerName(rs.getString("FullName"));
                    review.setAvatarUrl(rs.getString("AvatarURL"));
                    list.add(review);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getCheckedInTicketId(int accountId, int movieId) {
        String sql = "SELECT TOP 1 t.TicketID FROM Ticket t "
                + "JOIN Invoice i ON t.InvoiceID = i.InvoiceID "
                + "JOIN Schedule s ON t.ScheduleID = s.ScheduleID "
                + "LEFT JOIN MovieReview mr ON t.TicketID = mr.TicketID "
                + "WHERE i.AccountID = ? AND s.MovieID = ? AND t.IsCheckedIn = 1 "
                + "AND mr.ReviewID IS NULL";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("TicketID");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public clsMovieReview getReviewByAccountAndMovie(int accountId, int movieId) {
        String sql = "SELECT * FROM MovieReview WHERE AccountID = ? AND MovieID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    clsMovieReview review = new clsMovieReview();
                    review.setReviewId(rs.getInt("ReviewID"));
                    review.setMovieId(rs.getInt("MovieID"));
                    review.setAccountId(rs.getInt("AccountID"));
                    review.setTicketId(rs.getInt("TicketID"));
                    review.setRatingValue(rs.getInt("RatingValue"));
                    review.setComment(rs.getString("Comment"));
                    review.setCreatedAt(rs.getTimestamp("CreatedAt"));
                    return review;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addReview(clsMovieReview review) {
        String sql = "INSERT INTO MovieReview (MovieID, AccountID, TicketID, RatingValue, Comment, CreatedAt) VALUES (?, ?, ?, ?, ?, GETDATE())";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, review.getMovieId());
            ps.setInt(2, review.getAccountId());
            ps.setInt(3, review.getTicketId());
            ps.setInt(4, review.getRatingValue());
            ps.setString(5, review.getComment());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateReview(int reviewId, int accountId, int ratingValue, String comment) {
        String sql = "UPDATE MovieReview SET RatingValue = ?, Comment = ? "
                + "WHERE ReviewID = ? AND AccountID = ? "
                + "AND GETDATE() <= (SELECT MAX(EndTime) FROM Schedule WHERE MovieID = MovieReview.MovieID)";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ratingValue);
            ps.setString(2, comment);
            ps.setInt(3, reviewId);
            ps.setInt(4, accountId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean deleteReview(int reviewId, int accountId) {
        String sql = "DELETE FROM MovieReview WHERE ReviewID = ? AND AccountID = ? "
                + "AND GETDATE() <= (SELECT MAX(EndTime) FROM Schedule WHERE MovieID = MovieReview.MovieID)";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
