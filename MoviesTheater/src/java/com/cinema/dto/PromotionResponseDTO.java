/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.dto;

import com.cinema.model.Promotion;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author tuan6b
 */
public class PromotionResponseDTO {

    private int promotionId;
    private String promotionCode;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private String startDate;
    private String endDate;
    private Integer usageLimit;
    private int usedCount;
    private boolean isActive;
    private String status;

    public PromotionResponseDTO() {
    }

    /**
     * Build response DTO from Promotion entity with computed status.
     *
     * @param p the Promotion entity
     * @return populated PromotionResponseDTO
     */
    public static PromotionResponseDTO fromEntity(Promotion p) {
        PromotionResponseDTO dto = new PromotionResponseDTO();
        dto.setPromotionId(p.getPromotionId());
        dto.setPromotionCode(p.getPromotionCode());
        dto.setDescription(p.getDescription());
        dto.setDiscountType(p.getDiscountType());
        dto.setDiscountValue(p.getDiscountValue());
        dto.setMinOrderAmount(p.getMinOrderAmount());
        dto.setMaxDiscountAmount(p.getMaxDiscountAmount());
        dto.setStartDate(p.getStartDate() != null ? p.getStartDate().toString() : null);
        dto.setEndDate(p.getEndDate() != null ? p.getEndDate().toString() : null);
        dto.setUsageLimit(p.getUsageLimit());
        dto.setUsedCount(p.getUsedCount());
        dto.setIsActive(p.isIsActive());

        // Compute status dynamically
        dto.setStatus(computeStatus(p));
        return dto;
    }

    /**
     * Compute status based on business rules:
     * - "expired" if EndDate is before now (regardless of IsActive)
     * - "inactive" if IsActive=false and EndDate >= now
     * - "active" if IsActive=true and EndDate >= now
     */
    private static String computeStatus(Promotion p) {
        LocalDateTime now = LocalDateTime.now();
        if (p.getEndDate() != null && p.getEndDate().isBefore(now)) {
            return "expired";
        }
        if (!p.isIsActive()) {
            return "inactive";
        }
        return "active";
    }

    public int getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(int promotionId) {
        this.promotionId = promotionId;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getMinOrderAmount() {
        return minOrderAmount;
    }

    public void setMinOrderAmount(BigDecimal minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }

    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(Integer usageLimit) {
        this.usageLimit = usageLimit;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
