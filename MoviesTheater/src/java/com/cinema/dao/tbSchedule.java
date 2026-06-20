/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.dao;

import com.cinema.model.Room;
import com.cinema.model.clsMovie;
import com.cinema.model.clsSchedule;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Schedule/Showtime entity.
 * Follows NetBeans-based tbDAO naming standards.
 *
 * @author TBinh
 */
public class tbSchedule {

    /**
     * Maps a ResultSet row to a clsSchedule object with nested Room and Movie.
     */
    private clsSchedule mapRow(ResultSet rs) throws SQLException {
        clsSchedule schedule = new clsSchedule();
        schedule.setScheduleId(rs.getInt("ScheduleID"));
        schedule.setRoomId(rs.getInt("RoomID"));
        schedule.setMovieId(rs.getInt("MovieID"));
        schedule.setStartTime(rs.getTimestamp("StartTime"));
        schedule.setEndTime(rs.getTimestamp("EndTime"));
        schedule.setBaseTicketPrice(rs.getDouble("BaseTicketPrice"));
        schedule.setStatus(rs.getString("Status"));

        // Map room details if present in the projection
        try {
            Room room = new Room();
            room.setRoomId(rs.getInt("RoomID"));
            room.setRoomNumber(rs.getNString("RoomNumber"));
            room.setRoomType(rs.getString("RoomType"));
            room.setCapacity(rs.getInt("Capacity"));
            room.setActive(rs.getBoolean("RoomActive"));
            schedule.setRoom(room);
        } catch (SQLException ex) {
            // Ignore if Room columns are not present in result set
        }

        // Map movie details if present in the projection
        try {
            clsMovie movie = new clsMovie();
            movie.setMovieId(rs.getInt("MovieID"));
            movie.setMovieName(rs.getNString("MovieName"));
            movie.setDuration(rs.getInt("Duration"));
            movie.setPoster(rs.getString("Poster"));
            schedule.setMovie(movie);
        } catch (SQLException ex) {
            // Ignore if Movie columns are not present in result set
        }

        return schedule;
    }

    /**
     * Retrieve all available show dates for a specific movie.
     * Filter by 'Scheduled' status and only retrieve dates today or in the future for guests.
     *
     * @param movieId ID of the movie
     * @return list of dates as SQL Date objects
     */
    public List<Date> getAvailableDatesForMovie(int movieId) {
        List<Date> list = new ArrayList<>();
        String sql = """
                     SELECT DISTINCT CAST(StartTime AS DATE) AS ShowDate
                     FROM Schedule
                     WHERE MovieID = ? AND Status = 'Scheduled'
                     ORDER BY ShowDate ASC
                     """;
        
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getDate("ShowDate"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Retrieve schedules for a specific movie and date, ordered by start time.
     *
     * @param movieId ID of the movie
     * @param date date string in YYYY-MM-DD format
     * @return list of schedules
     */
    public List<clsSchedule> getSchedulesByMovieAndDate(int movieId, String date) {
        List<clsSchedule> list = new ArrayList<>();
        String sql = """
                     SELECT s.*, 
                            r.RoomNumber, r.RoomType, r.Capacity, r.IsActive AS RoomActive,
                            m.MovieName, m.Duration, m.Poster
                     FROM Schedule s
                     INNER JOIN Room r ON s.RoomID = r.RoomID
                     INNER JOIN Movie m ON s.MovieID = m.MovieID
                     WHERE s.MovieID = ? 
                       AND CAST(s.StartTime AS DATE) = ? 
                       AND s.Status = 'Scheduled'
                     ORDER BY s.StartTime ASC
                     """;
        
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, movieId);
            ps.setString(2, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Retrieve a schedule by its unique ScheduleID along with Room and Movie data.
     *
     * @param scheduleId ID of schedule
     * @return clsSchedule object or null if not found
     */
    public clsSchedule getScheduleById(int scheduleId) {
        String sql = """
                     SELECT s.*, 
                            r.RoomNumber, r.RoomType, r.Capacity, r.IsActive AS RoomActive,
                            m.MovieName, m.Duration, m.Poster
                     FROM Schedule s
                     INNER JOIN Room r ON s.RoomID = r.RoomID
                     INNER JOIN Movie m ON s.MovieID = m.MovieID
                     WHERE s.ScheduleID = ?
                     """;
        
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, scheduleId);
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
     * Retrieve all schedules filtered by date, movie and room.
     */
    public List<clsSchedule> getSchedules(String date, Integer movieId, Integer roomId) {
        List<clsSchedule> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                     SELECT s.*, 
                            r.RoomNumber, r.RoomType, r.Capacity, r.IsActive AS RoomActive,
                            m.MovieName, m.Duration, m.Poster
                     FROM Schedule s
                     INNER JOIN Room r ON s.RoomID = r.RoomID
                     INNER JOIN Movie m ON s.MovieID = m.MovieID
                     WHERE 1=1
                     """);
        List<Object> params = new ArrayList<>();

        if (date != null && !date.trim().isEmpty()) {
            sql.append(" AND CAST(s.StartTime AS DATE) = ?");
            params.add(date);
        }
        if (movieId != null && movieId > 0) {
            sql.append(" AND s.MovieID = ?");
            params.add(movieId);
        }
        if (roomId != null && roomId > 0) {
            sql.append(" AND s.RoomID = ?");
            params.add(roomId);
        }

        sql.append(" ORDER BY s.StartTime ASC");

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }
}
