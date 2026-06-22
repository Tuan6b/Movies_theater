package com.cinema.dao;

import com.cinema.model.Schedule;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {

    public List<Schedule> getAllSchedules() {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT ScheduleID, MovieID, RoomID, BaseTicketPrice, StartTime, EndTime, Status FROM Schedule ORDER BY StartTime";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql);
             ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                list.add(mapSchedule(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<Schedule> getSchedulesByPage(int offset, int noOfRecords) {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT ScheduleID, MovieID, RoomID, BaseTicketPrice, StartTime, EndTime, Status FROM Schedule ORDER BY StartTime OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, offset);
            stm.setInt(2, noOfRecords);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSchedule(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<Schedule> getSchedulesByMovieIdAndPage(int movieId, int offset, int noOfRecords) {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT ScheduleID, MovieID, RoomID, BaseTicketPrice, StartTime, EndTime, Status FROM Schedule WHERE MovieID = ? ORDER BY StartTime OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setInt(1, movieId);
            stm.setInt(2, offset);
            stm.setInt(3, noOfRecords);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSchedule(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public int getTotalSchedulesCount() {
        String sql = "SELECT COUNT(*) FROM Schedule";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql);
             ResultSet rs = stm.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public int getTotalSchedulesCountByMovieId(int movieId) {
        String sql = "SELECT COUNT(*) FROM Schedule WHERE MovieID = ?";
        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setInt(1, movieId);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public Schedule getScheduleById(int id) {
        String sql = "SELECT ScheduleID, MovieID, RoomID, BaseTicketPrice, StartTime, EndTime, Status FROM Schedule WHERE ScheduleID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, id);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return mapSchedule(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean addSchedule(Schedule s) {
        String sql = "INSERT INTO Schedule (MovieID, RoomID, BaseTicketPrice, StartTime, EndTime, Status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, s.getMovieID());
            stm.setInt(2, s.getRoomID());
            stm.setDouble(3, s.getBaseTicketPrice());
            stm.setTimestamp(4, toTimestamp(s.getShowDate(), s.getStartTime()));
            stm.setTimestamp(5, toTimestamp(s.getEndDate(), s.getEndTime()));
            stm.setString(6, s.getStatus());
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            String msg = "SQL ERROR in addSchedule: " + ex.getMessage();
            System.err.println(msg);
            throw new RuntimeException(msg, ex);
        }
    }

    public boolean updateSchedule(Schedule s) {
        String sql = "UPDATE Schedule SET MovieID = ?, RoomID = ?, BaseTicketPrice = ?, StartTime = ?, EndTime = ?, Status = ? WHERE ScheduleID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, s.getMovieID());
            stm.setInt(2, s.getRoomID());
            stm.setDouble(3, s.getBaseTicketPrice());
            stm.setTimestamp(4, toTimestamp(s.getShowDate(), s.getStartTime()));
            stm.setTimestamp(5, toTimestamp(s.getEndDate(), s.getEndTime()));
            stm.setString(6, s.getStatus());
            stm.setInt(7, s.getScheduleID());
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean deleteSchedule(int id) {
        String sql = "DELETE FROM Schedule WHERE ScheduleID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, id);
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean hasOverlappingSchedule(int roomId, String startDateTime, String endDateTime, int excludeScheduleId) {
        String sql = "SELECT COUNT(*) FROM Schedule WHERE RoomID = ? AND ScheduleID != ? "
                + "AND Status != 'Cancelled' AND StartTime < ? AND EndTime > ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, roomId);
            stm.setInt(2, excludeScheduleId);
            stm.setTimestamp(3, Timestamp.valueOf(LocalDateTime.parse(endDateTime, dateTimeFormatter)));
            stm.setTimestamp(4, Timestamp.valueOf(LocalDateTime.parse(startDateTime, dateTimeFormatter)));
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Timestamp toTimestamp(String date, String time) {
        String timePart = time;
        if (timePart.contains(".")) {
            timePart = timePart.substring(0, timePart.indexOf('.'));
        }
        if (timePart.length() > 5) {
            timePart = timePart.substring(0, 5);
        }
        return Timestamp.valueOf(LocalDateTime.parse(date + " " + timePart, dateTimeFormatter));
    }

    private String trimToHHmm(String time) {
        if (time == null) return "";
        if (time.contains(".")) time = time.substring(0, time.indexOf('.'));
        if (time.length() > 5) time = time.substring(0, 5);
        return time;
    }

    private Schedule mapSchedule(ResultSet rs) throws SQLException {
        String startStr = rs.getString("StartTime");
        String endStr = rs.getString("EndTime");
        String showDate = "";
        String startTime = "";
        String endTime = "";
        String endDate = "";
        if (startStr != null && startStr.contains(" ")) {
            String[] parts = startStr.split(" ");
            showDate = parts[0];
            startTime = trimToHHmm(parts[1]);
        }
        if (endStr != null && endStr.contains(" ")) {
            String[] parts = endStr.split(" ");
            endDate = parts[0];
            endTime = trimToHHmm(parts[1]);
        }
        return new Schedule(
                rs.getInt("ScheduleID"),
                rs.getInt("MovieID"),
                rs.getInt("RoomID"),
                rs.getDouble("BaseTicketPrice"),
                showDate,
                startTime,
                endTime,
                endDate,
                rs.getString("Status")
        );
    }
}
