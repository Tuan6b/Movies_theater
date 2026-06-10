/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.dao;

import com.cinema.model.Seat;
import com.cinema.util.DBContext;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * SeatDAO handles all database operations related to Seat entity.
 *
 * @author Tuan Phong Nguyen
 */
public class SeatDAO extends DBContext {

    /**
     * Automatically generate seats for a room
     *
     * Example: A1 A2 A3 B1 B2 B3
     *
     * @param roomId room ID
     * @param numberOfRows total row count
     * @param seatsPerRow seats per row
     * @return true if successful
     */
    public boolean generateSeats(int roomId,
            int numberOfRows,
            int seatsPerRow) {

        String sql = """
                     INSERT INTO Seat(
                         RoomID,
                         RowChar,
                         ColNumber,
                         SeatType,
                         IsActive
                     )
                     VALUES (?, ?, ?, 'Normal', 1)
                     """;

        try (PreparedStatement stm
                = connection.prepareStatement(sql)) {

            /*
             * Generate rows:
             * 0 -> A
             * 1 -> B
             * 2 -> C
             */
            for (int row = 0; row < numberOfRows; row++) {

                // Convert row index to alphabet letter
                char rowChar = (char) ('A' + row);

                /*
                 * Generate seat numbers
                 * Example:
                 * A1 A2 A3...
                 */
                for (int col = 1; col <= seatsPerRow; col++) {

                    stm.setInt(1, roomId);
                    stm.setString(2,
                            String.valueOf(rowChar));
                    stm.setInt(3, col);

                    stm.addBatch();
                }
            }

            // Execute all insert statements together
            stm.executeBatch();

            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    /**
     * Get all seats of a specific room
     *
     * @param roomId room ID
     * @return list of seats
     */
    public List<Seat> getSeatsByRoom(int roomId) {

        List<Seat> list = new ArrayList<>();

        String sql = """
                 SELECT *
                 FROM Seat
                 WHERE RoomID = ?
                 ORDER BY RowChar, ColNumber
                 """;

        try (PreparedStatement stm
                = connection.prepareStatement(sql)) {

            stm.setInt(1, roomId);

            try (ResultSet rs = stm.executeQuery()) {

                while (rs.next()) {

                    Seat seat = new Seat(
                            rs.getInt("SeatID"),
                            rs.getInt("RoomID"),
                            rs.getString("RowChar"),
                            rs.getInt("ColNumber"),
                            rs.getString("SeatType"),
                            rs.getBoolean("IsActive")
                    );

                    list.add(seat);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    /**
     * Update seat information
     *
     * @param seatId seat ID
     * @param seatType seat type
     * @param active seat status
     * @return true if successful
     */
    public boolean updateSeat(int seatId,
            String seatType,
            boolean active) {

        String sql = """
                 UPDATE Seat
                 SET SeatType = ?,
                     IsActive = ?
                 WHERE SeatID = ?
                 """;

        try (PreparedStatement stm
                = connection.prepareStatement(sql)) {

            // Set seat type
            stm.setString(1, seatType);

            // Set seat status
            stm.setBoolean(2, active);

            // Set seat ID
            stm.setInt(3, seatId);

            return stm.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

}
