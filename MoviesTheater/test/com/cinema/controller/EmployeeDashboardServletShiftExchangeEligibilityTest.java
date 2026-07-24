package com.cinema.controller;

import com.cinema.model.WorkShift;
import java.time.LocalDate;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for EmployeeDashboardServlet.isShiftExchangeable(WorkShift, int),
 * extracted from handleMyShiftsPost()'s "request_exchange" branch (UC54:
 * Shift Exchange). Covers BR-44.1 (future, still-Scheduled shift only) and
 * ownership, independent of BR-44.2 (self-transfer), which the caller
 * checks separately. Black-box (Equivalence Partitioning + Boundary Value
 * Analysis) test cases.
 */
public class EmployeeDashboardServletShiftExchangeEligibilityTest {

    private static final int REQUESTER_ID = 3;

    private WorkShift shift(int employeeId, String status, LocalDate date) {
        WorkShift s = new WorkShift();
        s.setEmployeeId(employeeId);
        s.setStatus(status);
        s.setShiftDate(date);
        return s;
    }

    // TC_UT_01: a future, Scheduled shift owned by the requester -> exchangeable
    @Test
    public void futureScheduledShiftOwnedByRequester_isExchangeable() {
        WorkShift s = shift(REQUESTER_ID, "Scheduled", LocalDate.now().plusDays(1));
        assertTrue(EmployeeDashboardServlet.isShiftExchangeable(s, REQUESTER_ID));
    }

    // TC_UT_02 (boundary): a shift dated exactly today is still exchangeable
    @Test
    public void todaysScheduledShift_isExchangeable() {
        WorkShift s = shift(REQUESTER_ID, "Scheduled", LocalDate.now());
        assertTrue(EmployeeDashboardServlet.isShiftExchangeable(s, REQUESTER_ID));
    }

    // TC_UT_03 (boundary): a shift dated yesterday is not exchangeable
    @Test
    public void pastShift_isNotExchangeable() {
        WorkShift s = shift(REQUESTER_ID, "Scheduled", LocalDate.now().minusDays(1));
        assertFalse(EmployeeDashboardServlet.isShiftExchangeable(s, REQUESTER_ID));
    }

    // TC_UT_04: shift belongs to a different employee -> not exchangeable
    @Test
    public void shiftOwnedByAnotherEmployee_isNotExchangeable() {
        WorkShift s = shift(REQUESTER_ID + 1, "Scheduled", LocalDate.now().plusDays(1));
        assertFalse(EmployeeDashboardServlet.isShiftExchangeable(s, REQUESTER_ID));
    }

    // TC_UT_05: shift already Completed -> not exchangeable
    @Test
    public void completedShift_isNotExchangeable() {
        WorkShift s = shift(REQUESTER_ID, "Completed", LocalDate.now().plusDays(1));
        assertFalse(EmployeeDashboardServlet.isShiftExchangeable(s, REQUESTER_ID));
    }

    // TC_UT_06: shift already Absent -> not exchangeable
    @Test
    public void absentShift_isNotExchangeable() {
        WorkShift s = shift(REQUESTER_ID, "Absent", LocalDate.now().plusDays(1));
        assertFalse(EmployeeDashboardServlet.isShiftExchangeable(s, REQUESTER_ID));
    }

    // TC_UT_07: shift not found (null, e.g. already exchanged/cancelled elsewhere) -> not exchangeable
    @Test
    public void nullShift_isNotExchangeable() {
        assertFalse(EmployeeDashboardServlet.isShiftExchangeable(null, REQUESTER_ID));
    }
}
