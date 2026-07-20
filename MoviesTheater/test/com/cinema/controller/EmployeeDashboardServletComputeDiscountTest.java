package com.cinema.controller;

import com.cinema.model.Promotion;
import java.math.BigDecimal;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for EmployeeDashboardServlet.computeDiscount(Promotion, double),
 * extracted from handleBook() (UC48: Create Manual Ticket / UC26: Apply
 * Promotion). Covers Black-box (Equivalence Partitioning + Boundary Value
 * Analysis) and White-box (Statement + Decision Coverage) test cases for
 * both discount types and the Percentage max-cap rule.
 */
public class EmployeeDashboardServletComputeDiscountTest {

    private static final double DELTA = 0.0001;

    private final EmployeeDashboardServlet servlet = new EmployeeDashboardServlet();

    private Promotion promo(String type, String value, String maxDiscount) {
        Promotion p = new Promotion();
        p.setDiscountType(type);
        p.setDiscountValue(new BigDecimal(value));
        p.setMaxDiscountAmount(maxDiscount != null ? new BigDecimal(maxDiscount) : null);
        return p;
    }

    // TC_UT_01: Percentage discount below the cap applies in full
    @Test
    public void percentage_belowCap_appliesFullPercentage() {
        Promotion p = promo("Percentage", "10", "1000000");
        assertEquals(100000.0, servlet.computeDiscount(p, 1000000.0), DELTA);
    }

    // TC_UT_02: Percentage discount above maxDiscountAmount is capped
    @Test
    public void percentage_aboveCap_isCappedAtMaxDiscountAmount() {
        Promotion p = promo("Percentage", "50", "100000");
        // 50% of 1,000,000 = 500,000, but capped at 100,000
        assertEquals(100000.0, servlet.computeDiscount(p, 1000000.0), DELTA);
    }

    // TC_UT_03: Percentage discount with no maxDiscountAmount set -> no cap applied
    @Test
    public void percentage_noMaxDiscount_appliesUncappedPercentage() {
        Promotion p = promo("Percentage", "20", null);
        assertEquals(200000.0, servlet.computeDiscount(p, 1000000.0), DELTA);
    }

    // TC_UT_04: discount type comparison is case-insensitive
    @Test
    public void percentage_lowercaseType_isCaseInsensitive() {
        Promotion p = promo("percentage", "10", null);
        assertEquals(100000.0, servlet.computeDiscount(p, 1000000.0), DELTA);
    }

    // TC_UT_05: FlatAmount discount below subtotal applies in full
    @Test
    public void flatAmount_belowSubtotal_appliesFullAmount() {
        Promotion p = promo("FlatAmount", "50000", null);
        assertEquals(50000.0, servlet.computeDiscount(p, 200000.0), DELTA);
    }

    // TC_UT_06 (boundary): FlatAmount discount larger than subtotal is capped
    // at the subtotal so the invoice never goes negative.
    @Test
    public void flatAmount_aboveSubtotal_isCappedAtSubtotal() {
        Promotion p = promo("FlatAmount", "500000", null);
        assertEquals(200000.0, servlet.computeDiscount(p, 200000.0), DELTA);
    }

    // TC_UT_07 (boundary): FlatAmount discount exactly equal to subtotal
    @Test
    public void flatAmount_equalToSubtotal_appliesFullAmount() {
        Promotion p = promo("FlatAmount", "200000", null);
        assertEquals(200000.0, servlet.computeDiscount(p, 200000.0), DELTA);
    }
}
