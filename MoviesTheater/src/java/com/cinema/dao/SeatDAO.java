package com.cinema.dao;

import com.cinema.model.Seat;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SeatDAO {

    public boolean generateSeats(int roomId, int numberOfRows, int seatsPerRow) {
        String sql = "INSERT INTO Seat(RoomID, RowChar, ColNumber, SeatType, IsActive) VALUES (?, ?, ?, 'Normal', 1)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql)) {
            for (int row = 0; row < numberOfRows; row++) {
                char rowChar = (char) ('A' + row);
                for (int col = 1; col <= seatsPerRow; col++) {
                    stm.setInt(1, roomId);
                    stm.setString(2, String.valueOf(rowChar));
                    stm.setInt(3, col);
                    stm.addBatch();
                }
            }
            stm.executeBatch();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public List<Seat> getSeatsByRoom(int roomId) {
        List<Seat> list = new ArrayList<>();
        String sql = "SELECT * FROM Seat WHERE RoomID = ? ORDER BY RowChar, ColNumber";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, roomId);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    list.add(new Seat(
                            rs.getInt("SeatID"), rs.getInt("RoomID"),
                            rs.getString("RowChar"), rs.getInt("ColNumber"),
                            rs.getString("SeatType"), rs.getBoolean("IsActive")));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public boolean deleteSeatsByRoom(int roomId) {
        String sql = "DELETE FROM Seat WHERE RoomID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, roomId);
            stm.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean updateSeatsByRow(int roomId, String rowChar, String seatType) {
        String sql = "UPDATE Seat SET SeatType = ? WHERE RoomID = ? AND RowChar = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setString(1, seatType);
            stm.setInt(2, roomId);
            stm.setString(3, rowChar);
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
