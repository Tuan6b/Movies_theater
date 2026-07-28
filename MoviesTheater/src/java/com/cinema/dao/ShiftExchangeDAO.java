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

    // Pending requests that name this employee as the new owner. Read-only for the
    // employee since UC44.1: the Manager decides, the recipient is only informed.
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

    /**
     * Every hand-off request in the cinema, for the Manager's review screen.
     *
     * @param status one of Pending / Accepted / Rejected / Cancelled, or null for
     *               all of them. Compared as a bind parameter, never concatenated.
     */
    public List<ShiftExchangeRequest> getAllForManager(String status) {
        return getAllForManager(status, 1, Integer.MAX_VALUE);
    }

    public List<ShiftExchangeRequest> getAllForManager(String status, int page, int pageSize) {
        List<ShiftExchangeRequest> list = new ArrayList<>();
        // Pending first, then newest: the queue the Manager has to act on stays on
        // top even when the filter is showing the whole history.
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        if (status != null) {
            sql.append("WHERE r.Status = ? ");
        }
        sql.append("ORDER BY CASE WHEN r.Status = 'Pending' THEN 0 ELSE 1 END, r.CreatedAt DESC ");
        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (status != null) {
                ps.setString(idx++, status);
            }
            ps.setInt(idx++, Math.max(0, (page - 1) * pageSize));
            ps.setInt(idx, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countAllForManager(String status) {
        String sql = "SELECT COUNT(*) FROM ShiftExchangeRequest "
                + (status != null ? "WHERE Status = ?" : "");
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (status != null) {
                ps.setString(1, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countPending() {
        String sql = "SELECT COUNT(*) FROM ShiftExchangeRequest WHERE Status = 'Pending'";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * One page of the employee's own request history, newest first. Paged because
     * this list only ever grows — an employee who has swapped shifts all year would
     * otherwise push the whole month calendar off the screen.
     */
    public List<ShiftExchangeRequest> getOutgoing(int requesterId, int page, int pageSize) {
        List<ShiftExchangeRequest> list = new ArrayList<>();
        String sql = BASE_SELECT
                + "WHERE r.RequesterID = ? "
                + "ORDER BY r.CreatedAt DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requesterId);
            ps.setInt(2, (page - 1) * pageSize);
            ps.setInt(3, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countOutgoing(int requesterId) {
        String sql = "SELECT COUNT(*) FROM ShiftExchangeRequest WHERE RequesterID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requesterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * The employee's still-undecided requests, unpaged. The calendar marks the
     * shifts these cover, so it needs all of them regardless of which page of the
     * history list happens to be on screen. Only Pending rows, so it stays small.
     */
    public List<ShiftExchangeRequest> getOutgoingPending(int requesterId) {
        List<ShiftExchangeRequest> list = new ArrayList<>();
        String sql = BASE_SELECT
                + "WHERE r.RequesterID = ? AND r.Status = 'Pending' "
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

    /** approve() outcomes, so the Manager screen can say what actually went wrong. */
    public static final String APPROVE_OK          = "OK";
    public static final String APPROVE_NOT_PENDING = "NOT_PENDING";
    public static final String APPROVE_SHIFT_MOVED = "SHIFT_MOVED";
    public static final String APPROVE_TARGET_BUSY = "TARGET_BUSY";
    public static final String APPROVE_ERROR       = "ERROR";

    /**
     * Approves a pending hand-off and transfers the shift to the target employee.
     *
     * The Manager is the approver, so this takes no employee id: authorisation is
     * the /manager/shift-exchanges route itself. Three things can still stop the
     * transfer, and each gets its own return code: the request was already settled,
     * the requester has lost the shift since asking, or the recipient is already
     * booked at that hour — UQ_WorkShift_Emp_Date_Start would reject that last one
     * anyway, so it is checked up front rather than surfaced as a SQL error.
     *
     * @return one of the APPROVE_* constants
     */
    public String approve(int requestId) {
        String sqlGet = "SELECT ShiftID, RequesterID, TargetEmpID FROM ShiftExchangeRequest "
                + "WHERE RequestID = ? AND Status = 'Pending'";
        try (Connection conn = DBUtils.getConnection()) {
            int shiftId;
            int originalOwnerId;
            int newOwnerId;
            try (PreparedStatement ps = conn.prepareStatement(sqlGet)) {
                ps.setInt(1, requestId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return APPROVE_NOT_PENDING;
                    shiftId = rs.getInt("ShiftID");
                    originalOwnerId = rs.getInt("RequesterID");
                    newOwnerId = rs.getInt("TargetEmpID");
                }
            }

            conn.setAutoCommit(false);
            try {
                // Does the recipient already work a shift starting at that hour that day?
                String sqlClash = "SELECT COUNT(*) FROM WorkShift target "
                        + "WHERE target.EmployeeID = ? AND EXISTS ("
                        + "  SELECT 1 FROM WorkShift src WHERE src.ShiftID = ? "
                        + "  AND src.ShiftDate = target.ShiftDate AND src.StartTime = target.StartTime)";
                try (PreparedStatement ps = conn.prepareStatement(sqlClash)) {
                    ps.setInt(1, newOwnerId);
                    ps.setInt(2, shiftId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            conn.rollback();
                            return APPROVE_TARGET_BUSY;
                        }
                    }
                }

                // Verify the requester still owns the shift before transferring
                String sqlTransfer = "UPDATE WorkShift SET EmployeeID = ? "
                        + "WHERE ShiftID = ? AND EmployeeID = ?";
                int rows;
                try (PreparedStatement ps = conn.prepareStatement(sqlTransfer)) {
                    ps.setInt(1, newOwnerId);
                    ps.setInt(2, shiftId);
                    ps.setInt(3, originalOwnerId);
                    rows = ps.executeUpdate();
                }

                if (rows == 0) {
                    conn.rollback();
                    return APPROVE_SHIFT_MOVED;
                }

                String sqlApprove = "UPDATE ShiftExchangeRequest "
                        + "SET Status = 'Accepted', RespondedAt = GETDATE() WHERE RequestID = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlApprove)) {
                    ps.setInt(1, requestId);
                    ps.executeUpdate();
                }

                // Any other pending request for this shift now names a requester who
                // no longer owns it, so it could never be approved. Closing them here
                // keeps the Manager's queue from filling with requests that can only
                // fail, and it happens inside the same transaction as the transfer.
                String sqlSupersede = "UPDATE ShiftExchangeRequest "
                        + "SET Status = 'Rejected', RespondedAt = GETDATE() "
                        + "WHERE ShiftID = ? AND Status = 'Pending' AND RequestID <> ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlSupersede)) {
                    ps.setInt(1, shiftId);
                    ps.setInt(2, requestId);
                    ps.executeUpdate();
                }

                conn.commit();
                return APPROVE_OK;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return APPROVE_ERROR;
    }

    /** Manager turns a pending hand-off down; the shift stays with the requester. */
    public boolean reject(int requestId) {
        String sql = "UPDATE ShiftExchangeRequest "
                + "SET Status = 'Rejected', RespondedAt = GETDATE() "
                + "WHERE RequestID = ? AND Status = 'Pending'";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
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
