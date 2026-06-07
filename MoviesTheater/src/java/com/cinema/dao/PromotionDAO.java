/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.dao;

import com.cinema.model.Promotion;
import com.cinema.util.DBUtils;
import java.math.BigDecimal;
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

/**
 *
 * @author tuan6b
 */
public class PromotionDAO {

    /**
     * Map a ResultSet row to a Promotion entity.
     */
    private Promotion mapRow(ResultSet rs) throws SQLException {
        Promotion p = new Promotion();
        p.setPromotionId(rs.getInt("PromotionID"));
        p.setPromotionCode(rs.getString("PromotionCode"));
        p.setDescription(rs.getNString("Description"));
        p.setDiscountType(rs.getString("DiscountType"));
        p.setDiscountValue(rs.getBigDecimal("DiscountValue"));
        p.setMinOrderAmount(rs.getBigDecimal("MinOrderAmount"));
        p.setMaxDiscountAmount(rs.getBigDecimal("MaxDiscountAmount"));

        Timestamp startTs = rs.getTimestamp("StartDate");
        if (startTs != null) {
            p.setStartDate(startTs.toLocalDateTime());
        }
        Timestamp endTs = rs.getTimestamp("EndDate");
        if (endTs != null) {
            p.setEndDate(endTs.toLocalDateTime());
        }

        int usageLimit = rs.getInt("UsageLimit");
        if (rs.wasNull()) {
            p.setUsageLimit(null);
        } else {
            p.setUsageLimit(usageLimit);
        }

        p.setUsedCount(rs.getInt("UsedCount"));
        p.setActive(rs.getBoolean("IsActive"));
        return p;
    }

    /**
     * Find a promotion by its ID.
     *
     * @param id the promotion ID
     * @return Promotion or null if not found
     * @throws SQLException on database error
     */
    public Promotion findById(int id) throws SQLException {
        String sql = "SELECT * FROM Promotion WHERE PromotionID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Check if a promotion code already exists, optionally excluding a specific ID.
     *
     * @param code the promotion code to check
     * @param excludeId the ID to exclude (0 or negative to skip exclusion)
     * @return true if code exists
     * @throws SQLException on database error
     */
    public boolean existsByCode(String code, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Promotion WHERE PromotionCode = ? AND PromotionID != ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Insert a new promotion and return the generated ID.
     *
     * @param p the Promotion to insert
     * @return generated promotion ID
     * @throws SQLException on database error
     */
    public int insert(Promotion p) throws SQLException {
        String sql = "INSERT INTO Promotion "
                + "(PromotionCode, Description, DiscountType, DiscountValue, "
                + "MinOrderAmount, MaxDiscountAmount, StartDate, EndDate, "
                + "UsageLimit, UsedCount, IsActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?)";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getPromotionCode());
            ps.setNString(2, p.getDescription());
            ps.setString(3, p.getDiscountType());
            ps.setBigDecimal(4, p.getDiscountValue());
            ps.setBigDecimal(5, p.getMinOrderAmount() != null
                    ? p.getMinOrderAmount() : BigDecimal.ZERO);

            if (p.getMaxDiscountAmount() != null) {
                ps.setBigDecimal(6, p.getMaxDiscountAmount());
            } else {
                ps.setNull(6, Types.DECIMAL);
            }

            ps.setTimestamp(7, Timestamp.valueOf(p.getStartDate()));
            ps.setTimestamp(8, Timestamp.valueOf(p.getEndDate()));

            if (p.getUsageLimit() != null) {
                ps.setInt(9, p.getUsageLimit());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

            ps.setBoolean(10, p.isActive());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Update an existing promotion.
     *
     * @param p the Promotion with updated fields
     * @throws SQLException on database error
     */
    public void update(Promotion p) throws SQLException {
        String sql = "UPDATE Promotion SET "
                + "PromotionCode = ?, Description = ?, DiscountType = ?, "
                + "DiscountValue = ?, MinOrderAmount = ?, MaxDiscountAmount = ?, "
                + "StartDate = ?, EndDate = ?, UsageLimit = ?, IsActive = ? "
                + "WHERE PromotionID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getPromotionCode());
            ps.setNString(2, p.getDescription());
            ps.setString(3, p.getDiscountType());
            ps.setBigDecimal(4, p.getDiscountValue());
            ps.setBigDecimal(5, p.getMinOrderAmount() != null
                    ? p.getMinOrderAmount() : BigDecimal.ZERO);

            if (p.getMaxDiscountAmount() != null) {
                ps.setBigDecimal(6, p.getMaxDiscountAmount());
            } else {
                ps.setNull(6, Types.DECIMAL);
            }

            ps.setTimestamp(7, Timestamp.valueOf(p.getStartDate()));
            ps.setTimestamp(8, Timestamp.valueOf(p.getEndDate()));

            if (p.getUsageLimit() != null) {
                ps.setInt(9, p.getUsageLimit());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

            ps.setBoolean(10, p.isActive());
            ps.setInt(11, p.getPromotionId());

            ps.executeUpdate();
        }
    }

    /**
     * Soft delete: set IsActive = 0.
     *
     * @param id the promotion ID
     * @throws SQLException on database error
     */
    public void softDelete(int id) throws SQLException {
        String sql = "UPDATE Promotion SET IsActive = 0 WHERE PromotionID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Sync IsActive flag based on StartDate/EndDate for all promotions.
     * Expires promotions whose end date has passed; activates promotions that have reached their start date.
     *
     * @throws SQLException on database error
     */
    public void syncStatusByDates() throws SQLException {
        try (Connection conn = DBUtils.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Promotion SET IsActive = 0 WHERE EndDate < GETDATE() AND IsActive = 1")) {
                int expired = ps.executeUpdate();
                if (expired > 0) {
                    System.out.println("[PromotionStatusScheduler] Expired " + expired + " promotion(s).");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Promotion SET IsActive = 1"
                    + " WHERE StartDate <= GETDATE() AND EndDate >= GETDATE() AND IsActive = 0")) {
                int activated = ps.executeUpdate();
                if (activated > 0) {
                    System.out.println("[PromotionStatusScheduler] Activated " + activated + " promotion(s).");
                }
            }
        }
    }

    /**
     * Check if a promotion is referenced by any paid invoice.
     *
     * @param promotionId the promotion ID
     * @return true if at least one paid invoice references this promotion
     * @throws SQLException on database error
     */
    public boolean hasInvoicePaid(int promotionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Invoice "
                + "WHERE PromotionID = ? AND PaymentStatus = 'Paid'";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, promotionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Search promotions with filters and pagination.
     *
     * @param keyword search keyword (nullable)
     * @param type discount type filter (nullable)
     * @param status status filter: "active", "expired", "inactive" (nullable)
     * @param page page number (1-based)
     * @param pageSize items per page
     * @return list of matching promotions
     * @throws SQLException on database error
     */
    public List<Promotion> search(String keyword, String type, String status,
            int page, int pageSize) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM Promotion WHERE 1=1");
        List<Object> params = new ArrayList<>();

        appendFilters(sql, params, keyword, type, status);

        // Sort descending by ID to show newest first
        sql.append(" ORDER BY PromotionID DESC");

        // Pagination with offset and limit
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        int offset = (page - 1) * pageSize;
        params.add(offset);
        params.add(pageSize);

        List<Promotion> promotions = new ArrayList<>();
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    promotions.add(mapRow(rs));
                }
            }
        }
        return promotions;
    }

