/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.service;

import com.cinema.dao.PromotionDAO;
import com.cinema.dto.PromotionRequestDTO;
import com.cinema.exception.ConflictException;
import com.cinema.exception.NotFoundException;
import com.cinema.exception.ValidationException;
import com.cinema.model.Promotion;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Business logic and validation for Promotion CRUD operations.
 *
 * @author tuan6b
 */
public class PromotionService {

    private final PromotionDAO promotionDAO;

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9\\-_]+$");
    private static final DateTimeFormatter FORM_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public PromotionService() {
        this.promotionDAO = new PromotionDAO();
    }

    /**
     * Constructor with injectable DAO for testing.
     *
     * @param promotionDAO the DAO instance
     */
    public PromotionService(PromotionDAO promotionDAO) {
        this.promotionDAO = promotionDAO;
    }

    // ========== PUBLIC API ==========

    /**
     * Search promotions with filters and pagination.
     *
     * @param keyword search keyword (nullable)
     * @param type discount type filter (nullable)
     * @param status status filter (nullable)
     * @param page page number (1-based)
     * @param pageSize items per page
     * @return list of matching promotions
     * @throws SQLException on database error
     */
    public List<Promotion> findPromotions(String keyword, String type, String status,
            int page, int pageSize) throws SQLException {
        return promotionDAO.search(keyword, normalizeType(type), status, page, pageSize);
    }

    /**
     * Count promotions matching filters.
     *
     * @param keyword search keyword (nullable)
     * @param type discount type filter (nullable)
     * @param status status filter (nullable)
     * @return total count
     * @throws SQLException on database error
     */
    public int countPromotions(String keyword, String type, String status) throws SQLException {
        return promotionDAO.countTotal(keyword, normalizeType(type), status);
    }

    /**
     * Get a single promotion by ID.
     *
     * @param id the promotion ID
     * @return the Promotion entity
     * @throws NotFoundException if not found
     * @throws SQLException on database error
     */
    public Promotion getById(int id) throws NotFoundException, SQLException {
        Promotion p = promotionDAO.findById(id);
        if (p == null) {
            throw new NotFoundException("Promotion not found");
        }
        return p;
    }

    /**
     * Create a new promotion after validating all rules.
     *
     * @param dto form data
     * @throws ValidationException if validation fails
     * @throws SQLException on database error
     */
    public void create(PromotionRequestDTO dto) throws ValidationException, SQLException {
        Map<String, String> errors = validateForCreate(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
        Promotion p = buildEntity(dto);
        promotionDAO.insert(p);
    }

    /**
     * Update an existing promotion with business rule enforcement.
     *
     * @param id the promotion ID
     * @param dto form data
     * @throws NotFoundException if promotion not found
     * @throws ConflictException if business rules conflict
     * @throws ValidationException if validation fails
     * @throws SQLException on database error
     */
    public void update(int id, PromotionRequestDTO dto)
            throws NotFoundException, ConflictException, ValidationException, SQLException {

        Promotion existing = promotionDAO.findById(id);
        if (existing == null) {
            throw new NotFoundException("Promotion not found");
        }

        // Cannot change code/type/value if already used
        if (existing.getUsedCount() > 0) {
            checkUsedRestrictions(existing, dto);
        }

        // Cannot reactivate an expired promotion
        if (dto.getIsActive() != null && dto.getIsActive()
                && existing.getEndDate() != null
                && existing.getEndDate().isBefore(LocalDateTime.now())) {
            throw new ConflictException("Cannot reactivate an expired promotion");
        }

        Map<String, String> errors = validateForUpdate(dto, existing);
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }

        applyUpdates(existing, dto);
        promotionDAO.update(existing);
    }

    /**
     * Soft-delete a promotion (set IsActive = 0).
     *
     * @param id the promotion ID
     * @throws NotFoundException if promotion not found
     * @throws ConflictException if referenced by paid invoices
     * @throws SQLException on database error
     */
    public void delete(int id) throws NotFoundException, ConflictException, SQLException {
        Promotion existing = promotionDAO.findById(id);
        if (existing == null) {
            throw new NotFoundException("Promotion not found");
        }
        if (promotionDAO.hasInvoicePaid(id)) {
            throw new ConflictException(
                    "Cannot delete promotion used in paid invoices");
        }
        promotionDAO.softDelete(id);
    }

    // ========== PRIVATE VALIDATION ==========

    /**
     * Validate all fields for CREATE. Returns error map (empty = valid).
     */
    private Map<String, String> validateForCreate(PromotionRequestDTO dto) throws SQLException {
        Map<String, String> errors = new LinkedHashMap<>();
        validatePromotionCode(dto.getPromotionCode(), 0, errors);
        validateDiscountType(dto.getDiscountType(), errors);
        validateDiscountValue(dto.getDiscountValue(), dto.getDiscountType(), errors);
        validateMinOrderAmount(dto.getMinOrderAmount(), errors);
        validateDates(dto.getStartDate(), dto.getEndDate(), errors);
        validateUsageLimit(dto.getUsageLimit(), errors);
        return errors;
    }

    /**
     * Validate fields for UPDATE with existing entity context. Returns error map.
     */
    private Map<String, String> validateForUpdate(PromotionRequestDTO dto, Promotion existing)
            throws SQLException {
        Map<String, String> errors = new LinkedHashMap<>();

        if (dto.getPromotionCode() != null && existing.getUsedCount() == 0) {
            validatePromotionCode(dto.getPromotionCode(), existing.getPromotionId(), errors);
        }
        if (dto.getDiscountType() != null) {
            validateDiscountType(dto.getDiscountType(), errors);
        }
        if (dto.getDiscountValue() != null) {
            String effectiveType = dto.getDiscountType() != null
                    ? dto.getDiscountType() : existing.getDiscountType();
            validateDiscountValue(dto.getDiscountValue(), effectiveType, errors);
        }
        if (dto.getMinOrderAmount() != null) {
            validateMinOrderAmount(dto.getMinOrderAmount(), errors);
        }
        if (dto.getStartDate() != null || dto.getEndDate() != null) {
            String effectiveStart = dto.getStartDate() != null
                    ? dto.getStartDate()
                    : existing.getStartDate().format(FORM_FORMATTER);
            String effectiveEnd = dto.getEndDate() != null
                    ? dto.getEndDate()
                    : existing.getEndDate().format(FORM_FORMATTER);
            validateDates(effectiveStart, effectiveEnd, errors);
        }
        if (dto.getUsageLimit() != null) {
            validateUsageLimit(dto.getUsageLimit(), errors);
        }
        return errors;
    }

    /**
     * Validate promotionCode: required, max 50, pattern A-Z 0-9 - _, unique.
     */
    private void validatePromotionCode(String code, int excludeId, Map<String, String> errors)
            throws SQLException {
        if (code == null || code.trim().isEmpty()) {
            errors.put("promotionCode", "Promotion code is required");
            return;
        }
        String upper = code.trim().toUpperCase();
        if (upper.length() > 50) {
            errors.put("promotionCode", "Promotion code must not exceed 50 characters");
            return;
        }
        if (!CODE_PATTERN.matcher(upper).matches()) {
            errors.put("promotionCode",
                    "Promotion code may only contain A-Z, 0-9, hyphens and underscores");
            return;
        }
        if (promotionDAO.existsByCode(upper, excludeId)) {
            errors.put("promotionCode", "Promotion code already exists");
        }
    }

    /**
     * Validate discountType: required, must be Percentage or FlatAmount.
     */
    private void validateDiscountType(String discountType, Map<String, String> errors) {
        if (discountType == null || discountType.trim().isEmpty()) {
            errors.put("discountType", "Discount type is required");
            return;
        }
        if (!"Percentage".equals(discountType) && !"FlatAmount".equals(discountType)) {
            errors.put("discountType", "Discount type must be Percentage or FlatAmount");
        }
    }

    /**
     * Validate discountValue: required, > 0, percentage must be <= 100.
     */
    private void validateDiscountValue(BigDecimal value, String type,
            Map<String, String> errors) {
        if (value == null) {
            errors.put("discountValue", "Discount value is required");
            return;
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            errors.put("discountValue", "Discount value must be greater than 0");
            return;
        }
        if ("Percentage".equals(type) && value.compareTo(new BigDecimal("100")) > 0) {
            errors.put("discountValue", "Percentage discount cannot exceed 100");
        }
    }

    /**
     * Validate minOrderAmount: must be >= 0 if provided.
     */
    private void validateMinOrderAmount(BigDecimal amount, Map<String, String> errors) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) < 0) {
            errors.put("minOrderAmount", "Minimum order amount must be >= 0");
        }
    }

    /**
     * Validate start and end dates: both required, endDate at least 1 day after startDate.
     */
    private void validateDates(String startDateStr, String endDateStr,
            Map<String, String> errors) {
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (startDateStr == null || startDateStr.trim().isEmpty()) {
            errors.put("startDate", "Start date is required");
        } else {
            startDate = parseDateTime(startDateStr);
            if (startDate == null) {
                errors.put("startDate", "Invalid start date format");
            }
        }

        if (endDateStr == null || endDateStr.trim().isEmpty()) {
            errors.put("endDate", "End date is required");
        } else {
            endDate = parseDateTime(endDateStr);
            if (endDate == null) {
                errors.put("endDate", "Invalid end date format");
            }
        }

        if (startDate != null && endDate != null) {
            long hoursBetween = ChronoUnit.HOURS.between(startDate, endDate);
            if (hoursBetween < 24) {
                errors.put("endDate", "End date must be at least 1 day after start date");
            }
        }
    }

    /**
     * Validate usageLimit: if provided must be > 0.
     */
    private void validateUsageLimit(Integer usageLimit, Map<String, String> errors) {
        if (usageLimit != null && usageLimit <= 0) {
            errors.put("usageLimit", "Usage limit must be greater than 0");
        }
    }

    /**
     * Enforce restrictions on promotions that have been used (usedCount > 0).
     * Code, type, and value cannot be changed.
     */
    private void checkUsedRestrictions(Promotion existing, PromotionRequestDTO dto) {
        if (dto.getPromotionCode() != null
                && !dto.getPromotionCode().trim().toUpperCase()
                        .equals(existing.getPromotionCode())) {
            throw new ConflictException(
                    "Cannot change code, type, or value of a promotion that has been used");
        }
        if (dto.getDiscountType() != null
                && !dto.getDiscountType().equals(existing.getDiscountType())) {
            throw new ConflictException(
                    "Cannot change code, type, or value of a promotion that has been used");
        }
        if (dto.getDiscountValue() != null
                && dto.getDiscountValue().compareTo(existing.getDiscountValue()) != 0) {
            throw new ConflictException(
                    "Cannot change code, type, or value of a promotion that has been used");
        }
    }

    /**
     * Build a new Promotion entity from form DTO.
     */
    private Promotion buildEntity(PromotionRequestDTO dto) {
        Promotion p = new Promotion();
        p.setPromotionCode(dto.getPromotionCode().trim().toUpperCase());
        p.setDescription(dto.getDescription());
        p.setDiscountType(dto.getDiscountType());
        p.setDiscountValue(dto.getDiscountValue());
        p.setMinOrderAmount(dto.getMinOrderAmount() != null
                ? dto.getMinOrderAmount() : BigDecimal.ZERO);

        if ("Percentage".equals(dto.getDiscountType())) {
            p.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        } else {
            p.setMaxDiscountAmount(null);
        }

        p.setStartDate(parseDateTime(dto.getStartDate().trim()));
        p.setEndDate(parseDateTime(dto.getEndDate().trim()));
        p.setUsageLimit(dto.getUsageLimit());
        p.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return p;
    }

    /**
     * Apply non-null DTO fields onto an existing Promotion entity.
     */
    private void applyUpdates(Promotion existing, PromotionRequestDTO dto) {
        if (dto.getPromotionCode() != null) {
            existing.setPromotionCode(dto.getPromotionCode().trim().toUpperCase());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getDiscountType() != null) {
            existing.setDiscountType(dto.getDiscountType());
        }
        if (dto.getDiscountValue() != null) {
            existing.setDiscountValue(dto.getDiscountValue());
        }
        if (dto.getMinOrderAmount() != null) {
            existing.setMinOrderAmount(dto.getMinOrderAmount());
        }
        if (dto.getMaxDiscountAmount() != null) {
            String effectiveType = dto.getDiscountType() != null
                    ? dto.getDiscountType() : existing.getDiscountType();
            if ("Percentage".equals(effectiveType)) {
                existing.setMaxDiscountAmount(dto.getMaxDiscountAmount());
            }
        }
        if (dto.getStartDate() != null) {
            LocalDateTime parsed = parseDateTime(dto.getStartDate().trim());
            if (parsed != null) {
                existing.setStartDate(parsed);
            }
        }
        if (dto.getEndDate() != null) {
            LocalDateTime parsed = parseDateTime(dto.getEndDate().trim());
            if (parsed != null) {
                existing.setEndDate(parsed);
            }
        }
        if (dto.getUsageLimit() != null) {
            existing.setUsageLimit(dto.getUsageLimit());
        }
        if (dto.getIsActive() != null) {
            existing.setIsActive(dto.getIsActive());
        }
    }

    /**
     * Normalize type filter: return null for empty/invalid values so DAO ignores it.
     */
    private String normalizeType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return null;
        }
        String t = type.trim();
        return ("Percentage".equals(t) || "FlatAmount".equals(t)) ? t : null;
    }

    /**
     * Parse a date-time string in ISO format or HTML datetime-local format.
     * Returns null if the string cannot be parsed.
     */
    private LocalDateTime parseDateTime(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        String s = str.trim();
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTime.parse(s, FORM_FORMATTER);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }
}
