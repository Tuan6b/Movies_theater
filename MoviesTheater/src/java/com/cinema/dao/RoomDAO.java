package com.cinema.dao;

import com.cinema.model.Room;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public List<Room> getAllRooms() {
        List<Room> list = new ArrayList<>();
        String sql = """
                     SELECT RoomID, RoomNumber, RoomType, Capacity, NumberOfRows, SeatsPerRow, IsActive
                     FROM Room
                     """;
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql);
             ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                list.add(new Room(
                        rs.getInt("RoomID"), rs.getString("RoomNumber"),
                        rs.getString("RoomType"), rs.getInt("Capacity"),
                        rs.getBoolean("IsActive"), rs.getInt("NumberOfRows"),
                        rs.getInt("SeatsPerRow")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public Room getRoomById(int roomId) {
        String sql = "SELECT * FROM Room WHERE RoomID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, roomId);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return new Room(
                            rs.getInt("RoomID"), rs.getString("RoomNumber"),
                            rs.getString("RoomType"), rs.getInt("Capacity"),
                            rs.getBoolean("IsActive"), rs.getInt("NumberOfRows"),
                            rs.getInt("SeatsPerRow"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean addRoom(Room room) {
        String sql = "INSERT INTO Room(RoomNumber, RoomType, Capacity, NumberOfRows, SeatsPerRow, IsActive) VALUES (?, ?, ?, ?, ?, 1)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setString(1, room.getRoomNumber());
            stm.setString(2, room.getRoomType());
            stm.setInt(3, room.getCapacity());
            stm.setInt(4, room.getNumberOfRows());
            stm.setInt(5, room.getSeatsPerRow());
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean updateRoom(Room room) {
        String sql = "UPDATE Room SET RoomNumber = ?, RoomType = ?, Capacity = ?, NumberOfRows = ?, SeatsPerRow = ?, IsActive = ? WHERE RoomID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setString(1, room.getRoomNumber());
            stm.setString(2, room.getRoomType());
            stm.setInt(3, room.getCapacity());
            stm.setInt(4, room.getNumberOfRows());
            stm.setInt(5, room.getSeatsPerRow());
            stm.setBoolean(6, room.isActive());
            stm.setInt(7, room.getRoomId());
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean deleteRoom(int roomId) {
        String sql = "UPDATE Room SET IsActive = 0 WHERE RoomID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, roomId);
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public List<Room> getRoomsByPage(int offset, int noOfRecords) {
        return getRoomsByPage(offset, noOfRecords, null);
    }

    public List<Room> getRoomsByPage(int offset, int noOfRecords, Boolean isActive) {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT RoomID, RoomNumber, RoomType, Capacity, NumberOfRows, SeatsPerRow, IsActive FROM Room"
                + (isActive != null ? " WHERE IsActive = ?" : "")
                + " ORDER BY RoomID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql)) {
            int idx = 1;
            if (isActive != null) stm.setBoolean(idx++, isActive);
            stm.setInt(idx++, offset);
            stm.setInt(idx, noOfRecords);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    list.add(new Room(
                            rs.getInt("RoomID"), rs.getString("RoomNumber"),
                            rs.getString("RoomType"), rs.getInt("Capacity"),
                            rs.getBoolean("IsActive"), rs.getInt("NumberOfRows"),
                            rs.getInt("SeatsPerRow")));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public boolean isRoomNumberExists(String roomNumber) {
        return isRoomNumberExists(roomNumber, -1);
    }

    public boolean isRoomNumberExists(String roomNumber, int excludeRoomId) {
        String sql = "SELECT COUNT(*) FROM Room WHERE RoomNumber = ? AND RoomID != ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setString(1, roomNumber);
            stm.setInt(2, excludeRoomId);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public int getTotalRoomsCount() {
        return getTotalRoomsCount(null);
    }

    public int getTotalRoomsCount(Boolean isActive) {
        String sql = isActive != null ? "SELECT COUNT(*) FROM Room WHERE IsActive = ?" : "SELECT COUNT(*) FROM Room";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql)) {
            if (isActive != null) stm.setBoolean(1, isActive);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public int addRoomAndGetId(Room room) {
        String sql = "INSERT INTO Room(RoomNumber, RoomType, Capacity, NumberOfRows, SeatsPerRow, IsActive) VALUES (?, ?, ?, ?, ?, 1)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stm.setString(1, room.getRoomNumber());
            stm.setString(2, room.getRoomType());
            stm.setInt(3, room.getCapacity());
            stm.setInt(4, room.getNumberOfRows());
            stm.setInt(5, room.getSeatsPerRow());
            int affected = stm.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stm.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }
}
