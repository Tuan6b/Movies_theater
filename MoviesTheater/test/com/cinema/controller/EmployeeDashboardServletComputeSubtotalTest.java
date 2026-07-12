package com.cinema.controller;

import com.cinema.model.Seat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for EmployeeDashboardServlet.computeSubtotal(List&lt;Seat&gt;, double).
 * Covers Black-box (Equivalence Partitioning + Boundary Value Analysis) and
 * White-box (Statement + Decision Coverage) test cases, see
 * doc/Huong_dan_Unit_Test.docx section 6 for the design rationale.
 */
public class EmployeeDashboardServletComputeSubtotalTest {

    private static final double DELTA = 0.0001;

    private final EmployeeDashboardServlet servlet = new EmployeeDashboardServlet();

    private Seat seat(String type) {
        Seat s = new Seat();
        s.setSeatType(type);
        return s;
    }

    // TC_UT_01: empty seat list returns 0
    @Test
    public void emptyList_returnsZero() {
        assertEquals(0.0, servlet.computeSubtotal(Collections.emptyList(), 100000), DELTA);
    }

    // TC_UT_02: normal seat returns base price
    @Test
    public void normalSeat_returnsBasePrice() {
        assertEquals(100000.0, servlet.computeSubtotal(List.of(seat("Normal")), 100000), DELTA);
    }

    // TC_UT_03: VIP seat returns 1.5x base price
    @Test
    public void vipSeat_returnsOnePointFiveTimesBasePrice() {
        assertEquals(150000.0, servlet.computeSubtotal(List.of(seat("VIP")), 100000), DELTA);
    }

    // TC_UT_04: Couple seat returns 2x base price
    @Test
    public void coupleSeat_returnsTwiceBasePrice() {
        assertEquals(200000.0, servlet.computeSubtotal(List.of(seat("Couple")), 100000), DELTA);
    }

    // TC_UT_05: mixed seats sum all three branches in one run
    @Test
    public void mixedSeats_sumsAllBranchesCorrectly() {
        List<Seat> seats = Arrays.asList(seat("VIP"), seat("Couple"), seat("Normal"));
        assertEquals(450000.0, servlet.computeSubtotal(seats, 100000), DELTA);
    }

    // TC_UT_06: seat type comparison is case-insensitive
    @Test
    public void lowercaseSeatType_isCaseInsensitive() {
        assertEquals(150000.0, servlet.computeSubtotal(List.of(seat("vip")), 100000), DELTA);
    }

    // TC_UT_07: zero base price returns zero
    @Test
    public void zeroBasePrice_returnsZero() {
        assertEquals(0.0, servlet.computeSubtotal(List.of(seat("Normal")), 0), DELTA);
    }
}
