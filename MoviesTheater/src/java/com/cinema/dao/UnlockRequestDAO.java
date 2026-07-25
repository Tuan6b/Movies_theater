package com.cinema.dao;

import com.cinema.model.UnlockRequest;
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

public class UnlockRequestDAO {

    public int insert(UnlockRequest req) {
        String sql = "INSERT INTO UnlockRequest (AccountID, Reason, Status) VALUES (?, ?, 'Pending')";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, req.getAccountId());
            ps.setNString(2, req.getReason());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public UnlockRequest getById(int id) {
        String sql = "SELECT * FROM UnlockRequest WHERE RequestID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<UnlockRequest> getByAccountId(int accountId) {
        List<UnlockRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM UnlockRequest WHERE AccountID = ? ORDER BY CreatedAt DESC";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<UnlockRequest> getAll() {
        List<UnlockRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM UnlockRequest ORDER BY CreatedAt DESC";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<UnlockRequest> getPending() {
        List<UnlockRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM UnlockRequest WHERE Status = 'Pending' ORDER BY CreatedAt ASC";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateStatus(int requestId, String status, int reviewedBy) {
        String sql = "UPDATE UnlockRequest SET Status = ?, ReviewedBy = ?, ReviewedAt = GETDATE() WHERE RequestID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reviewedBy);
            ps.setInt(3, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private UnlockRequest map(ResultSet rs) throws SQLException {
        UnlockRequest r = new UnlockRequest();
        r.setRequestId(rs.getInt("RequestID"));
        r.setAccountId(rs.getInt("AccountID"));
        r.setReason(rs.getNString("Reason"));
        r.setStatus(rs.getString("Status"));
        r.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
        Timestamp rt = rs.getTimestamp("ReviewedAt");
        if (rt != null) r.setReviewedAt(rt.toLocalDateTime());
        int rb = rs.getInt("ReviewedBy");
        if (!rs.wasNull()) r.setReviewedBy(rb);
        return r;
    }
}
