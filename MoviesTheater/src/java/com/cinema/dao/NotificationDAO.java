/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.dao;

import com.cinema.model.Notification;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for handling Notification entities.
 *
 * @author tuan6b
 */
public class NotificationDAO {

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("NotificationID"));
        n.setAccountId(rs.getInt("AccountID"));
        n.setType(rs.getString("Type"));
        n.setTitle(rs.getNString("Title"));
        n.setMessage(rs.getNString("Message"));
        int referenceId = rs.getInt("ReferenceID");
        n.setReferenceId(rs.wasNull() ? null : referenceId);
        n.setRead(rs.getBoolean("IsRead"));
        Timestamp createdTs = rs.getTimestamp("CreatedAt");
        if (createdTs != null) n.setCreatedAt(createdTs.toLocalDateTime());
        return n;
    }

    /**
     * Insert a new notification and return the generated ID, or -1 on failure.
     */
    public int insert(Notification n) {
        String sql = "INSERT INTO Notification (AccountID, Type, Title, Message, ReferenceID) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n.getAccountId());
            ps.setString(2, n.getType());
            ps.setNString(3, n.getTitle());
            ps.setNString(4, n.getMessage());
            if (n.getReferenceId() != null) {
                ps.setInt(5, n.getReferenceId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Fetch the most recent notifications for an account, newest first.
     */
    public List<Notification> getRecentByAccount(int accountId, int limit) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT TOP (?) * FROM Notification "
                + "WHERE AccountID = ? ORDER BY CreatedAt DESC";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Count unread notifications for an account (used for the bell badge).
     */
    public int countUnread(int accountId) {
        String sql = "SELECT COUNT(*) FROM Notification WHERE AccountID = ? AND IsRead = 0";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Mark a single notification as read. Scoped to accountId so a user cannot
     * mark another account's notification as read.
     */
    public boolean markAsRead(int notificationId, int accountId) {
        String sql = "UPDATE Notification SET IsRead = 1 WHERE NotificationID = ? AND AccountID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mark all unread notifications for an account as read. Returns the number updated.
     */
    public int markAllAsRead(int accountId) {
        String sql = "UPDATE Notification SET IsRead = 1 WHERE AccountID = ? AND IsRead = 0";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
