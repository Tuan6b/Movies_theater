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
import java.time.format.DateTimeFormatter;
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
        p.setStatus(rs.getString("Status"));
        return p;
    }

    /**
     * Find a valid, active promotion by its code.
     * Validates: IsActive=1, within date range, under usage limit.
     */
    public Promotion findByActiveCode(String code) {
        String sql = "SELECT * FROM Promotion "
                + "WHERE PromotionCode = ? AND IsActive = 1 "
                + "AND StartDate <= GETDATE() AND EndDate >= GETDATE() "
                + "AND (UsageLimit IS NULL OR UsedCount < UsageLimit)";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
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
                + "UsageLimit, UsedCount, IsActive, Status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?)";
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
            ps.setString(11, p.getStatus());

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
                + "StartDate = ?, EndDate = ?, UsageLimit = ?, IsActive = ?, Status = ? "
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
            ps.setString(11, p.getStatus());
            ps.setInt(12, p.getPromotionId());

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
        String sql = "UPDATE Promotion SET Status = 'inactive', IsActive = 0 WHERE PromotionID = ?";
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
            // Expire: active/upcoming/inactive → expired when EndDate has passed
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Promotion SET Status = 'expired', IsActive = 0 "
                    + "WHERE Status IN ('active', 'upcoming', 'inactive') AND EndDate < GETDATE()")) {
                int n = ps.executeUpdate();
                if (n > 0) System.out.println("[PromotionStatusScheduler] Expired " + n + " promotion(s).");
            }
            // Activate: upcoming → active when StartDate has passed
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Promotion SET Status = 'active', IsActive = 1 "
                    + "WHERE Status = 'upcoming' AND StartDate <= GETDATE()")) {
                int n = ps.executeUpdate();
                if (n > 0) System.out.println("[PromotionStatusScheduler] Activated " + n + " promotion(s).");
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
    private static final java.util.Map<String, String> SORT_COL_MAP;
    static {
        SORT_COL_MAP = new java.util.LinkedHashMap<>();
        SORT_COL_MAP.put("code",      "PromotionCode");
        SORT_COL_MAP.put("type",      "DiscountType");
        SORT_COL_MAP.put("value",     "DiscountValue");
        SORT_COL_MAP.put("startDate", "StartDate");
        SORT_COL_MAP.put("endDate",   "EndDate");
        SORT_COL_MAP.put("uses",      "UsedCount");
    }

    private String resolveOrderCol(String sortBy) {
        if (sortBy == null) return "PromotionID";
        String col = SORT_COL_MAP.get(sortBy);
        return col != null ? col : "PromotionID";
    }

    public List<Promotion> search(String keyword, String type, String status,
            int page, int pageSize, String sortBy, String sortDir) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM Promotion WHERE 1=1");
        List<Object> params = new ArrayList<>();

        appendFilters(sql, params, keyword, type, status);

        String dir = "DESC".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(resolveOrderCol(sortBy)).append(" ").append(dir);

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

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND Status = ?");
            params.add(status.trim().toLowerCase());
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

    /**
     * Toggle the IsActive flag for a promotion.
     *
     * @param id the promotion ID
     * @param active the new IsActive value
     * @throws SQLException on database error
     */
    public void updateStatus(int id, String status) throws SQLException {
        boolean isActive = "active".equals(status);
        String sql = "UPDATE Promotion SET Status = ?, IsActive = ? WHERE PromotionID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setBoolean(2, isActive);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    /**
     * Activate a promotion early by setting StartDate to now and IsActive to true.
     *
     * @param id the promotion ID
     * @throws SQLException on database error
     */
    public void activateEarly(int id) throws SQLException {
        String sql = "UPDATE Promotion SET StartDate = GETDATE(), Status = 'active', IsActive = 1 WHERE PromotionID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Extend the end date of an expired promotion and reactivate it.
     *
     * @param id the promotion ID
     * @param newEndDate the new end date (must be in the future)
     * @throws SQLException on database error
     */
    public void extendEndDate(int id, LocalDateTime newEndDate) throws SQLException {
        String sql = "UPDATE Promotion SET EndDate = ?, Status = 'active', IsActive = 1 WHERE PromotionID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(newEndDate));
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * Generate the next unique promotion code based on current year/month and existing codes.
     * Pattern: KM + YYYYMM + 3-digit sequence (e.g. KM202506001)
     *
     * @return a unique promotion code string
     * @throws SQLException on database error
     */
    public String generateNextCode() throws SQLException {
        String prefix = "KM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String sql = "SELECT COUNT(*) FROM Promotion WHERE PromotionCode LIKE ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return String.format("%s%03d", prefix, rs.getInt(1) + 1);
                }
            }
        }
        return prefix + "001";
    }

    /**
     * Permanently delete a promotion from the database.
     *
     * @param id the promotion ID
     * @throws SQLException on database error
     */
    public void hardDelete(int id) throws SQLException {
        String sql = "DELETE FROM Promotion WHERE PromotionID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
