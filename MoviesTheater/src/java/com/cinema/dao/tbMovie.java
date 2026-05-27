/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.dao;

import com.cinema.model.clsMovie;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Movie entity.
 * Follows NetBeans-based tbDAO naming standards.
 *
 * @author TBinh
 */
public class tbMovie {

    /**
     * Maps a ResultSet row to a clsMovie object.
     *
     * @param rs ResultSet containing movie row
     * @return clsMovie object
     * @throws SQLException if mapping fails
     */
    private clsMovie mapRow(ResultSet rs) throws SQLException {
        clsMovie movie = new clsMovie();
        movie.setMovieId(rs.getInt("MovieID"));
        movie.setMovieName(rs.getNString("MovieName"));
        movie.setDescription(rs.getNString("Description"));
        movie.setDuration(rs.getInt("Duration"));
        movie.setReleaseDate(rs.getDate("ReleaseDate"));
        movie.setPoster(rs.getString("Poster"));
        movie.setTrailer(rs.getString("Trailer"));
        movie.setLanguage(rs.getNString("Language"));
        movie.setSubtitle(rs.getNString("Subtitle"));
        movie.setDirector(rs.getNString("Director"));
        movie.setCast(rs.getNString("Cast"));
        movie.setCountry(rs.getNString("Country"));
        movie.setAgeRestriction(rs.getInt("AgeRestriction"));
        movie.setActive(rs.getBoolean("IsActive"));
        return movie;
    }

    /**
     * Retrieve all active movies from database.
     *
     * @return list of active movies
     */
    public List<clsMovie> getAllActiveMovies() {
        List<clsMovie> list = new ArrayList<>();
        String sql = "SELECT * FROM Movie WHERE IsActive = 1";
        
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Find a movie by its unique MovieID.
     *
     * @param movieId ID of the movie
     * @return clsMovie object if found, else null
     */
    public clsMovie getMovieById(int movieId) {
        String sql = "SELECT * FROM Movie WHERE MovieID = ?";
        
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
