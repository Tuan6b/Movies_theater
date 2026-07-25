package com.cinema.dao;
import com.cinema.model.CustomerTicketHistory;
import com.cinema.model.CustomerTicketHistory.FoodItem;
import com.cinema.model.CustomerTicketHistory.TicketItem;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads invoice-level ticket history for the authenticated customer.
 */
public class CustomerTicketHistoryDAO {

    private static final String BASE_FROM = """
            FROM Invoice i
            OUTER APPLY (
                SELECT TOP 1 t.ScheduleID
                FROM Ticket t
                WHERE t.InvoiceID = i.InvoiceID
                ORDER BY t.TicketID
            ) booked
            LEFT JOIN Schedule sch ON sch.ScheduleID = booked.ScheduleID
            LEFT JOIN Movie m ON m.MovieID = sch.MovieID
            LEFT JOIN Room r ON r.RoomID = sch.RoomID
            """;

    private static final String VALID_CONDITION = """
            i.PaymentStatus = 'Paid'
            AND sch.Status <> 'Cancelled'
            AND sch.EndTime >= GETDATE()
            AND EXISTS (
                SELECT 1
                FROM Ticket remaining
                WHERE remaining.InvoiceID = i.InvoiceID
                  AND remaining.IsCheckedIn = 0
            )
            """;

