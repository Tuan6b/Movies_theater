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
        String sql = "SELECT r.*, u.FullName, u.AvatarURL " +
                     "FROM MovieReview r " +
                     "JOIN UserProfile u ON r.AccountID = u.AccountID " +
                     "WHERE r.MovieID = ? " +
                     "ORDER BY r.CreatedAt DESC";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
}