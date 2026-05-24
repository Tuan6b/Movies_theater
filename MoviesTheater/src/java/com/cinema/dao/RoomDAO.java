/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.dao;

import com.cinema.model.Room;
import com.cinema.util.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tuan Phong Nguyen
 */
/**
 * Data Access Object (DAO) for managing Room entity persistence operations.
 * Handles database interactions using JDBC.
 */
public class RoomDAO extends DBContext {

    /**
     * Retrieves all cinema rooms recorded in the database.
     *
     * * @return a list of Room objects, or an empty list if none found
     */
    public List<Room> getAllRooms() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT RoomID, RoomNumber, RoomType, Capacity, IsActive FROM Room";

        try (PreparedStatement stm = connection.prepareStatement(sql); ResultSet rs = stm.executeQuery()) {

            while (rs.next()) {
                list.add(new Room(
                        rs.getInt("RoomID"),
                        rs.getString("RoomNumber"),
                        rs.getString("RoomType"),
                        rs.getInt("Capacity"),
                        rs.getBoolean("IsActive")
                ));
            }
        } catch (SQLException ex) {
            // Logs SQL errors to standard error stream for debugging purposes
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Fetches details of a specific cinema room by its unique ID.
     *
     * * @param id the unique Room ID
     * @return the matched Room object, or null if no match is found
     */
    public Room getRoomById(int id) {
        String sql = "SELECT RoomID, RoomNumber, RoomType, Capacity, IsActive FROM Room WHERE RoomID = ?";

        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setInt(1, id);

            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return new Room(
                            rs.getInt("RoomID"),
                            rs.getString("RoomNumber"),
                            rs.getString("RoomType"),
                            rs.getInt("Capacity"),
                            rs.getBoolean("IsActive")
                    );
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * UC32: Inserts a new cinema room record into the system. By default, newly
     * added rooms are set to active (IsActive = 1).
     *
     * * @param room the transient Room object containing registration details
     * @return true if insertion succeeds, false otherwise
     */
    public boolean insertRoom(Room room) {
        String sql = "INSERT INTO Room (RoomNumber, RoomType, Capacity, IsActive) VALUES (?, ?, ?, 1)";

        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setString(1, room.getRoomNumber());
            stm.setString(2, room.getRoomType());
            stm.setInt(3, room.getCapacity());

            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * UC33: Updates the fields of an existing cinema room record.
     *
     * * @param room the Room object containing modified data and identifying
     * ID
     * @return true if the update affects at least one row, false otherwise
     */
    public boolean updateRoom(Room room) {
        String sql = "UPDATE Room SET RoomNumber = ?, RoomType = ?, Capacity = ?, IsActive = ? WHERE RoomID = ?";

        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setString(1, room.getRoomNumber());
            stm.setString(2, room.getRoomType());
            stm.setInt(3, room.getCapacity());
            stm.setBoolean(4, room.isIsActive());
            stm.setInt(5, room.getRoomId());

            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * UC34: Deactivates a cinema room instead of removing it permanently (Soft
     * Delete). This preserves historical invoice records linked via foreign
     * keys.
     *
     * * @param id the unique ID of the room to be disabled
     * @return true if deactivated successfully, false otherwise
     */
    public boolean softDeleteRoom(int id) {
        String sql = "UPDATE Room SET IsActive = 0 WHERE RoomID = ?";

        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setInt(1, id);
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
