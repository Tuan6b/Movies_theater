package com.cinema.controller;

import java.time.LocalDateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Decision table tests for EmployeeDashboardServlet.decideCheckinOutcome(), the rules
 * behind UC46. Extracted from the check-in handlers so both entry points — the manual
 * button (ticket id) and the QR scanner (ticket code) — are covered without a database
 * or a camera.
 *
 * SHOW_START is fixed rather than derived from the clock so these cases cannot drift.
 */
public class EmployeeDashboardServletCheckinOutcomeTest {

    private static final LocalDateTime SHOW_START = LocalDateTime.of(2026, 7, 27, 20, 0);
    private static final LocalDateTime SHOW_END   = LocalDateTime.of(2026, 7, 27, 22, 15);

    // R1: no ticket carries that code — a mistyped or forged QR
    @Test
    public void unknownCode_isNotFound() {
        String outcome = EmployeeDashboardServlet.decideCheckinOutcome(
                false, false, null, null, null, SHOW_START);
        assertEquals(EmployeeDashboardServlet.CHECKIN_NOT_FOUND, outcome);
    }

    // R2: second scan of the same ticket — reported before any time rule so a
    // duplicate never masquerades as a late arrival
    @Test
    public void alreadyCheckedIn_isReportedAsDuplicate() {
        String outcome = EmployeeDashboardServlet.decideCheckinOutcome(
                true, true, "Paid", SHOW_START, SHOW_END, SHOW_START);
        assertEquals(EmployeeDashboardServlet.CHECKIN_ALREADY, outcome);
    }

    // R3: ticket exists but its invoice was never paid
    @Test
    public void unpaidInvoice_isBlocked() {
        String outcome = EmployeeDashboardServlet.decideCheckinOutcome(
                true, false, "Pending", SHOW_START, SHOW_END, SHOW_START);
        assertEquals(EmployeeDashboardServlet.CHECKIN_UNPAID, outcome);
    }

    // R4: arriving before the door opens
    @Test
    public void beforeDoorsOpen_isTooEarly() {
        LocalDateTime now = SHOW_START.minusMinutes(
                EmployeeDashboardServlet.CHECKIN_OPENS_MINUTES_BEFORE + 1);
        String outcome = EmployeeDashboardServlet.decideCheckinOutcome(
                true, false, "Paid", SHOW_START, SHOW_END, now);
        assertEquals(EmployeeDashboardServlet.CHECKIN_TOO_EARLY, outcome);
    }

    // R4 boundary: exactly at the opening minute is allowed
    @Test
    public void exactlyAtDoorsOpen_isAllowed() {
        LocalDateTime now = SHOW_START.minusMinutes(
                EmployeeDashboardServlet.CHECKIN_OPENS_MINUTES_BEFORE);
        String outcome = EmployeeDashboardServlet.decideCheckinOutcome(
                true, false, "Paid", SHOW_START, SHOW_END, now);
        assertEquals(EmployeeDashboardServlet.CHECKIN_OK, outcome);
    }

    // R5: the show is over
    @Test
    public void afterShowEnd_isBlocked() {
        String outcome = EmployeeDashboardServlet.decideCheckinOutcome(
                true, false, "Paid", SHOW_START, SHOW_END, SHOW_END.plusMinutes(1));
        assertEquals(EmployeeDashboardServlet.CHECKIN_SHOW_ENDED, outcome);
    }

    // R5 boundary: the last minute of the show still lets a latecomer in
    @Test
    public void exactlyAtShowEnd_isAllowed() {
        String outcome = EmployeeDashboardServlet.decideCheckinOutcome(
                true, false, "Paid", SHOW_START, SHOW_END, SHOW_END);
        assertEquals(EmployeeDashboardServlet.CHECKIN_OK, outcome);
    }

    // R6: the normal case — paid ticket scanned during the window
    @Test
    public void paidTicketDuringWindow_isAllowed() {
        String outcome = EmployeeDashboardServlet.decideCheckinOutcome(
                true, false, "Paid", SHOW_START, SHOW_END, SHOW_START.plusMinutes(10));
        assertEquals(EmployeeDashboardServlet.CHECKIN_OK, outcome);
    }

    // PaymentStatus comparison is case-insensitive: the column is free text and other
    // parts of the codebase write 'Paid' while SQL seed data has used lower case.
    @Test
    public void paymentStatusCheck_ignoresCase() {
        String outcome = EmployeeDashboardServlet.decideCheckinOutcome(
                true, false, "paid", SHOW_START, SHOW_END, SHOW_START);
        assertEquals(EmployeeDashboardServlet.CHECKIN_OK, outcome);
    }

    // A schedule with no times recorded must not block the door on a null check.
    @Test
    public void missingShowtimes_skipTimeRules() {
        String outcome = EmployeeDashboardServlet.decideCheckinOutcome(
                true, false, "Paid", null, null, SHOW_START);
        assertEquals(EmployeeDashboardServlet.CHECKIN_OK, outcome);
    }
}
