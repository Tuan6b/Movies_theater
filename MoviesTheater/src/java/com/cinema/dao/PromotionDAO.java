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
 * Data Access Object for handling Promotion entities.
 * Refactored to reduce verbose JDBC boilerplate using reusable parameter-binding helpers.
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
        if (startTs != null) p.setStartDate(startTs.toLocalDateTime());
        Timestamp endTs = rs.getTimestamp("EndDate");
        if (endTs != null) p.setEndDate(endTs.toLocalDateTime());

        int usageLimit = rs.getInt("UsageLimit");
        p.setUsageLimit(rs.wasNull() ? null : usageLimit);
        p.setUsedCount(rs.getInt("UsedCount"));
        p.setActive(rs.getBoolean("IsActive"));
        p.setStatus(rs.getString("Status"));
        return p;
    }

    /**
     * Helper to bind a single typed parameter to a PreparedStatement.
     */
    private void setParam(PreparedStatement ps, int index, Object val) throws SQLException {
        if (val == null) {
            ps.setNull(index, Types.NULL);
        } else if (val instanceof String) {
            ps.setString(index, (String) val);
        } else if (val instanceof Integer) {
            ps.setInt(index, (Integer) val);
        } else if (val instanceof BigDecimal) {
            ps.setBigDecimal(index, (BigDecimal) val);
        } else if (val instanceof Timestamp) {
            ps.setTimestamp(index, (Timestamp) val);
        } else if (val instanceof Boolean) {
            ps.setBoolean(index, (Boolean) val);
        } else if (val instanceof LocalDateTime) {
            ps.setTimestamp(index, Timestamp.valueOf((LocalDateTime) val));
        } else {
            ps.setObject(index, val);
        }
    }

    /**
     * Bind a list of parameters to a PreparedStatement.
     */
    private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            setParam(ps, i + 1, params.get(i));
        }
    }

    /**
     * Helper to execute update queries (INSERT, UPDATE, DELETE) with parameters.
     */
    private int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                setParam(ps, i + 1, params[i]);
            }
            return ps.executeUpdate();
        }
    }

    /**
     * Helper to bind promotion entity fields to a statement (shared by insert and update).
     */
    private void setPromotionParams(PreparedStatement ps, Promotion p) throws SQLException {
        ps.setString(1, p.getPromotionCode());
        ps.setNString(2, p.getDescription());
        ps.setString(3, p.getDiscountType());
        ps.setBigDecimal(4, p.getDiscountValue());
        ps.setBigDecimal(5, p.getMinOrderAmount() != null ? p.getMinOrderAmount() : BigDecimal.ZERO);
        if (p.getMaxDiscountAmount() != null) {
            ps.setBigDecimal(6, p.getMaxDiscountAmount());
        } else {
            ps.setNull(6, Types.DECIMAL);
        }
        ps.setTimestamp(7, p.getStartDate() != null ? Timestamp.valueOf(p.getStartDate()) : null);
        ps.setTimestamp(8, p.getEndDate() != null ? Timestamp.valueOf(p.getEndDate()) : null);
        if (p.getUsageLimit() != null) {
            ps.setInt(9, p.getUsageLimit());
        } else {
            ps.setNull(9, Types.INTEGER);
        }
        ps.setBoolean(10, p.isActive());
        ps.setString(11, p.getStatus());
    }

    /**
     * Find a valid, active promotion by its code.
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
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Find a promotion by its ID.
     */
    public Promotion findById(int id) throws SQLException {
        String sql = "SELECT * FROM Promotion WHERE PromotionID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Check if a promotion code already exists, optionally excluding a specific ID.
     */
    public boolean existsByCode(String code, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Promotion WHERE PromotionCode = ? AND PromotionID != ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Insert a new promotion and return the generated ID.
     */
    public int insert(Promotion p) throws SQLException {
        String sql = "INSERT INTO Promotion "
                + "(PromotionCode, Description, DiscountType, DiscountValue, "
                + "MinOrderAmount, MaxDiscountAmount, StartDate, EndDate, "
                + "UsageLimit, UsedCount, IsActive, Status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setPromotionParams(ps, p);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Update an existing promotion.
     */
    public void update(Promotion p) throws SQLException {
        String sql = "UPDATE Promotion SET "
                + "PromotionCode = ?, Description = ?, DiscountType = ?, "
                + "DiscountValue = ?, MinOrderAmount = ?, MaxDiscountAmount = ?, "
                + "StartDate = ?, EndDate = ?, UsageLimit = ?, IsActive = ?, Status = ? "
                + "WHERE PromotionID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            setPromotionParams(ps, p);
            ps.setInt(12, p.getPromotionId());
            ps.executeUpdate();
        }
    }

    /**
     * Soft delete: set IsActive = 0.
     */
    public void softDelete(int id) throws SQLException {
        executeUpdate("UPDATE Promotion SET Status = 'inactive', IsActive = 0 WHERE PromotionID = ?", id);
    }

    /**
     * Sync IsActive flag based on StartDate/EndDate for all promotions.
     */
    public void syncStatusByDates() throws SQLException {
        int expired = executeUpdate("UPDATE Promotion SET Status = 'expired', IsActive = 0 "
                + "WHERE Status IN ('active', 'upcoming', 'inactive') AND EndDate < GETDATE()");
        if (expired > 0) System.out.println("[PromotionStatusScheduler] Expired " + expired + " promotion(s).");

        int activated = executeUpdate("UPDATE Promotion SET Status = 'active', IsActive = 1 "
                + "WHERE Status = 'upcoming' AND StartDate <= GETDATE()");
        if (activated > 0) System.out.println("[PromotionStatusScheduler] Activated " + activated + " promotion(s).");
    }

    /**
     * Check if a promotion is referenced by any paid invoice.
     */
    public boolean hasInvoicePaid(int promotionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Invoice WHERE PromotionID = ? AND PaymentStatus = 'Paid'";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, promotionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

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

    /**
     * Search promotions with filters and pagination.
     */
    public List<Promotion> search(String keyword, String type, String status,
            int page, int pageSize, String sortBy, String sortDir) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM Promotion WHERE 1=1");
        List<Object> params = new ArrayList<>();

        appendFilters(sql, params, keyword, type, status);

        String dir = "DESC".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(resolveOrderCol(sortBy)).append(" ").append(dir);
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
     */
    public int countTotal(String keyword, String type, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Promotion WHERE 1=1");
        List<Object> params = new ArrayList<>();

        appendFilters(sql, params, keyword, type, status);

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Append WHERE clause filters to the SQL builder.
     */
    private void appendFilters(StringBuilder sql, List<Object> params,
            String keyword, String type, String status) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (PromotionCode LIKE ? OR Description LIKE ?)");
            String likePattern = "%" + keyword.trim() + "%";
            params.add(likePattern);
            params.add(likePattern);
        }
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
     * Toggle status and active state of a promotion.
     */
    public void updateStatus(int id, String status) throws SQLException {
        executeUpdate("UPDATE Promotion SET Status = ?, IsActive = ? WHERE PromotionID = ?", status, "active".equals(status), id);
    }

    /**
     * Activate a promotion early.
     */
    public void activateEarly(int id) throws SQLException {
        executeUpdate("UPDATE Promotion SET StartDate = GETDATE(), Status = 'active', IsActive = 1 WHERE PromotionID = ?", id);
    }

    /**
     * Extend the end date of an expired promotion and reactivate it.
     */
    public void extendEndDate(int id, LocalDateTime newEndDate) throws SQLException {
        executeUpdate("UPDATE Promotion SET EndDate = ?, Status = 'active', IsActive = 1 WHERE PromotionID = ?", newEndDate, id);
    }

    /**
     * Generate the next unique promotion code.
     */
    public String generateNextCode() throws SQLException {
        String prefix = "KM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String sql = "SELECT COUNT(*) FROM Promotion WHERE PromotionCode LIKE ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return String.format("%s%03d", prefix, rs.getInt(1) + 1);
            }
        }
        return prefix + "001";
    }

    /**
     * Permanently delete a promotion from the database.
     */
    public void hardDelete(int id) throws SQLException {
        executeUpdate("DELETE FROM Promotion WHERE PromotionID = ?", id);
    }
}
