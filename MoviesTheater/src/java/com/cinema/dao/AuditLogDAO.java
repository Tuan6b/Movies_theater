package com.cinema.dao;

import com.cinema.model.AuditLog;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    public void log(Integer accountId, String actionType, String description, String ipAddress) {
        String sql = "INSERT INTO SystemLog (AccountID, ActionType, Description, IPAddress) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (accountId != null) ps.setInt(1, accountId);
            else ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(2, actionType);
            ps.setNString(3, description);
            ps.setString(4, ipAddress);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<AuditLog> getLogs(int page, int pageSize, String search) {
        StringBuilder sql = new StringBuilder(
            "SELECT l.LogID, l.AccountID, l.ActionType, l.Description, l.IPAddress, l.CreatedAt, "
            + "a.Email AS AccountEmail, u.FullName "
            + "FROM SystemLog l "
            + "LEFT JOIN Account a ON l.AccountID = a.AccountID "
            + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
            + "WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (l.ActionType LIKE ? OR l.Description LIKE ? OR a.Email LIKE ? OR u.FullName LIKE ?)");
            String like = "%" + search.trim() + "%";
            params.add(like); params.add(like); params.add(like); params.add(like);
        }

        sql.append(" ORDER BY l.CreatedAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add((page - 1) * pageSize);
        params.add(pageSize);

        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapLog(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countLogs(String search) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM SystemLog l "
            + "LEFT JOIN Account a ON l.AccountID = a.AccountID "
            + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (l.ActionType LIKE ? OR l.Description LIKE ? OR a.Email LIKE ? OR u.FullName LIKE ?)");
            String like = "%" + search.trim() + "%";
            params.add(like); params.add(like); params.add(like); params.add(like);
        }
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<AuditLog> getRecentLogs(int limit) {
        String sql = "SELECT TOP (?) l.LogID, l.AccountID, l.ActionType, l.Description, l.IPAddress, l.CreatedAt, "
                    + "a.Email AS AccountEmail, u.FullName FROM SystemLog l "
                    + "LEFT JOIN Account a ON l.AccountID = a.AccountID "
                    + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                    + "ORDER BY l.CreatedAt DESC";
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapLog(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private AuditLog mapLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setLogId(rs.getInt("LogID"));
        int aid = rs.getInt("AccountID");
        log.setAccountId(rs.wasNull() ? null : aid);
        log.setActionType(rs.getString("ActionType"));
        log.setDescription(rs.getNString("Description"));
        log.setIpAddress(rs.getString("IPAddress"));
        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) log.setCreatedAt(ts.toLocalDateTime());
        log.setAccountEmail(rs.getString("AccountEmail"));
        log.setFullName(rs.getNString("FullName"));
        return log;
    }
}
