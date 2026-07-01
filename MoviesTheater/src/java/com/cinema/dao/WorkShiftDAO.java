/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.dao;

import com.cinema.model.WorkShift;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author tuan6b
 */
public class WorkShiftDAO {

    public List<WorkShift> getAll(int empId, String status, String dateStr, int page, int pageSize) {
        List<WorkShift> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT ws.ShiftID, ws.EmployeeID, ws.ShiftDate, ws.StartTime, ws.EndTime, "
                + "ws.Status, ws.Notes, ws.CreatedAt, "
                + "u.FullName AS EmployeeName, a.Email AS EmployeeEmail "
                + "FROM WorkShift ws "
                + "JOIN Account a ON ws.EmployeeID = a.AccountID "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "WHERE 1=1");

        if (empId > 0) {
            sql.append(" AND ws.EmployeeID = ").append(empId);
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND ws.Status = ?");
        }
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            sql.append(" AND ws.ShiftDate = ?");
        }
        sql.append(" ORDER BY ws.ShiftDate DESC, ws.StartTime ASC");
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (status != null && !status.trim().isEmpty()) {
                ps.setString(idx++, status.trim());
            }
            if (dateStr != null && !dateStr.trim().isEmpty()) {
                ps.setDate(idx++, Date.valueOf(dateStr.trim()));
            }
            ps.setInt(idx++, (page - 1) * pageSize);
            ps.setInt(idx, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapShift(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countAll(int empId, String status, String dateStr) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM WorkShift ws WHERE 1=1");

        if (empId > 0) {
            sql.append(" AND ws.EmployeeID = ").append(empId);
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND ws.Status = ?");
        }
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            sql.append(" AND ws.ShiftDate = ?");
        }

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (status != null && !status.trim().isEmpty()) {
                ps.setString(idx++, status.trim());
            }
            if (dateStr != null && !dateStr.trim().isEmpty()) {
                ps.setDate(idx++, Date.valueOf(dateStr.trim()));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public WorkShift getById(int shiftId) {
        String sql = "SELECT ws.ShiftID, ws.EmployeeID, ws.ShiftDate, ws.StartTime, ws.EndTime, "
                + "ws.Status, ws.Notes, ws.CreatedAt, "
                + "u.FullName AS EmployeeName, a.Email AS EmployeeEmail "
                + "FROM WorkShift ws "
                + "JOIN Account a ON ws.EmployeeID = a.AccountID "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "WHERE ws.ShiftID = ?";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shiftId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapShift(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int add(WorkShift shift) {
        String sql = "INSERT INTO WorkShift (EmployeeID, ShiftDate, StartTime, EndTime, Status, Notes) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shift.getEmployeeId());
            ps.setDate(2, Date.valueOf(shift.getShiftDate()));
            ps.setTime(3, Time.valueOf(shift.getStartTime()));
            ps.setTime(4, Time.valueOf(shift.getEndTime()));
            ps.setString(5, shift.getStatus() != null ? shift.getStatus() : "Scheduled");
            ps.setNString(6, shift.getNotes());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean update(WorkShift shift) {
        String sql = "UPDATE WorkShift SET EmployeeID = ?, ShiftDate = ?, StartTime = ?, "
                + "EndTime = ?, Status = ?, Notes = ? WHERE ShiftID = ?";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shift.getEmployeeId());
            ps.setDate(2, Date.valueOf(shift.getShiftDate()));
            ps.setTime(3, Time.valueOf(shift.getStartTime()));
            ps.setTime(4, Time.valueOf(shift.getEndTime()));
            ps.setString(5, shift.getStatus());
            ps.setNString(6, shift.getNotes());
            ps.setInt(7, shift.getShiftId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int shiftId) {
        String sql = "DELETE FROM WorkShift WHERE ShiftID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shiftId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasActiveShift(int employeeId) {
        // Accept both Scheduled and Completed so re-login during same shift is allowed
        String sql = "SELECT COUNT(*) FROM WorkShift "
                + "WHERE EmployeeID = ? "
                + "AND ShiftDate = CAST(GETDATE() AS DATE) "
                + "AND StartTime <= CAST(GETDATE() AS TIME) "
                + "AND EndTime   >= CAST(GETDATE() AS TIME) "
                + "AND Status IN ('Scheduled', 'Completed')";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void checkIn(int employeeId) {
        String sql = "UPDATE WorkShift SET Status = 'Completed' "
                + "WHERE EmployeeID = ? "
                + "AND ShiftDate = CAST(GETDATE() AS DATE) "
                + "AND StartTime <= CAST(GETDATE() AS TIME) "
                + "AND EndTime   >= CAST(GETDATE() AS TIME) "
                + "AND Status = 'Scheduled'";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<WorkShift> getByEmployeeAndMonth(int empId, int year, int month) {
        String sql = "SELECT ws.ShiftID, ws.EmployeeID, ws.ShiftDate, ws.StartTime, ws.EndTime, "
                + "ws.Status, ws.Notes, ws.CreatedAt, "
                + "u.FullName AS EmployeeName, a.Email AS EmployeeEmail "
                + "FROM WorkShift ws "
                + "JOIN Account a ON ws.EmployeeID = a.AccountID "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "WHERE ws.EmployeeID = ? AND YEAR(ws.ShiftDate) = ? AND MONTH(ws.ShiftDate) = ? "
                + "ORDER BY ws.ShiftDate ASC, ws.StartTime ASC";
        List<WorkShift> list = new ArrayList<>();
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapShift(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean existsShift(int empId, java.time.LocalDate date, java.time.LocalTime startTime) {
        String sql = "SELECT COUNT(*) FROM WorkShift WHERE EmployeeID = ? AND ShiftDate = ? AND StartTime = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(startTime));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int bulkCreate(int empId, int year, int month, LocalTime start, LocalTime end) {
        List<WorkShift> existing = getByEmployeeAndMonth(empId, year, month);
        Set<LocalDate> occupied = new HashSet<>();
        for (WorkShift ws : existing) {
            if (ws.getStartTime() != null && ws.getStartTime().equals(start)) {
                occupied.add(ws.getShiftDate());
            }
        }

        LocalDate first = LocalDate.of(year, month, 1);
        LocalDate last  = first.withDayOfMonth(first.lengthOfMonth());
        LocalDate today = LocalDate.now();

        String sql = "INSERT INTO WorkShift (EmployeeID, ShiftDate, StartTime, EndTime, Status) VALUES (?, ?, ?, ?, 'Scheduled')";
        int created = 0;
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (LocalDate d = first; !d.isAfter(last); d = d.plusDays(1)) {
                if (!d.isBefore(today) && !occupied.contains(d)) {
                    ps.setInt(1, empId);
                    ps.setDate(2, Date.valueOf(d));
                    ps.setTime(3, Time.valueOf(start));
                    ps.setTime(4, Time.valueOf(end));
                    ps.addBatch();
                    created++;
                }
            }
            if (created > 0) {
                ps.executeBatch();
                conn.commit();
            }
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return created;
    }

    public List<WorkShift> getByShiftTypeAndMonth(LocalTime start, LocalTime end, int year, int month) {
        String sql = "SELECT ws.ShiftID, ws.EmployeeID, ws.ShiftDate, ws.StartTime, ws.EndTime, "
                + "ws.Status, ws.Notes, ws.CreatedAt, "
                + "u.FullName AS EmployeeName, a.Email AS EmployeeEmail "
                + "FROM WorkShift ws "
                + "JOIN Account a ON ws.EmployeeID = a.AccountID "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "WHERE CAST(ws.StartTime AS TIME) = ? AND CAST(ws.EndTime AS TIME) = ? "
                + "AND YEAR(ws.ShiftDate) = ? AND MONTH(ws.ShiftDate) = ? "
                + "ORDER BY ws.ShiftDate ASC, ws.EmployeeID ASC";
        List<WorkShift> list = new ArrayList<>();
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTime(1, Time.valueOf(start));
            ps.setTime(2, Time.valueOf(end));
            ps.setInt(3, year);
            ps.setInt(4, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapShift(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countWorkingDays(int empId) {
        String sql = "SELECT COUNT(*) FROM WorkShift WHERE EmployeeID = ? AND Status = 'Completed'";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private WorkShift mapShift(ResultSet rs) throws SQLException {
        WorkShift s = new WorkShift();
        s.setShiftId(rs.getInt("ShiftID"));
        s.setEmployeeId(rs.getInt("EmployeeID"));
        s.setEmployeeName(rs.getNString("EmployeeName"));
        s.setEmployeeEmail(rs.getString("EmployeeEmail"));

        Date d = rs.getDate("ShiftDate");
        if (d != null) s.setShiftDate(d.toLocalDate());

        Time st = rs.getTime("StartTime");
        if (st != null) s.setStartTime(st.toLocalTime());

        Time et = rs.getTime("EndTime");
        if (et != null) s.setEndTime(et.toLocalTime());

        s.setStatus(rs.getString("Status"));
        s.setNotes(rs.getNString("Notes"));

        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) s.setCreatedAt(ts.toLocalDateTime());

        return s;
    }
}
