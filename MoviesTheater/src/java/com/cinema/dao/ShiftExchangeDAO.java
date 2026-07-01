/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.dao;

import com.cinema.model.ShiftExchangeRequest;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tuan6b
 */
public class ShiftExchangeDAO {

    private static final String BASE_SELECT =
            "SELECT r.RequestID, r.ShiftID, r.RequesterID, r.TargetEmpID, r.Message, "
            + "r.Status, r.CreatedAt, r.RespondedAt, "
            + "ws.ShiftDate, ws.StartTime, ws.EndTime, "
            + "ru.FullName AS RequesterName, tu.FullName AS TargetName "
            + "FROM ShiftExchangeRequest r "
            + "JOIN WorkShift ws ON r.ShiftID = ws.ShiftID "
            + "JOIN Account ra ON r.RequesterID = ra.AccountID "
            + "LEFT JOIN UserProfile ru ON ra.AccountID = ru.AccountID "
            + "JOIN Account ta ON r.TargetEmpID = ta.AccountID "
            + "LEFT JOIN UserProfile tu ON ta.AccountID = tu.AccountID ";

    public int createRequest(int shiftId, int requesterId, int targetEmpId, String message) {
        // WHERE EXISTS ensures the shift belongs to the requester at the DB level
        String sql = "INSERT INTO ShiftExchangeRequest (ShiftID, RequesterID, TargetEmpID, Message) "
                + "SELECT ?, ?, ?, ? "
                + "WHERE EXISTS (SELECT 1 FROM WorkShift WHERE ShiftID = ? AND EmployeeID = ?)";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shiftId);
            ps.setInt(2, requesterId);
            ps.setInt(3, targetEmpId);
            ps.setNString(4, message);
            ps.setInt(5, shiftId);
            ps.setInt(6, requesterId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<ShiftExchangeRequest> getIncoming(int targetEmpId) {
        List<ShiftExchangeRequest> list = new ArrayList<>();
        String sql = BASE_SELECT
                + "WHERE r.TargetEmpID = ? AND r.Status = 'Pending' "
                + "ORDER BY r.CreatedAt DESC";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, targetEmpId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ShiftExchangeRequest> getOutgoing(int requesterId) {
        List<ShiftExchangeRequest> list = new ArrayList<>();
        String sql = BASE_SELECT
                + "WHERE r.RequesterID = ? "
                + "ORDER BY r.CreatedAt DESC";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requesterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Accepts a pending handoff request and transfers the shift to the target employee.
    public boolean accept(int requestId, int currentEmpId) {
        String sqlGet = "SELECT ShiftID, RequesterID FROM ShiftExchangeRequest "
                + "WHERE RequestID = ? AND TargetEmpID = ? AND Status = 'Pending'";
        try (Connection conn = DBUtils.getConnection()) {
            int shiftId = 0;
            int originalOwnerId = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlGet)) {
                ps.setInt(1, requestId);
                ps.setInt(2, currentEmpId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return false;
                    shiftId = rs.getInt("ShiftID");
                    originalOwnerId = rs.getInt("RequesterID");
                }
            }

            conn.setAutoCommit(false);
            try {
                // Verify the requester still owns the shift before transferring
                String sqlTransfer = "UPDATE WorkShift SET EmployeeID = ? "
                        + "WHERE ShiftID = ? AND EmployeeID = ?";
                int rows;
                try (PreparedStatement ps = conn.prepareStatement(sqlTransfer)) {
                    ps.setInt(1, currentEmpId);
                    ps.setInt(2, shiftId);
                    ps.setInt(3, originalOwnerId);
                    rows = ps.executeUpdate();
                }

                if (rows == 0) {
                    conn.rollback();
                    return false;
                }

                String sqlAccept = "UPDATE ShiftExchangeRequest "
                        + "SET Status = 'Accepted', RespondedAt = GETDATE() WHERE RequestID = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlAccept)) {
                    ps.setInt(1, requestId);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean reject(int requestId, int currentEmpId) {
        String sql = "UPDATE ShiftExchangeRequest "
                + "SET Status = 'Rejected', RespondedAt = GETDATE() "
                + "WHERE RequestID = ? AND TargetEmpID = ? AND Status = 'Pending'";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setInt(2, currentEmpId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cancel(int requestId, int requesterId) {
        String sql = "UPDATE ShiftExchangeRequest "
                + "SET Status = 'Cancelled', RespondedAt = GETDATE() "
                + "WHERE RequestID = ? AND RequesterID = ? AND Status = 'Pending'";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setInt(2, requesterId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ShiftExchangeRequest mapRequest(ResultSet rs) throws SQLException {
        ShiftExchangeRequest r = new ShiftExchangeRequest();
        r.setRequestId(rs.getInt("RequestID"));
        r.setShiftId(rs.getInt("ShiftID"));
        r.setRequesterId(rs.getInt("RequesterID"));
        r.setRequesterName(rs.getNString("RequesterName"));
        r.setTargetEmpId(rs.getInt("TargetEmpID"));
        r.setTargetName(rs.getNString("TargetName"));
        r.setMessage(rs.getNString("Message"));
        r.setStatus(rs.getString("Status"));

        Date d = rs.getDate("ShiftDate");
        if (d != null) r.setShiftDate(d.toLocalDate());

        Time st = rs.getTime("StartTime");
        if (st != null) r.setShiftStart(st.toLocalTime());

        Time et = rs.getTime("EndTime");
        if (et != null) r.setShiftEnd(et.toLocalTime());

        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());

        Timestamp rts = rs.getTimestamp("RespondedAt");
        if (rts != null) r.setRespondedAt(rts.toLocalDateTime());

        return r;
    }
}
