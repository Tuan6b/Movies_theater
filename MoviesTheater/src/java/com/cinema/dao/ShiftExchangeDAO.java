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
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tuan6b
 */
public class ShiftExchangeDAO {

    private static final int ROLE_EMPLOYEE = 3;

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

    // Returns the generated RequestID (> 0) on success, or 0 if the shift no
    // longer belongs to the requester, if the target is not an active employee,
    // or on error.
    public int createRequest(int shiftId, int requesterId, int targetEmpId, String message) {
        // Both EXISTS clauses are the authorisation check, done at the DB level so a
        // hand-edited form cannot bypass them. The first confirms the shift belongs
        // to the requester. The second confirms the recipient is an employee who is
        // not blocked: TargetEmpID only has a foreign key to Account, so without it
        // a shift could be handed to a Customer, an Admin or a deactivated account,
        // none of which can work it.
        String sql = "INSERT INTO ShiftExchangeRequest (ShiftID, RequesterID, TargetEmpID, Message) "
                + "SELECT ?, ?, ?, ? "
                + "WHERE EXISTS (SELECT 1 FROM WorkShift WHERE ShiftID = ? AND EmployeeID = ?) "
                + "AND EXISTS (SELECT 1 FROM Account WHERE AccountID = ? "
                + "AND RoleID = " + ROLE_EMPLOYEE + " AND IsBlocked = 0)";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, shiftId);
            ps.setInt(2, requesterId);
            ps.setInt(3, targetEmpId);
            ps.setNString(4, message);
            ps.setInt(5, shiftId);
            ps.setInt(6, requesterId);
            ps.setInt(7, targetEmpId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ShiftExchangeRequest getById(int requestId) {
        String sql = BASE_SELECT + "WHERE r.RequestID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRequest(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
