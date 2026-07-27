package com.cinema.dao;

import com.cinema.model.Account;
import com.cinema.model.Food;
import com.cinema.model.Invoice;
import com.cinema.model.Seat;
import com.cinema.model.Ticket;
import com.cinema.model.clsSchedule;
import com.cinema.util.DBUtils;
import com.cinema.util.SeatPricing;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
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
     * Returns the list of generated ticket codes, or an empty list if the booking
     * failed for a system reason such as a lost connection.
     * Used for UC48: Create Manual Ticket.
     *
     * BR-19 is enforced by UQ_Ticket_Seat_Schedule on Ticket(ScheduleID, SeatID).
     * The seat map rendered by the book form is only advisory: a seat can be sold at
     * another counter between that render and this insert, and the unique constraint
     * is what actually stops the double sale. That case, and a promotion that runs
     * out of redemptions mid-transaction, are reported as BookingConflictException so
     * the caller can distinguish a refused booking from a broken one.
     */
    public List<String> createManualBooking(int scheduleId, List<Seat> selectedSeats,
            int customerAccountId, double basePrice,
            String paymentMethod, Integer promotionId, double discountAmount)
            throws BookingConflictException {
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

            // 1. Calculate seat prices and subtotal.
            // SeatPricing is shared with EmployeeDashboardServlet.computeSubtotal so
            // the discount cannot be calculated against different multipliers than
            // the prices written to Invoice and Ticket here.
            double subtotal = 0.0;
            List<Double> seatPrices = new ArrayList<>();
            for (Seat seat : selectedSeats) {
                double seatPrice = SeatPricing.priceFor(seat, basePrice);
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

            // 4. Increment promotion usage counter.
            // The UsedCount < UsageLimit test has to be part of this UPDATE.
            // findByActiveCode checked the same condition earlier on its own
            // connection, so two counters redeeming the last available use would both
            // have passed that check and both incremented past the limit here.
            if (promotionId != null) {
                String sqlPromo = "UPDATE Promotion SET UsedCount = UsedCount + 1 "
                        + "WHERE PromotionID = ? AND (UsageLimit IS NULL OR UsedCount < UsageLimit)";
                try (PreparedStatement psPromo = conn.prepareStatement(sqlPromo)) {
                    psPromo.setInt(1, promotionId);
                    if (psPromo.executeUpdate() == 0) {
                        conn.rollback();
                        throw new BookingConflictException(
                                BookingConflictException.Reason.PROMOTION_EXHAUSTED);
                    }
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
            // A unique violation here means BR-19 stopped a double sale, which is a
            // refused booking rather than a system fault and deserves its own message.
            if (isUniqueViolation(ex)) {
                throw new BookingConflictException(BookingConflictException.Reason.SEAT_TAKEN);
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

            // 4. Increment promotion usage counter.
            // Same race as the manual flow: the limit was checked on a different
            // connection before checkout, so it is re-tested inside the UPDATE. A
            // promotion that ran out in the meantime rolls the whole booking back
            // rather than pushing UsedCount past UsageLimit.
            if (promotionId != null) {
                String sqlPromo = "UPDATE Promotion SET UsedCount = UsedCount + 1 "
                        + "WHERE PromotionID = ? AND (UsageLimit IS NULL OR UsedCount < UsageLimit)";
                try (PreparedStatement psPromo = conn.prepareStatement(sqlPromo)) {
                    psPromo.setInt(1, promotionId);
                    if (psPromo.executeUpdate() == 0) {
                        conn.rollback();
                        return new ArrayList<>();
                    }
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

    /**
     * Reports whether a failure was a unique key violation.
     *
     * SQL Server raises 2627 for a unique constraint and 2601 for a unique index.
     * executeBatch reports the failure as a BatchUpdateException whose own error
     * code can be left unset, so the chained exceptions are inspected as well.
     */
    private boolean isUniqueViolation(SQLException ex) {
        for (SQLException current = ex; current != null; current = current.getNextException()) {
            int code = current.getErrorCode();
            if (code == 2627 || code == 2601) {
                return true;
            }
        }
        return false;
    }

    // ========== UC46: CHECK-IN BY TICKET CODE (QR) ==========

    /**
     * Everything the counter needs to decide whether a scanned ticket may enter.
     * Carries the raw showtime window and the invoice payment status, which the
     * check-in list query does not select.
     */
    public static class CheckinInfo {
        private int ticketId;
        private String code;
        private boolean checkedIn;
        private String paymentStatus;
        private LocalDateTime showStart;
        private LocalDateTime showEnd;
        private String movieName;
        private String seatName;
        private String customerName;

        public int getTicketId()            { return ticketId; }
        public String getCode()             { return code; }
        public boolean isCheckedIn()        { return checkedIn; }
        public String getPaymentStatus()    { return paymentStatus; }
        public LocalDateTime getShowStart() { return showStart; }
        public LocalDateTime getShowEnd()   { return showEnd; }
        public String getMovieName()        { return movieName; }
        public String getSeatName()         { return seatName; }
        public String getCustomerName()     { return customerName; }
    }

    private static final String CHECKIN_INFO_SELECT = """
                     SELECT t.TicketID, t.Code, t.IsCheckedIn,
                            i.PaymentStatus,
                            sc.StartTime, sc.EndTime,
                            m.MovieName,
                            s.RowChar + CAST(s.ColNumber AS VARCHAR) AS SeatName,
                            u.FullName AS CustomerName
                     FROM Ticket t
                     INNER JOIN Schedule sc ON t.ScheduleID = sc.ScheduleID
                     INNER JOIN Movie m ON sc.MovieID = m.MovieID
                     INNER JOIN Seat s ON t.SeatID = s.SeatID
                     INNER JOIN Invoice i ON t.InvoiceID = i.InvoiceID
                     INNER JOIN Account a ON i.AccountID = a.AccountID
                     LEFT JOIN UserProfile u ON a.AccountID = u.AccountID
                     """;

    /**
     * Looks up a ticket by the code carried in its QR. Returns null when no ticket
     * has that code. Ticket.Code is UNIQUE, so at most one row can match.
     */
    public CheckinInfo findCheckinInfoByCode(String code) throws SQLException {
        return findCheckinInfo(CHECKIN_INFO_SELECT + " WHERE t.Code = ?", code);
    }

    /** Same data, looked up by TicketID — used by the manual button in the list. */
    public CheckinInfo findCheckinInfoById(int ticketId) throws SQLException {
        return findCheckinInfo(CHECKIN_INFO_SELECT + " WHERE t.TicketID = ?", ticketId);
    }

    private CheckinInfo findCheckinInfo(String sql, Object param) throws SQLException {
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param instanceof Integer) {
                ps.setInt(1, (Integer) param);
            } else {
                ps.setString(1, String.valueOf(param));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                CheckinInfo info = new CheckinInfo();
                info.ticketId      = rs.getInt("TicketID");
                info.code          = rs.getString("Code");
                info.checkedIn     = rs.getBoolean("IsCheckedIn");
                info.paymentStatus = rs.getString("PaymentStatus");
                Timestamp start = rs.getTimestamp("StartTime");
                if (start != null) info.showStart = start.toLocalDateTime();
                Timestamp end = rs.getTimestamp("EndTime");
                if (end != null) info.showEnd = end.toLocalDateTime();
                info.movieName    = rs.getNString("MovieName");
                info.seatName     = rs.getString("SeatName");
                info.customerName = rs.getNString("CustomerName");
                return info;
            }
        }
    }

    /**
     * Marks a ticket as checked in. The IsCheckedIn = 0 guard makes this idempotent:
     * a second scan of the same ticket updates nothing and returns false, so the
     * original CheckedInAt survives.
     *
     * @return true only if this call is the one that let the ticket through
     */
    public boolean markCheckedIn(int ticketId) throws SQLException {
        String sql = "UPDATE Ticket SET IsCheckedIn = 1, CheckedInAt = GETDATE() "
                + "WHERE TicketID = ? AND IsCheckedIn = 0";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketId);
            return ps.executeUpdate() > 0;
        }
    }
}
