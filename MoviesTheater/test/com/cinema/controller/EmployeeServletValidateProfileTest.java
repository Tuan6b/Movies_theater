package com.cinema.controller;

import java.time.LocalDate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the profile rules added to EmployeeServlet for UC44 (Manage
 * Employee): phone normalisation, phone format, and the minimum working age from
 * the Labour Code. Each function takes plain values — the age check takes the
 * reference date rather than reading the clock — so none of this needs a
 * database or a running server.
 *
 * Black-box coverage: Equivalence Partitioning plus Boundary Value Analysis
 * around the 18th birthday and the 10-digit phone length.
 */
public class EmployeeServletValidateProfileTest {

    // ── normalizePhone ──────────────────────────────────────────────────────

    // TC_UT_01: spaces, dots and hyphens are stripped, so the duplicate check
    // sees the same string a plain entry would produce.
    @Test
    public void separators_areStripped() {
        assertEquals("0900000003", EmployeeServlet.normalizePhone("090 000 0003"));
        assertEquals("0900000003", EmployeeServlet.normalizePhone("090.000.0003"));
        assertEquals("0900000003", EmployeeServlet.normalizePhone("090-000-0003"));
    }

    // TC_UT_02: the international prefix collapses to a leading zero.
    @Test
    public void internationalPrefix_becomesLeadingZero() {
        assertEquals("0900000003", EmployeeServlet.normalizePhone("+84900000003"));
        assertEquals("0900000003", EmployeeServlet.normalizePhone("84900000003"));
    }

    // TC_UT_03: null and blank collapse to "", which the caller reports as missing.
    @Test
    public void nullOrBlank_becomesEmpty() {
        assertEquals("", EmployeeServlet.normalizePhone(null));
        assertEquals("", EmployeeServlet.normalizePhone("   "));
    }

    // TC_UT_04: a number that already starts with 0 is left alone — "84" inside a
    // local number must not be mistaken for the country code.
    @Test
    public void localNumber_isUnchanged() {
        assertEquals("0849000003", EmployeeServlet.normalizePhone("0849000003"));
    }

    // ── isValidPhoneFormat ──────────────────────────────────────────────────

    // TC_UT_05: exactly 10 digits starting with 0 -> valid.
    @Test
    public void tenDigitsStartingWithZero_isValid() {
        assertTrue(EmployeeServlet.isValidPhoneFormat("0901234567"));
    }

    // TC_UT_06 (BVA): 9 and 11 digits sit either side of the boundary -> invalid.
    @Test
    public void wrongLength_isInvalid() {
        assertFalse(EmployeeServlet.isValidPhoneFormat("090123456"));
        assertFalse(EmployeeServlet.isValidPhoneFormat("09012345678"));
    }

    // TC_UT_07: right length but not starting with 0 -> invalid.
    @Test
    public void missingLeadingZero_isInvalid() {
        assertFalse(EmployeeServlet.isValidPhoneFormat("1901234567"));
    }

    // TC_UT_08: letters anywhere -> invalid.
    @Test
    public void nonDigits_areInvalid() {
        assertFalse(EmployeeServlet.isValidPhoneFormat("090123456a"));
        assertFalse(EmployeeServlet.isValidPhoneFormat(null));
    }

    // ── isOldEnough (Bộ luật Lao động: đủ 18 tuổi) ──────────────────────────

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 28);

    // TC_UT_09 (BVA): the 18th birthday itself counts as old enough.
    @Test
    public void exactlyEighteenToday_isOldEnough() {
        assertTrue(EmployeeServlet.isOldEnough(LocalDate.of(2008, 7, 28), TODAY));
    }

    // TC_UT_10 (BVA): one day short of the 18th birthday is not.
    @Test
    public void oneDayBeforeEighteenth_isTooYoung() {
        assertFalse(EmployeeServlet.isOldEnough(LocalDate.of(2008, 7, 29), TODAY));
    }

    // TC_UT_11: comfortably an adult -> old enough.
    @Test
    public void clearlyAdult_isOldEnough() {
        assertTrue(EmployeeServlet.isOldEnough(LocalDate.of(1995, 1, 1), TODAY));
    }

    // TC_UT_12: a child, and the junk row this rule was written for — an employee
    // seeded with a date of birth of yesterday.
    @Test
    public void childOrNonsenseDate_isTooYoung() {
        assertFalse(EmployeeServlet.isOldEnough(LocalDate.of(2015, 5, 20), TODAY));
        assertFalse(EmployeeServlet.isOldEnough(LocalDate.of(2026, 7, 27), TODAY));
    }

    // TC_UT_13: null never passes.
    @Test
    public void nullDateOfBirth_isTooYoung() {
        assertFalse(EmployeeServlet.isOldEnough(null, TODAY));
    }

    // TC_UT_14: leap-day birthday reaching 18 in a non-leap year still counts on
    // 1 March — plusYears clamps 29 Feb to 28 Feb, which must not push the
    // birthday past the reference date.
    @Test
    public void leapDayBirthday_isHandled() {
        assertTrue(EmployeeServlet.isOldEnough(LocalDate.of(2008, 2, 29), LocalDate.of(2026, 3, 1)));
    }
}
