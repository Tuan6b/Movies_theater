package com.cinema.dao;

import com.cinema.model.Account;
import com.cinema.model.Invoice;
import com.cinema.model.Seat;
import com.cinema.model.Ticket;
import com.cinema.model.clsSchedule;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Data Access Object for Ticket and Booking operations.
 *
 * @author Antigravity
 */
public class TicketDAO {

    /**
     * Retrieves all booked tickets for a specific showtime schedule.
     * Used for UC47: View Booking Ticket List.
     */
    public List<Ticket> getBookedTicketsByScheduleId(int scheduleId) {
        List<Ticket> tickets = new ArrayList<>();
        String sql = """
                     SELECT t.TicketID, t.ScheduleID, t.SeatID, t.InvoiceID, t.PriceAtBooking, t.Code, t.IsCheckedIn, t.CheckedInAt,
                            s.RowChar, s.ColNumber, s.SeatType, s.IsActive AS SeatActive,
                            i.AccountID, i.SubTotal, i.DiscountAmount, i.TotalAmount, i.PaymentMethod, i.PaymentStatus, i.CreatedAt AS InvoiceCreatedAt,
                            a.Email, u.FullName, u.PhoneNumber
                     FROM Ticket t
                     INNER JOIN Seat s ON t.SeatID = s.SeatID
                     INNER JOIN Invoice i ON t.InvoiceID = i.InvoiceID
                     INNER JOIN Account a ON i.AccountID = a.AccountID
                     LEFT JOIN UserProfile u ON a.AccountID = u.AccountID
                     WHERE t.ScheduleID = ?
                     ORDER BY s.RowChar, s.ColNumber
                     """;

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, scheduleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ticket ticket = new Ticket();
                    ticket.setTicketId(rs.getInt("TicketID"));
                    ticket.setScheduleId(rs.getInt("ScheduleID"));
                    ticket.setSeatId(rs.getInt("SeatID"));
                    ticket.setInvoiceId(rs.getInt("InvoiceID"));
                    ticket.setPriceAtBooking(rs.getDouble("PriceAtBooking"));
                    ticket.setCode(rs.getString("Code"));
                    ticket.setCheckedIn(rs.getBoolean("IsCheckedIn"));
                    ticket.setCheckedInAt(rs.getTimestamp("CheckedInAt"));

                    // Map Seat
                    Seat seat = new Seat();
                    seat.setSeatId(rs.getInt("SeatID"));
                    seat.setRoomId(0); // Room ID not needed here
                    seat.setRowChar(rs.getString("RowChar"));
                    seat.setColNumber(rs.getInt("ColNumber"));
                    seat.setSeatType(rs.getString("SeatType"));
                    seat.setActive(rs.getBoolean("SeatActive"));
                    ticket.setSeat(seat);

                    // Map Invoice
                    Invoice invoice = new Invoice();
                    invoice.setInvoiceId(rs.getInt("InvoiceID"));
                    invoice.setAccountId(rs.getInt("AccountID"));
                    invoice.setSubTotal(rs.getDouble("SubTotal"));
                    invoice.setDiscountAmount(rs.getDouble("DiscountAmount"));
                    invoice.setTotalAmount(rs.getDouble("TotalAmount"));
                    invoice.setPaymentMethod(rs.getString("PaymentMethod"));
                    invoice.setPaymentStatus(rs.getString("PaymentStatus"));
                    invoice.setCreatedAt(rs.getTimestamp("InvoiceCreatedAt"));

                    // Map Account
                    Account account = new Account();
                    account.setAccountId(rs.getInt("AccountID"));
                    account.setEmail(rs.getString("Email"));
                    account.setFullName(rs.getNString("FullName"));
                    account.setPhoneNumber(rs.getString("PhoneNumber"));
                    invoice.setAccount(account);

                    ticket.setInvoice(invoice);
                    tickets.add(ticket);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tickets;
    }

