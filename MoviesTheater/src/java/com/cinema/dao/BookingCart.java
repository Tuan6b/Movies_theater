/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.dao;

/**
 *
 * @author ADMIN
 */
import com.cinema.model.SeatView;
import com.cinema.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BookingSeatDAO {

    public List<SeatView> getSeatsByScheduleId(int scheduleId) {
        List<SeatView> seats = new ArrayList<>();

        String sql =
                "SELECT " +
                "   se.SeatID, " +
                "   se.RoomID, " +
                "   se.RowChar, " +
                "   se.ColNumber, " +
                "   se.SeatType, " +
                "   CASE WHEN EXISTS ( " +
                "       SELECT 1 " +
                "       FROM Ticket t " +
                "       JOIN Invoice i ON t.InvoiceID = i.InvoiceID " +
                "       WHERE t.ScheduleID = ? " +
                "       AND t.SeatID = se.SeatID " +
                "       AND i.PaymentStatus IN ('PENDING', 'PAID') " +
                "   ) THEN 1 ELSE 0 END AS IsBooked " +
                "FROM Seat se " +
                "JOIN [Schedule] s ON se.RoomID = s.RoomID " +
                "WHERE s.ScheduleID = ? " +
                "ORDER BY se.RowChar, se.ColNumber";

        try {
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, scheduleId);
            ps.setInt(2, scheduleId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                SeatView seat = new SeatView();

                seat.setSeatId(rs.getInt("SeatID"));
                seat.setRoomId(rs.getInt("RoomID"));
                seat.setRowChar(rs.getString("RowChar"));
                seat.setColNumber(rs.getInt("ColNumber"));
                seat.setSeatType(rs.getString("SeatType"));
                seat.setBooked(rs.getInt("IsBooked") == 1);

                seats.add(seat);
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return seats;
    }

    public boolean isSeatBooked(int scheduleId, int seatId) {
        String sql =
                "SELECT COUNT(*) AS Total " +
                "FROM Ticket t " +
                "JOIN Invoice i ON t.InvoiceID = i.InvoiceID " +
                "WHERE t.ScheduleID = ? " +
                "AND t.SeatID = ? " +
                "AND i.PaymentStatus IN ('PENDING', 'PAID')";

        try {
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, scheduleId);
            ps.setInt(2, seatId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                boolean booked = rs.getInt("Total") > 0;

                rs.close();
                ps.close();
                conn.close();

                return booked;
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public String getSeatNameById(int seatId) {
        String sql =
                "SELECT RowChar, ColNumber " +
                "FROM Seat " +
                "WHERE SeatID = ?";

        try {
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, seatId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String seatName = rs.getString("RowChar") + rs.getInt("ColNumber");

                rs.close();
                ps.close();
                conn.close();

                return seatName;
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
