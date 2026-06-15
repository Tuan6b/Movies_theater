package com.cinema.dao;

import com.cinema.model.BookingScheduleView;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BookingScheduleDAO {
    
    public BookingScheduleView getScheduleById(int scheduleId) {
        String sql = "SELECT s.ScheduleID, s.MovieID, m.MovieName, s.RoomID, r.RoomNumber, r.RoomType, r.Capacity, " +
                     "CAST(s.StartTime AS DATE) as ShowDate, CONVERT(varchar(5), s.StartTime, 108) as StartTime, s.BaseTicketPrice " +
                     "FROM Schedule s " +
                     "JOIN Movie m ON s.MovieID = m.MovieID " +
                     "JOIN Room r ON s.RoomID = r.RoomID " +
                     "WHERE s.ScheduleID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, scheduleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BookingScheduleView view = new BookingScheduleView();
                    view.setScheduleId(rs.getInt("ScheduleID"));
                    view.setMovieId(rs.getInt("MovieID"));
                    view.setMovieName(rs.getString("MovieName"));
                    view.setRoomId(rs.getInt("RoomID"));
                    view.setRoomNumber(rs.getString("RoomNumber"));
                    view.setRoomType(rs.getString("RoomType"));
                    view.setCapacity(rs.getInt("Capacity"));
                    view.setShowDate(rs.getString("ShowDate"));
                    view.setStartTime(rs.getString("StartTime"));
                    view.setBaseTicketPrice(rs.getDouble("BaseTicketPrice"));
                    return view;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
