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

    public List<Room> getAllRooms() {

        List<Room> list = new ArrayList<>();

        String sql = """
                     SELECT RoomID,
                            RoomNumber,
                            RoomType,
                            Capacity,
                            IsActive
                     FROM Room
                     """;

        try (PreparedStatement stm
                = connection.prepareStatement(sql); ResultSet rs = stm.executeQuery()) {

            while (rs.next()) {

                Room room = new Room(
                        rs.getInt("RoomID"),
                        rs.getString("RoomNumber"),
                        rs.getString("RoomType"),
                        rs.getInt("Capacity"),
                        rs.getBoolean("IsActive")
                );

                list.add(room);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    public Room getRoomById(int roomId) {

        String sql = """
                     SELECT *
                     FROM Room
                     WHERE RoomID = ?
                     """;

        try (PreparedStatement stm
                = connection.prepareStatement(sql)) {

            stm.setInt(1, roomId);

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

    public boolean addRoom(Room room) {

        String sql = """
                     INSERT INTO Room(
                        RoomNumber,
                        RoomType,
                        Capacity,
                        IsActive
                     )
                     VALUES (?, ?, ?, 1)
                     """;

        try (PreparedStatement stm
                = connection.prepareStatement(sql)) {

            stm.setString(1, room.getRoomNumber());
            stm.setString(2, room.getRoomType());
            stm.setInt(3, room.getCapacity());

            return stm.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean updateRoom(Room room) {

        String sql = """
                     UPDATE Room
                     SET RoomNumber = ?,
                         RoomType = ?,
                         Capacity = ?,
                         IsActive = ?
                     WHERE RoomID = ?
                     """;

        try (PreparedStatement stm
                = connection.prepareStatement(sql)) {

            stm.setString(1, room.getRoomNumber());
            stm.setString(2, room.getRoomType());
            stm.setInt(3, room.getCapacity());
            stm.setBoolean(4, room.isActive());
            stm.setInt(5, room.getRoomId());

            return stm.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public boolean deleteRoom(int roomId) {

        String sql = """
                     UPDATE Room
                     SET IsActive = 0
                     WHERE RoomID = ?
                     """;

        try (PreparedStatement stm
                = connection.prepareStatement(sql)) {

            stm.setInt(1, roomId);

            return stm.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }
}
