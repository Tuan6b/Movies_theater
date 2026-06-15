package com.cinema.dao;

import com.cinema.model.Notification;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public int createNotification(String type, String message, String link) {
        String sql = "INSERT INTO Notification (Type, Message, Link) VALUES (?, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, type);
            ps.setNString(2, message);
            ps.setString(3, link);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Notification> getUnreadNotifications(int limit) {
        String sql = "SELECT TOP (?) NotificationID, Type, Message, Link, IsRead, CreatedAt "
                    + "FROM Notification WHERE IsRead = 0 ORDER BY CreatedAt DESC";
        List<Notification> list = new ArrayList<>();
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapNotification(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int countUnread() {
        String sql = "SELECT COUNT(*) FROM Notification WHERE IsRead = 0";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE Notification SET IsRead = 1 WHERE NotificationID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean markAllAsRead() {
        String sql = "UPDATE Notification SET IsRead = 1 WHERE IsRead = 0";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Notification mapNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("NotificationID"));
        n.setType(rs.getString("Type"));
        n.setMessage(rs.getNString("Message"));
        n.setLink(rs.getString("Link"));
        n.setIsRead(rs.getBoolean("IsRead"));
        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) n.setCreatedAt(ts.toLocalDateTime());
        return n;
    }
}
