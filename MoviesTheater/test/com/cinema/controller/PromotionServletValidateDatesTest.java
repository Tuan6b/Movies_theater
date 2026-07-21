package com.cinema.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for PromotionServlet.validateDates(String, String, Map), used by
 * UC42 (Manage Promotion) to enforce BR-12: start date must be earlier than
 * end date, with a minimum 24-hour span. Covers Black-box (Equivalence
 * Partitioning + Boundary Value Analysis) test cases, complementary to the
 * existing decidePromotionAction Decision Table tests.
 */
public class PromotionServletValidateDatesTest {

    private final PromotionServlet servlet = new PromotionServlet();

    // TC_UT_01: both dates missing -> both required-field errors
    @Test
    public void bothDatesMissing_returnsBothRequiredErrors() {
        Map<String, String> errors = new LinkedHashMap<>();
        servlet.validateDates(null, "", errors);
        assertEquals("Start date is required", errors.get("startDate"));
        assertEquals("End date is required", errors.get("endDate"));
    }

    // TC_UT_02: malformed start date string -> format error, no NPE on the
    // subsequent 24h-span check
    @Test
    public void malformedStartDate_returnsFormatError() {
        Map<String, String> errors = new LinkedHashMap<>();
        servlet.validateDates("not-a-date", "2026-08-02T10:00", errors);
        assertEquals("Invalid start date format", errors.get("startDate"));
    }

    // TC_UT_03 (boundary): span of exactly 24 hours is accepted (BR-12: "at
    // least 24 hours")
    @Test
    public void exactly24HourSpan_isAccepted() {
        Map<String, String> errors = new LinkedHashMap<>();
        servlet.validateDates("2026-08-01T10:00", "2026-08-02T10:00", errors);
        assertFalse(errors.containsKey("endDate"));
    }

    // TC_UT_04 (boundary): span of 23 hours 59 minutes is rejected
    @Test
    public void justUnder24HourSpan_isRejected() {
        Map<String, String> errors = new LinkedHashMap<>();
        servlet.validateDates("2026-08-01T10:00", "2026-08-02T09:59", errors);
        assertEquals("End date must be at least 1 day after start date", errors.get("endDate"));
    }

    // TC_UT_05: end date before start date -> rejected by the same 24h-span rule
    @Test
    public void endBeforeStart_isRejected() {
        Map<String, String> errors = new LinkedHashMap<>();
        servlet.validateDates("2026-08-05T10:00", "2026-08-01T10:00", errors);
        assertEquals("End date must be at least 1 day after start date", errors.get("endDate"));
    }

    // TC_UT_06: valid multi-day range -> no errors at all
    @Test
    public void validMultiDayRange_returnsNoErrors() {
        Map<String, String> errors = new LinkedHashMap<>();
        servlet.validateDates("2026-08-01T00:00", "2026-08-31T23:59", errors);
        assertTrue(errors.isEmpty());
    }
}
