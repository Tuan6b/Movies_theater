package com.cinema.dao;

import com.cinema.model.Room;
import com.cinema.util.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * RoomDAO handles all database operations related to Room entity.
 * This class follows DAO pattern and uses JDBC for SQL execution.
 * Extends DBContext to reuse database connection.
 * @author Tuan Phong Nguyen
 */
public class RoomDAO extends DBContext {

    /**
     * Retrieve all rooms from database (including inactive ones if not filtered in SQL)
     *
     * @return list of Room objects
     */
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

        try (PreparedStatement stm = connection.prepareStatement(sql);
             ResultSet rs = stm.executeQuery()) {

            // Loop through result set and map each row to Room object
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

    /**
     * Find a room by its ID
     *
     * @param roomId ID of the room
     * @return Room object if found, otherwise null
     */
    public Room getRoomById(int roomId) {

        String sql = """
                     SELECT *
                     FROM Room
                     WHERE RoomID = ?
                     """;

        try (PreparedStatement stm = connection.prepareStatement(sql)) {

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

    /**
     * Insert a new room into database
     * Note: IsActive is default = 1 (active)
     *
     * @param room Room object containing data
     * @return true if insert successful
     */
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
     * Update existing room information
     *
     * @param room Room object with updated data
     * @return true if update successful
     */
    public boolean updateRoom(Room room) {

        String sql = """
                     UPDATE Room
                     SET RoomNumber = ?,
                         RoomType = ?,
                         Capacity = ?,
                         IsActive = ?
                     WHERE RoomID = ?
                     """;

        try (PreparedStatement stm = connection.prepareStatement(sql)) {

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

    /**
     * Soft delete room (set IsActive = 0 instead of deleting record)
     *
     * @param roomId ID of room to deactivate
     * @return true if update successful
     */
    public boolean deleteRoom(int roomId) {

        String sql = """
                     UPDATE Room
                     SET IsActive = 0
                     WHERE RoomID = ?
                     """;

        try (PreparedStatement stm = connection.prepareStatement(sql)) {

            stm.setInt(1, roomId);

            return stm.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }
    
    /**
     * Fetch a subset of rooms from the database based on offset and limit
     * @param offset the starting index of the records
     * @param noOfRecords the number of records to return
     * @return list of Room objects for the current page
     */
    public List<Room> getRoomsByPage(int offset, int noOfRecords) {
        List<Room> list = new ArrayList<>();
        
        // SQL Syntax for SQL Server (Requires ORDER BY to use OFFSET)
        String sql = """
                     SELECT RoomID, RoomNumber, RoomType, Capacity, IsActive
                     FROM Room
                     ORDER BY RoomID
                     OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                     """;

        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setInt(1, offset);
            stm.setInt(2, noOfRecords);

            try (ResultSet rs = stm.executeQuery()) {
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
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Count the total number of room records in the database
     * @return total row count of Room table
     */
    public int getTotalRoomsCount() {
        String sql = "SELECT COUNT(*) FROM Room";
        try (PreparedStatement stm = connection.prepareStatement(sql);
             ResultSet rs = stm.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }
}