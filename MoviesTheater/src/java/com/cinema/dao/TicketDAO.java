package com.cinema.dao;

import com.cinema.model.Account;
import com.cinema.model.Food;
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

    /**
     * Performs a transaction to save a customer self-checkout booking:
     * 1. Inserts an Invoice with discount, payment method and status 'Paid'.
     * 2. Inserts a Ticket for each seat (with pre-computed per-seat price).
     * 3. Inserts an InvoiceFood row for each selected food item.
     * 4. Increments promotion UsedCount if a promotion was applied.
     * Returns the list of created Ticket rows (with generated codes), in the
     * same order as seatIds, or an empty list on failure (e.g. a seat was
     * booked by someone else in the meantime).
     */
    public List<Ticket> createCustomerBooking(int scheduleId, List<Integer> seatIds, List<Double> seatPrices,
            int accountId, String paymentMethod, Integer promotionId, double discountAmount,
            Map<Integer, Integer> foodQuantities, Map<Integer, Food> foodMap) {
        String sqlInvoice = """
                            INSERT INTO Invoice (AccountID, PromotionID, SubTotal, DiscountAmount, TotalAmount, PaymentMethod, PaymentStatus, CreatedAt)
                            VALUES (?, ?, ?, ?, ?, ?, 'Paid', GETDATE())
                            """;
        String sqlTicket = """
                           INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt)
                           VALUES (?, ?, ?, ?, ?, 0, NULL)
                           """;
        String sqlInvoiceFood = """
                                INSERT INTO InvoiceFood (InvoiceID, FoodID, Quantity, PriceAtBooking)
                                VALUES (?, ?, ?, ?)
                                """;

        Connection conn = null;
        try {
            conn = DBUtils.getConnection();
            conn.setAutoCommit(false);

            double ticketSubtotal = 0.0;
            for (double price : seatPrices) {
                ticketSubtotal += price;
            }

            double foodSubtotal = 0.0;
            if (foodQuantities != null) {
                for (Map.Entry<Integer, Integer> entry : foodQuantities.entrySet()) {
                    Food food = foodMap != null ? foodMap.get(entry.getKey()) : null;
                    if (food != null) {
                        foodSubtotal += food.getPrice() * entry.getValue();
                    }
                }
            }

            double subtotal = ticketSubtotal + foodSubtotal;
            double totalAmount = Math.max(0.0, subtotal - discountAmount);

            // 1. Insert Invoice
            int invoiceId = -1;
            try (PreparedStatement psInvoice = conn.prepareStatement(sqlInvoice, Statement.RETURN_GENERATED_KEYS)) {
                psInvoice.setInt(1, accountId);
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

            // 2. Insert Tickets and collect the created rows (with codes)
            List<Ticket> tickets = new ArrayList<>();
            try (PreparedStatement psTicket = conn.prepareStatement(sqlTicket)) {
                for (int i = 0; i < seatIds.size(); i++) {
                    int seatId = seatIds.get(i);
                    double seatPrice = seatPrices.get(i);
                    String ticketCode = "TK-" + scheduleId + "-" + seatId + "-"
                            + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                    psTicket.setInt(1, scheduleId);
                    psTicket.setInt(2, seatId);
                    psTicket.setInt(3, invoiceId);
                    psTicket.setDouble(4, seatPrice);
                    psTicket.setString(5, ticketCode);
                    psTicket.addBatch();

                    Ticket ticket = new Ticket();
                    ticket.setScheduleId(scheduleId);
                    ticket.setSeatId(seatId);
                    ticket.setInvoiceId(invoiceId);
                    ticket.setPriceAtBooking(seatPrice);
                    ticket.setCode(ticketCode);
                    tickets.add(ticket);
                }
                psTicket.executeBatch();
            }

            // 3. Insert InvoiceFood rows for selected food items
            if (foodQuantities != null && !foodQuantities.isEmpty()) {
                try (PreparedStatement psFood = conn.prepareStatement(sqlInvoiceFood)) {
                    for (Map.Entry<Integer, Integer> entry : foodQuantities.entrySet()) {
                        Food food = foodMap != null ? foodMap.get(entry.getKey()) : null;
                        if (food == null) {
                            continue;
                        }
                        psFood.setInt(1, invoiceId);
                        psFood.setInt(2, entry.getKey());
                        psFood.setInt(3, entry.getValue());
                        psFood.setDouble(4, food.getPrice());
                        psFood.addBatch();
                    }
                    psFood.executeBatch();
                }
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
            return tickets;
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
}
