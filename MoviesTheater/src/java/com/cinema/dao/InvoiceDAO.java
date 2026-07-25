package com.cinema.dao;

import com.cinema.model.Invoice;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {

    public int createPending(int accountId, Integer promotionId,
            double subTotal, double discountAmount, double totalAmount) throws SQLException {
        String sql = "INSERT INTO Invoice (AccountID, PromotionID, SubTotal, DiscountAmount, "
                + "TotalAmount, PaymentMethod, PaymentStatus, CreatedAt) "
                + "VALUES (?, ?, ?, ?, ?, 'VNPay', 'Pending', GETDATE())";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, accountId);
            if (promotionId != null) {
                ps.setInt(2, promotionId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setDouble(3, subTotal);
            ps.setDouble(4, discountAmount);
            ps.setDouble(5, totalAmount);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public int updateStatusAtomic(int invoiceId, String newStatus, String expectedCurrentStatus) {
        String sql = "Paid".equals(newStatus)
                ? "UPDATE Invoice SET PaymentStatus = ?, SavedAt = COALESCE(SavedAt, GETDATE()) WHERE InvoiceID = ? AND PaymentStatus = ?"
                : "UPDATE Invoice SET PaymentStatus = ? WHERE InvoiceID = ? AND PaymentStatus = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, invoiceId);
            ps.setString(3, expectedCurrentStatus);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int updateStatusAtomic(Connection conn, int invoiceId, String newStatus, String expectedCurrentStatus) throws SQLException {
        String sql = "Paid".equals(newStatus)
                ? "UPDATE Invoice SET PaymentStatus = ?, SavedAt = COALESCE(SavedAt, GETDATE()) WHERE InvoiceID = ? AND PaymentStatus = ?"
                : "UPDATE Invoice SET PaymentStatus = ? WHERE InvoiceID = ? AND PaymentStatus = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, invoiceId);
            ps.setString(3, expectedCurrentStatus);
            return ps.executeUpdate();
        }
    }

    public void updateTxnDetails(int invoiceId, String vnpTxnNo, String bankCode, String payDate) {
        String sql = "UPDATE Invoice SET TransactionRef = ?, BankCode = ?, PayDate = ? WHERE InvoiceID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vnpTxnNo);
            ps.setString(2, bankCode);
            ps.setString(3, payDate);
            ps.setInt(4, invoiceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTxnDetails(Connection conn, int invoiceId, String vnpTxnNo, String bankCode, String payDate) throws SQLException {
        String sql = "UPDATE Invoice SET TransactionRef = ?, BankCode = ?, PayDate = ? WHERE InvoiceID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vnpTxnNo);
            ps.setString(2, bankCode);
            ps.setString(3, payDate);
            ps.setInt(4, invoiceId);
            ps.executeUpdate();
        }
    }

    public String getPaymentStatus(int invoiceId) {
        String sql = "SELECT PaymentStatus FROM Invoice WHERE InvoiceID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("PaymentStatus");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Marks a paid invoice as saved in the authenticated customer's
     * "Vé của tôi" history. The update is idempotent: saving the same invoice
     * again keeps the original saved time and still succeeds. Also claims the
     * invoice if it was created under an unassigned AccountID (<= 0).
     */
    public boolean saveToMyTickets(int invoiceId, int accountId) {
        String sql = """
                UPDATE Invoice
                SET SavedAt = COALESCE(SavedAt, GETDATE()),
                    AccountID = CASE WHEN AccountID <= 0 OR AccountID IS NULL THEN ? ELSE AccountID END
                WHERE InvoiceID = ?
                  AND (AccountID = ? OR AccountID <= 0 OR AccountID IS NULL)
                  AND PaymentStatus = 'Paid'
                """;
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, invoiceId);
            ps.setInt(3, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Invoice findByInvoiceId(int invoiceId) {
        String sql = "SELECT * FROM Invoice WHERE InvoiceID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Invoice> findExpiredPending(int expiryMinutes) {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM Invoice WHERE PaymentStatus = 'Pending' "
                + "AND DATEDIFF(MINUTE, CreatedAt, GETDATE()) > ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, expiryMinutes);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean incrementPromotionUsage(int invoiceId) {
        String sql = "UPDATE Promotion SET UsedCount = UsedCount + 1 "
                + "WHERE PromotionID = (SELECT PromotionID FROM Invoice WHERE InvoiceID = ?) "
                + "AND PromotionID IS NOT NULL";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void incrementPromotionUsage(Connection conn, int invoiceId) throws SQLException {
        String sql = "UPDATE Promotion SET UsedCount = UsedCount + 1 "
                + "WHERE PromotionID = (SELECT PromotionID FROM Invoice WHERE InvoiceID = ?) "
                + "AND PromotionID IS NOT NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ps.executeUpdate();
        }
    }

    public int getScheduleIdByInvoice(int invoiceId) {
        String sql = "SELECT TOP 1 ScheduleID FROM Ticket WHERE InvoiceID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("ScheduleID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getScheduleIdByInvoice(Connection conn, int invoiceId) throws SQLException {
        String sql = "SELECT TOP 1 ScheduleID FROM Ticket WHERE InvoiceID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("ScheduleID");
            }
        }
        return -1;
    }

    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setInvoiceId(rs.getInt("InvoiceID"));
        inv.setAccountId(rs.getInt("AccountID"));
        inv.setPromotionId(rs.getObject("PromotionID") != null ? rs.getInt("PromotionID") : null);
        inv.setSubTotal(rs.getDouble("SubTotal"));
        inv.setDiscountAmount(rs.getDouble("DiscountAmount"));
        inv.setTotalAmount(rs.getDouble("TotalAmount"));
        inv.setPaymentMethod(rs.getString("PaymentMethod"));
        inv.setPaymentStatus(rs.getString("PaymentStatus"));
        inv.setCreatedAt(rs.getTimestamp("CreatedAt"));
        inv.setTransactionRef(rs.getString("TransactionRef"));
        inv.setBankCode(rs.getString("BankCode"));
        inv.setPayDate(rs.getString("PayDate"));
        return inv;
    }
}
