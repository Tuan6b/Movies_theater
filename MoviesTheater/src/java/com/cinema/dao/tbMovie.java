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
        movie.setMovieName(rs.getString("MovieName"));
        movie.setDescription(rs.getString("Description"));
        movie.setDuration(rs.getInt("Duration"));
        movie.setReleaseDate(rs.getDate("ReleaseDate"));
        movie.setPoster(rs.getString("Poster"));
        movie.setTrailer(rs.getString("Trailer"));
        movie.setLanguage(rs.getString("Language"));
        movie.setSubtitle(rs.getString("Subtitle"));
        movie.setDirector(rs.getString("Director"));
        movie.setCast(rs.getString("Cast"));
        movie.setCountry(rs.getString("Country"));
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
    
    /**
     * Insert a new movie into the database
     * 
     * @param movie clsMovie object containing new movie data
     * @return boolean true if success, false if failed
     */
    public boolean insertMovie(clsMovie movie) {
        String sql = "INSERT INTO Movie (MovieName, Description, Duration, ReleaseDate, Poster, Trailer, "
                + "Language, Subtitle, Director, Cast, Country, AgeRestriction, IsActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBUtils.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            
            ps.setNString(1, movie.getMovieName());
            ps.setNString(2, movie.getDescription());
            ps.setInt(3, movie.getDuration());
            ps.setDate(4, movie.getReleaseDate());
            ps.setString(5, movie.getPoster());
            ps.setString(6, movie.getTrailer());
            ps.setNString(7, movie.getLanguage());
            ps.setNString(8, movie.getSubtitle());
            ps.setNString(9, movie.getDirector());
            ps.setNString(10, movie.getCast());
            ps.setNString(11, movie.getCountry());
            ps.setInt(12, movie.getAgeRestriction());
            ps.setBoolean(13, movie.isActive());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<clsMovie> getAllMoviesAdmin() {
        List<clsMovie> list = new ArrayList<>();
        String sql = "SELECT * FROM Movie ORDER BY MovieID DESC";
        
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
}
