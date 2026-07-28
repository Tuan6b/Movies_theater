package com.cinema.controller;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for WorkShiftServlet.isAllowedShiftStatus(String), the whitelist that
 * guards the status parameter of UC53 (Manage Work Shift). The accepted set mirrors
 * CHK_WorkShift_Status in the schema. UC45 derives an employee's WorkingDays by
 * counting rows with Status = 'Completed', so an unvalidated value reaching the
 * database would distort staff records as well as break the constraint. Covers
 * Black-box (Equivalence Partitioning) and White-box (Decision Coverage) cases.
 */
public class WorkShiftServletStatusWhitelistTest {

    // TC_UT_01: 'Scheduled' is accepted
    @Test
    public void scheduled_isAllowed() {
        assertTrue(WorkShiftServlet.isAllowedShiftStatus("Scheduled"));
    }

    // TC_UT_02: 'Completed' is accepted
    @Test
    public void completed_isAllowed() {
        assertTrue(WorkShiftServlet.isAllowedShiftStatus("Completed"));
    }

    // TC_UT_03: 'Absent' is accepted
    @Test
    public void absent_isAllowed() {
        assertTrue(WorkShiftServlet.isAllowedShiftStatus("Absent"));
    }

    // TC_UT_04: a value the schema rejects is refused before reaching the database.
    // 'Cancelled' is valid for ShiftExchangeRequest but not for WorkShift.
    @Test
    public void cancelled_isRejected() {
        assertFalse(WorkShiftServlet.isAllowedShiftStatus("Cancelled"));
    }

    // TC_UT_05: an arbitrary hand-edited value is refused
    @Test
    public void arbitraryValue_isRejected() {
        assertFalse(WorkShiftServlet.isAllowedShiftStatus("Hacked"));
    }

    // TC_UT_06: matching is case-sensitive, so only the canonical spelling the form
    // submits is accepted
    @Test
    public void wrongCase_isRejected() {
        assertFalse(WorkShiftServlet.isAllowedShiftStatus("completed"));
    }

    // TC_UT_07: empty string is refused
    @Test
    public void emptyString_isRejected() {
        assertFalse(WorkShiftServlet.isAllowedShiftStatus(""));
    }

    // TC_UT_08: null is refused rather than throwing
    @Test
    public void nullStatus_isRejected() {
        assertFalse(WorkShiftServlet.isAllowedShiftStatus(null));
    }
}
