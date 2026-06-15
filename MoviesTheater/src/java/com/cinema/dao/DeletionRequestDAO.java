package com.cinema.dao;

import com.cinema.model.DeletionRequest;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DeletionRequestDAO {

    public int createRequest(int accountId, String reason) {
        String sql = "INSERT INTO DeletionRequest (AccountID, Reason) VALUES (?, ?)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, accountId);
            ps.setNString(2, reason);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean hasPendingRequest(int accountId) {
        String sql = "SELECT COUNT(*) FROM DeletionRequest WHERE AccountID = ? AND Status = 'Pending'";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<DeletionRequest> getRequests(int page, int pageSize, String statusFilter) {
        StringBuilder sql = new StringBuilder(
            "SELECT r.RequestID, r.AccountID, r.Reason, r.Status, r.ReviewedBy, r.ReviewNote, r.CreatedAt, r.ReviewedAt, "
            + "a.Email AS AccountEmail, u.FullName FROM DeletionRequest r "
            + "JOIN Account a ON r.AccountID = a.AccountID "
            + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (statusFilter != null && !statusFilter.isEmpty() && !"all".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND r.Status = ?");
            params.add(statusFilter);
        }

        sql.append(" ORDER BY r.CreatedAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add((page - 1) * pageSize);
        params.add(pageSize);

        List<DeletionRequest> list = new ArrayList<>();
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRequest(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int countRequests(String statusFilter) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM DeletionRequest r WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (statusFilter != null && !statusFilter.isEmpty() && !"all".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND r.Status = ?");
            params.add(statusFilter);
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

    public DeletionRequest getRequestById(int requestId) {
        String sql = "SELECT r.RequestID, r.AccountID, r.Reason, r.Status, r.ReviewedBy, r.ReviewNote, r.CreatedAt, r.ReviewedAt, "
                    + "a.Email AS AccountEmail, u.FullName FROM DeletionRequest r "
                    + "JOIN Account a ON r.AccountID = a.AccountID "
                    + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID WHERE r.RequestID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRequest(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean approveRequest(int requestId, int reviewedBy, String reviewNote) {
        String sql = "UPDATE DeletionRequest SET Status = 'Approved', ReviewedBy = ?, ReviewNote = ?, ReviewedAt = GETDATE() WHERE RequestID = ? AND Status = 'Pending'";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewedBy);
            ps.setNString(2, reviewNote);
            ps.setInt(3, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean rejectRequest(int requestId, int reviewedBy, String reviewNote) {
        String sql = "UPDATE DeletionRequest SET Status = 'Rejected', ReviewedBy = ?, ReviewNote = ?, ReviewedAt = GETDATE() WHERE RequestID = ? AND Status = 'Pending'";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewedBy);
            ps.setNString(2, reviewNote);
            ps.setInt(3, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteAccountByRequest(int accountId) {
        String sql = "DELETE FROM Account WHERE AccountID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private DeletionRequest mapRequest(ResultSet rs) throws SQLException {
        DeletionRequest r = new DeletionRequest();
        r.setRequestId(rs.getInt("RequestID"));
        r.setAccountId(rs.getInt("AccountID"));
        r.setReason(rs.getNString("Reason"));
        r.setStatus(rs.getString("Status"));
        int rb = rs.getInt("ReviewedBy");
        r.setReviewedBy(rs.wasNull() ? null : rb);
        r.setReviewNote(rs.getNString("ReviewNote"));
        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        Timestamp tr = rs.getTimestamp("ReviewedAt");
        if (tr != null) r.setReviewedAt(tr.toLocalDateTime());
        r.setAccountEmail(rs.getString("AccountEmail"));
        r.setFullName(rs.getNString("FullName"));
        return r;
    }
}
