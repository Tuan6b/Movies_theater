package com.cinema.controller;

import java.time.LocalTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for WorkShiftServlet.getShiftTypeKey(LocalTime, LocalTime), used
 * by UC53 (Manage Work Shift) to resolve which of the 5 fixed shift types
 * (BR-43.1) a WorkShift row belongs to when redirecting back to its calendar
 * tab after an edit. Covers Black-box (Equivalence Partitioning) and
 * White-box (Statement + Decision Coverage) test cases.
 */
public class WorkShiftServletShiftTypeKeyTest {

    private final WorkShiftServlet servlet = new WorkShiftServlet();

    // TC_UT_01: exact match on the first entry (6H_SANG, 08:00-14:00)
    @Test
    public void sixHourMorning_resolvesCorrectKey() {
        assertEquals("6H_SANG",
                servlet.getShiftTypeKey(LocalTime.of(8, 0), LocalTime.of(14, 0)));
    }

    // TC_UT_02: exact match on the last entry (8H_CHIEU, 13:00-22:30)
    @Test
    public void eightHourAfternoon_resolvesCorrectKey() {
        assertEquals("8H_CHIEU",
                servlet.getShiftTypeKey(LocalTime.of(13, 0), LocalTime.of(22, 30)));
    }

    // TC_UT_03: exact match on a middle entry (6H_TOI, 20:00-23:59)
    @Test
    public void sixHourEvening_resolvesCorrectKey() {
        assertEquals("6H_TOI",
                servlet.getShiftTypeKey(LocalTime.of(20, 0), LocalTime.of(23, 59)));
    }

    // TC_UT_04 (decision coverage): start matches one type but end does not
    // match that same type -> must NOT short-circuit on start alone, falls
    // back to the "6H_SANG" default.
    @Test
    public void startMatchesButEndDoesNot_fallsBackToDefault() {
        // 08:00 is 6H_SANG's start and also 8H_SANG's start, but 20:00 matches
        // neither type's end time.
        assertEquals("6H_SANG",
                servlet.getShiftTypeKey(LocalTime.of(8, 0), LocalTime.of(20, 0)));
    }

    // TC_UT_05: no entry matches either boundary -> default fallback
    @Test
    public void noMatch_fallsBackToDefault() {
        assertEquals("6H_SANG",
                servlet.getShiftTypeKey(LocalTime.of(1, 0), LocalTime.of(2, 0)));
    }
}