    /**
     * Retrieves all seat IDs that have been booked for a specific schedule.
     */
    public List<Integer> getBookedSeatIdsByScheduleId(int scheduleId) {
        List<Integer> seatIds = new ArrayList<>();
        String sql = "SELECT SeatID FROM Ticket WHERE ScheduleID = ?";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, scheduleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seatIds.add(rs.getInt("SeatID"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return seatIds;
    }

    /**
     * Performs a transaction to save a manual ticket booking:
     * 1. Inserts an Invoice with discount and payment method.
     * 2. Inserts a Ticket for each seat.
     * 3. Increments promotion UsedCount if a promotion was applied.
     * Returns the list of generated ticket codes, or an empty list on failure.
     * Used for UC48: Create Manual Ticket.
     */
    public List<String> createManualBooking(int scheduleId, List<Seat> selectedSeats,
            int customerAccountId, double basePrice,
            String paymentMethod, Integer promotionId, double discountAmount) {
        String sqlInvoice = """
                            INSERT INTO Invoice (AccountID, PromotionID, SubTotal, DiscountAmount, TotalAmount, PaymentMethod, PaymentStatus, CreatedAt)
                            VALUES (?, ?, ?, ?, ?, ?, 'Paid', GETDATE())
                            """;
        String sqlTicket = """
                           INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt)
                           VALUES (?, ?, ?, ?, ?, 0, NULL)
                           """;

        Connection conn = null;
        try {
            conn = DBUtils.getConnection();
            conn.setAutoCommit(false);

            // 1. Calculate seat prices and subtotal
            double subtotal = 0.0;
            List<Double> seatPrices = new ArrayList<>();
            for (Seat seat : selectedSeats) {
                double seatPrice;
                if ("VIP".equalsIgnoreCase(seat.getSeatType())) {
                    seatPrice = basePrice * 1.5;
                } else if ("Couple".equalsIgnoreCase(seat.getSeatType())) {
                    seatPrice = basePrice * 2.0;
                } else {
                    seatPrice = basePrice;
                }
                seatPrices.add(seatPrice);
                subtotal += seatPrice;
            }
            double totalAmount = Math.max(0.0, subtotal - discountAmount);

            // 2. Insert Invoice
            int invoiceId = -1;
            try (PreparedStatement psInvoice = conn.prepareStatement(sqlInvoice, Statement.RETURN_GENERATED_KEYS)) {
                psInvoice.setInt(1, customerAccountId);
                if (promotionId != null) {
                    psInvoice.setInt(2, promotionId);
                } else {
                    psInvoice.setNull(2, Types.INTEGER);
                }
                psInvoice.setDouble(3, subtotal);
                psInvoice.setDouble(4, discountAmount);
                psInvoice.setDouble(5, totalAmount);
                psInvoice.setString(6, paymentMethod);
                psInvoice.executeUpdate();

                try (ResultSet keys = psInvoice.getGeneratedKeys()) {
                    if (keys.next()) {
                        invoiceId = keys.getInt(1);
                    }
                }
            }

            if (invoiceId == -1) {
                throw new SQLException("Failed to retrieve generated Invoice ID");
            }

            // 3. Insert Tickets and collect the generated codes
            List<String> codes = new ArrayList<>();
            try (PreparedStatement psTicket = conn.prepareStatement(sqlTicket)) {
                for (int i = 0; i < selectedSeats.size(); i++) {
                    Seat seat = selectedSeats.get(i);
                    double seatPrice = seatPrices.get(i);
                    String ticketCode = "TK-" + scheduleId + "-" + seat.getSeatId() + "-"
                            + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                    codes.add(ticketCode);

                    psTicket.setInt(1, scheduleId);
                    psTicket.setInt(2, seat.getSeatId());
                    psTicket.setInt(3, invoiceId);
                    psTicket.setDouble(4, seatPrice);
                    psTicket.setString(5, ticketCode);
                    psTicket.addBatch();
                }
                psTicket.executeBatch();
            }

            // 4. Increment promotion usage counter
            if (promotionId != null) {
                try (PreparedStatement psPromo = conn.prepareStatement(
                        "UPDATE Promotion SET UsedCount = UsedCount + 1 WHERE PromotionID = ?")) {
                    psPromo.setInt(1, promotionId);
                    psPromo.executeUpdate();
                }
            }

            conn.commit();
            return codes;
        } catch (SQLException ex) {
            ex.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            return new ArrayList<>();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }

    public void createPendingTickets(Connection conn, int invoiceId, int scheduleId,
            List<Integer> seatIds, Map<Integer, Double> seatPrices) throws SQLException {
        String sql = "INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt) "
                + "VALUES (?, ?, ?, ?, ?, 0, NULL)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int seatId : seatIds) {
                double price = seatPrices != null ? seatPrices.getOrDefault(seatId, 0.0) : 0.0;
                String holdCode = "HOLD-" + invoiceId + "-" + seatId + "-"
                        + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                ps.setInt(1, scheduleId);
                ps.setInt(2, seatId);
                ps.setInt(3, invoiceId);
                ps.setDouble(4, price);
                ps.setString(5, holdCode);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void finalizeTickets(Connection conn, int invoiceId, int scheduleId) throws SQLException {
        String sql = "UPDATE Ticket SET Code = ? WHERE InvoiceID = ? AND Code LIKE 'HOLD-%' AND SeatID = ?";
        String getSeatsSql = "SELECT SeatID FROM Ticket WHERE InvoiceID = ? AND Code LIKE 'HOLD-%'";
        List<Integer> seatIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(getSeatsSql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) seatIds.add(rs.getInt("SeatID"));
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int seatId : seatIds) {
                String finalCode = "TK-" + scheduleId + "-" + seatId + "-"
                        + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                ps.setString(1, finalCode);
                ps.setInt(2, invoiceId);
                ps.setInt(3, seatId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Ticket> getByInvoiceId(int invoiceId) {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.*, s.RowChar, s.ColNumber, s.SeatType FROM Ticket t "
                + "LEFT JOIN Seat s ON t.SeatID = s.SeatID WHERE t.InvoiceID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ticket t = new Ticket();
                    t.setTicketId(rs.getInt("TicketID"));
                    t.setScheduleId(rs.getInt("ScheduleID"));
                    t.setSeatId(rs.getInt("SeatID"));
                    t.setInvoiceId(rs.getInt("InvoiceID"));
                    t.setPriceAtBooking(rs.getDouble("PriceAtBooking"));
                    t.setCode(rs.getString("Code"));
                    t.setCheckedIn(rs.getBoolean("IsCheckedIn"));
                    t.setCheckedInAt(rs.getTimestamp("CheckedInAt"));
                    Seat seat = new Seat();
                    seat.setSeatId(rs.getInt("SeatID"));
                    seat.setRowChar(rs.getString("RowChar"));
                    seat.setColNumber(rs.getInt("ColNumber"));
                    seat.setSeatType(rs.getString("SeatType"));
                    t.setSeat(seat);
                    tickets.add(t);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    public void deleteByInvoiceId(int invoiceId) {
        String sql = "DELETE FROM Ticket WHERE InvoiceID = ? AND Code LIKE 'HOLD-%'";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
