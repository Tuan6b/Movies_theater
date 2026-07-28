package com.cinema.util;

import com.cinema.model.Seat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for SeatPricing, the single source of seat prices shared by
 * EmployeeDashboardServlet.computeSubtotal and TicketDAO.createManualBooking.
 *
 * The multipliers used to be written out separately in both places, so the
 * promotion discount could be calculated against a different subtotal than the one
 * stored on the Invoice. priceFor is covered directly here because the DAO calls it
 * per seat, a path the servlet-level subtotal tests do not exercise.
 */
public class SeatPricingTest {

    private static final double DELTA = 0.0001;

    private Seat seat(String type) {
        Seat s = new Seat();
        s.setSeatType(type);
        return s;
    }

    // TC_UT_01: a Normal seat costs the base price
    @Test
    public void normalSeat_returnsBasePrice() {
        assertEquals(100000.0, SeatPricing.priceFor(seat("Normal"), 100000), DELTA);
    }

    // TC_UT_02: a VIP seat costs 1.5x the base price
    @Test
    public void vipSeat_returnsOnePointFiveTimesBasePrice() {
        assertEquals(150000.0, SeatPricing.priceFor(seat("VIP"), 100000), DELTA);
    }

    // TC_UT_03: a Couple seat costs 2x the base price
    @Test
    public void coupleSeat_returnsTwiceBasePrice() {
        assertEquals(200000.0, SeatPricing.priceFor(seat("Couple"), 100000), DELTA);
    }

    // TC_UT_04: seat type comparison is case-insensitive, matching the stored values
    @Test
    public void lowercaseSeatType_isCaseInsensitive() {
        assertEquals(200000.0, SeatPricing.priceFor(seat("couple"), 100000), DELTA);
    }

    // TC_UT_05: an unknown seat type falls back to the base price
    @Test
    public void unknownSeatType_fallsBackToBasePrice() {
        assertEquals(100000.0, SeatPricing.priceFor(seat("Sofa"), 100000), DELTA);
    }

    // TC_UT_06: a null seat type falls back to the base price instead of throwing
    @Test
    public void nullSeatType_fallsBackToBasePrice() {
        assertEquals(100000.0, SeatPricing.priceFor(seat(null), 100000), DELTA);
    }

    // TC_UT_07: an empty selection has no cost
    @Test
    public void emptySelection_returnsZero() {
        assertEquals(0.0, SeatPricing.subtotal(Collections.emptyList(), 100000), DELTA);
    }

    // TC_UT_08: a mixed selection sums every branch in one run
    @Test
    public void mixedSelection_sumsAllBranches() {
        List<Seat> seats = Arrays.asList(seat("VIP"), seat("Couple"), seat("Normal"));
        assertEquals(450000.0, SeatPricing.subtotal(seats, 100000), DELTA);
    }

    // TC_UT_09: the per-seat prices the DAO writes add up to the subtotal the servlet
    // validates the promotion against — the drift this class exists to prevent
    @Test
    public void perSeatPrices_addUpToSubtotal() {
        List<Seat> seats = Arrays.asList(seat("VIP"), seat("Couple"), seat("Normal"));
        double summed = 0.0;
        for (Seat s : seats) {
            summed += SeatPricing.priceFor(s, 85000);
        }
        assertEquals(SeatPricing.subtotal(seats, 85000), summed, DELTA);
    }

    // TC_UT_10: a zero base price yields zero for every seat type
    @Test
    public void zeroBasePrice_returnsZero() {
        assertEquals(0.0, SeatPricing.priceFor(seat("VIP"), 0), DELTA);
    }
}