    public List<CustomerTicketHistory> findHistory(int accountId, String keyword,
            String statusFilter, int offset, int pageSize) {
        List<CustomerTicketHistory> history = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("""
                SELECT i.InvoiceID, i.AccountID, i.SubTotal, i.DiscountAmount,
                       i.TotalAmount, i.PaymentMethod, i.PaymentStatus, i.CreatedAt,
                       i.TransactionRef, i.BankCode, i.PayDate,
                       sch.ScheduleID, sch.StartTime, sch.EndTime, sch.Status AS ScheduleStatus,
                       m.MovieID, m.MovieName, m.Poster,
                       r.RoomNumber, r.RoomType,
                       CASE WHEN
                """);
        sql.append(VALID_CONDITION);
        sql.append("""
                       THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END AS IsValid,
                       CASE WHEN EXISTS (
                           SELECT 1 FROM Ticket uncheckedTicket
                           WHERE uncheckedTicket.InvoiceID = i.InvoiceID
                             AND uncheckedTicket.IsCheckedIn = 0
                       ) THEN CAST(0 AS BIT) ELSE CAST(1 AS BIT) END AS AllCheckedIn
                """);
        sql.append(BASE_FROM);
        sql.append(" WHERE i.AccountID = ? AND i.SavedAt IS NOT NULL AND i.PaymentStatus IN ('Paid', 'Refunded') ");

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (hasKeyword) {
            sql.append(" AND m.MovieName LIKE ? ESCAPE '\\' ");
        }
        appendStatusCondition(sql, statusFilter);
        sql.append(" ORDER BY i.CreatedAt DESC, i.InvoiceID DESC ");
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            ps.setInt(index++, accountId);
            if (hasKeyword) {
                ps.setNString(index++, "%" + escapeLike(keyword.trim()) + "%");
            }
            ps.setInt(index++, Math.max(0, offset));
            ps.setInt(index, Math.max(1, pageSize));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(mapSummary(rs));
                }
            }

            // The summary ResultSet must be closed before another statement is
            // executed on the same SQL Server connection. Without MARS enabled,
            // loading ticket rows inside the ResultSet loop can close/fail the
            // active result and leave the history list empty.
            for (CustomerTicketHistory booking : history) {
                loadTickets(conn, booking);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return history;
    }

    public int countHistory(int accountId, String keyword, String statusFilter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) ");
        sql.append(BASE_FROM);
        sql.append(" WHERE i.AccountID = ? AND i.SavedAt IS NOT NULL AND i.PaymentStatus IN ('Paid', 'Refunded') ");

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (hasKeyword) {
            sql.append(" AND m.MovieName LIKE ? ESCAPE '\\' ");
        }
        appendStatusCondition(sql, statusFilter);

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            ps.setInt(index++, accountId);
            if (hasKeyword) {
                ps.setNString(index, "%" + escapeLike(keyword.trim()) + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public CustomerTicketHistory findOwnedDetail(int accountId, int invoiceId) {
        StringBuilder sql = new StringBuilder();
        sql.append("""
                SELECT i.InvoiceID, i.AccountID, i.SubTotal, i.DiscountAmount,
                       i.TotalAmount, i.PaymentMethod, i.PaymentStatus, i.CreatedAt,
                       i.TransactionRef, i.BankCode, i.PayDate,
                       sch.ScheduleID, sch.StartTime, sch.EndTime, sch.Status AS ScheduleStatus,
                       m.MovieID, m.MovieName, m.Poster,
                       r.RoomNumber, r.RoomType,
                       CASE WHEN
                """);
        sql.append(VALID_CONDITION);
        sql.append("""
                       THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END AS IsValid,
                       CASE WHEN EXISTS (
                           SELECT 1 FROM Ticket uncheckedTicket
                           WHERE uncheckedTicket.InvoiceID = i.InvoiceID
                             AND uncheckedTicket.IsCheckedIn = 0
                       ) THEN CAST(0 AS BIT) ELSE CAST(1 AS BIT) END AS AllCheckedIn
                """);
        sql.append(BASE_FROM);
        sql.append("""
                WHERE i.AccountID = ?
                  AND i.InvoiceID = ?
                  AND i.PaymentStatus IN ('Paid', 'Refunded')
                """);

        try (Connection conn = DBUtils.getConnection()) {
            CustomerTicketHistory booking = null;

            // Read the invoice summary first and close its ResultSet before
            // executing the ticket/food queries on the same SQL Server connection.
            // This works even when multipleActiveResultSets is not enabled.
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                ps.setInt(1, accountId);
                ps.setInt(2, invoiceId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        booking = mapSummary(rs);
                    }
                }
            }

            if (booking == null) {
                System.err.println("[CustomerTicketHistoryDAO] No paid invoice found for accountId=" + accountId + ", invoiceId=" + invoiceId);
                return null;
            }

            // Auto-mark as saved if currently null so it also appears in My Tickets list
            new InvoiceDAO().saveToMyTickets(invoiceId, accountId);

            loadTickets(conn, booking);
            loadFoods(conn, booking);
            return booking;
        } catch (SQLException ex) {
            System.err.println("[CustomerTicketHistoryDAO] Exception in findOwnedDetail for accountId=" + accountId + ", invoiceId=" + invoiceId + ": " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    private void loadTickets(Connection conn, CustomerTicketHistory booking) throws SQLException {
        String sql = """
                SELECT t.TicketID, t.SeatID, t.PriceAtBooking, t.Code,
                       t.IsCheckedIn, t.CheckedInAt,
                       s.RowChar, s.ColNumber, s.SeatType
                FROM Ticket t
                INNER JOIN Seat s ON s.SeatID = t.SeatID
                WHERE t.InvoiceID = ?
                ORDER BY s.RowChar, s.ColNumber, t.TicketID
                """;

        double ticketTotal = 0.0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, booking.getInvoiceId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TicketItem item = new TicketItem();
                    item.setTicketId(rs.getInt("TicketID"));
                    item.setSeatId(rs.getInt("SeatID"));
                    item.setSeatName(rs.getString("RowChar") + rs.getInt("ColNumber"));
                    item.setSeatType(rs.getString("SeatType"));
                    item.setPrice(rs.getDouble("PriceAtBooking"));
                    item.setCode(rs.getString("Code"));
                    item.setCheckedIn(rs.getBoolean("IsCheckedIn"));
                    item.setCheckedInAt(rs.getTimestamp("CheckedInAt"));
                    booking.getTickets().add(item);
                    ticketTotal += item.getPrice();
                    if (booking.getBookingCode() == null || booking.getBookingCode().isBlank()) {
                        booking.setBookingCode(item.getCode());
                    }
                }
            }
        }
        booking.setTicketTotal(ticketTotal);
    }

    private void loadFoods(Connection conn, CustomerTicketHistory booking) throws SQLException {
        String sql = """
                SELECT f.FoodID, f.FoodName, item.Quantity, item.PriceAtBooking
                FROM InvoiceFood item
                INNER JOIN Food f ON f.FoodID = item.FoodID
                WHERE item.InvoiceID = ?
                ORDER BY f.FoodName
                """;

        double foodTotal = 0.0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, booking.getInvoiceId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FoodItem item = new FoodItem();
                    item.setFoodId(rs.getInt("FoodID"));
                    item.setFoodName(rs.getString("FoodName"));
                    item.setQuantity(rs.getInt("Quantity"));
                    item.setPriceAtBooking(rs.getDouble("PriceAtBooking"));
                    booking.getFoods().add(item);
                    foodTotal += item.getLineTotal();
                }
            }
        }
        booking.setFoodTotal(foodTotal);
    }

    private CustomerTicketHistory mapSummary(ResultSet rs) throws SQLException {
        CustomerTicketHistory booking = new CustomerTicketHistory();
        booking.setInvoiceId(rs.getInt("InvoiceID"));
        booking.setScheduleId(rs.getInt("ScheduleID"));
        booking.setMovieId(rs.getInt("MovieID"));
        booking.setMovieName(rs.getNString("MovieName"));
        booking.setPoster(rs.getString("Poster"));
        booking.setStartTime(rs.getTimestamp("StartTime"));
        booking.setEndTime(rs.getTimestamp("EndTime"));
        booking.setScheduleStatus(rs.getString("ScheduleStatus"));
        booking.setRoomNumber(rs.getNString("RoomNumber"));
        booking.setRoomType(rs.getString("RoomType"));
        booking.setSubTotal(rs.getDouble("SubTotal"));
        booking.setDiscountAmount(rs.getDouble("DiscountAmount"));
        booking.setTotalAmount(rs.getDouble("TotalAmount"));
        booking.setPaymentMethod(rs.getString("PaymentMethod"));
        booking.setPaymentStatus(rs.getString("PaymentStatus"));
        booking.setCreatedAt(rs.getTimestamp("CreatedAt"));
        booking.setTransactionRef(getSafeString(rs, "TransactionRef"));
        booking.setBankCode(getSafeString(rs, "BankCode"));
        booking.setPayDate(getSafeString(rs, "PayDate"));
        booking.setValid(rs.getBoolean("IsValid"));
        booking.setAllCheckedIn(rs.getBoolean("AllCheckedIn"));
        return booking;
    }

    private String getSafeString(ResultSet rs, String columnName) {
        try {
            return rs.getString(columnName);
        } catch (SQLException e) {
            return null;
        }
    }

    private void appendStatusCondition(StringBuilder sql, String statusFilter) {
        if ("valid".equals(statusFilter)) {
            sql.append(" AND (").append(VALID_CONDITION).append(") ");
        } else if ("expired".equals(statusFilter)) {
            sql.append(" AND NOT (").append(VALID_CONDITION).append(") ");
        }
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