    /**
     * Count total promotions matching filters (for pagination metadata).
     *
     * @param keyword search keyword (nullable)
     * @param type discount type filter (nullable)
     * @param status status filter (nullable)
     * @return total count
     * @throws SQLException on database error
     */
    public int countTotal(String keyword, String type, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Promotion WHERE 1=1");
        List<Object> params = new ArrayList<>();

        appendFilters(sql, params, keyword, type, status);

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Append WHERE clause filters to the SQL builder.
     */
    private void appendFilters(StringBuilder sql, List<Object> params,
            String keyword, String type, String status) {

        // Keyword filter: search in PromotionCode and Description
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (PromotionCode LIKE ? OR Description LIKE ?)");
            String likePattern = "%" + keyword.trim() + "%";
            params.add(likePattern);
            params.add(likePattern);
        }

        // Discount type filter
        if (type != null && !type.trim().isEmpty()) {
            sql.append(" AND DiscountType = ?");
            params.add(type.trim());
        }

        // Status filter (computed dynamically)
        if (status != null && !status.trim().isEmpty()) {
            switch (status.trim().toLowerCase()) {
                case "active":
                    sql.append(" AND IsActive = 1 AND StartDate <= GETDATE() AND EndDate >= GETDATE()");
                    break;
                case "upcoming":
                    sql.append(" AND StartDate > GETDATE()");
                    break;
                case "expired":
                    sql.append(" AND EndDate < GETDATE()");
                    break;
                case "inactive":
                    sql.append(" AND IsActive = 0 AND EndDate >= GETDATE()");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Set PreparedStatement parameters from a list of objects.
     */
    private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object val = params.get(i);
            if (val instanceof String) {
                ps.setString(i + 1, (String) val);
            } else if (val instanceof Integer) {
                ps.setInt(i + 1, (Integer) val);
            } else if (val instanceof BigDecimal) {
                ps.setBigDecimal(i + 1, (BigDecimal) val);
            } else if (val instanceof Timestamp) {
                ps.setTimestamp(i + 1, (Timestamp) val);
            }
        }
    }
}
