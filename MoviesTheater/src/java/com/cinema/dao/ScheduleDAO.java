package com.cinema.dao;

import com.cinema.model.Schedule;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.cinema.util.DBContext;

public class ScheduleDAO extends DBContext {

    public List<Schedule> getAllSchedules() {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT ScheduleID, MovieID, RoomID, StartTime, EndTime, Status FROM Schedule ORDER BY StartTime";
        try (PreparedStatement stm = connection.prepareStatement(sql); ResultSet rs = stm.executeQuery()) {
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
        String sql = "SELECT ScheduleID, MovieID, RoomID, StartTime, EndTime, Status FROM Schedule ORDER BY StartTime OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (PreparedStatement stm = connection.prepareStatement(sql)) {
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

    public int getTotalSchedulesCount() {
        String sql = "SELECT COUNT(*) FROM Schedule";
        try (PreparedStatement stm = connection.prepareStatement(sql); ResultSet rs = stm.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public Schedule getScheduleById(int id) {
        String sql = "SELECT ScheduleID, MovieID, RoomID, StartTime, EndTime, Status FROM Schedule WHERE ScheduleID = ?";
        try (PreparedStatement stm = connection.prepareStatement(sql)) {
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
        String sql = "INSERT INTO Schedule (MovieID, RoomID, StartTime, EndTime, Status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setInt(1, s.getMovieID());
            stm.setInt(2, s.getRoomID());
            stm.setString(3, s.getShowDate() + " " + s.getStartTime());
            stm.setString(4, s.getShowDate() + " " + s.getEndTime());
            stm.setString(5, s.getStatus());
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean updateSchedule(Schedule s) {
        String sql = "UPDATE Schedule SET MovieID = ?, RoomID = ?, StartTime = ?, EndTime = ?, Status = ? WHERE ScheduleID = ?";
        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setInt(1, s.getMovieID());
            stm.setInt(2, s.getRoomID());
            stm.setString(3, s.getShowDate() + " " + s.getStartTime());
            stm.setString(4, s.getShowDate() + " " + s.getEndTime());
            stm.setString(5, s.getStatus());
            stm.setInt(6, s.getScheduleID());
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean deleteSchedule(int id) {
        String sql = "DELETE FROM Schedule WHERE ScheduleID = ?";
        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setInt(1, id);
            return stm.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private Schedule mapSchedule(ResultSet rs) throws SQLException {
        String startStr = rs.getString("StartTime");
        String endStr = rs.getString("EndTime");
        String showDate = "";
        String startTime = "";
        String endTime = "";
        if (startStr != null && startStr.contains(" ")) {
            String[] parts = startStr.split(" ");
            showDate = parts[0];
            startTime = parts[1];
        }
        if (endStr != null && endStr.contains(" ")) {
            String[] parts = endStr.split(" ");
            endTime = parts[1];
        }
        return new Schedule(
                rs.getInt("ScheduleID"),
                rs.getInt("MovieID"),
                rs.getInt("RoomID"),
                showDate,
                startTime,
                endTime,
                rs.getString("Status")
        );
    }
}
