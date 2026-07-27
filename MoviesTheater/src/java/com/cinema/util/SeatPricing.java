/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.util;

import com.cinema.model.Seat;
import java.util.List;

/**
 * Single source of truth for seat pricing.
 *
 * The multipliers used to be written out twice: once in
 * EmployeeDashboardServlet.computeSubtotal, which prices the basket that the
 * promotion minimum and the discount are calculated against, and once in the loop
 * inside TicketDAO.createManualBooking, which prices the rows actually written to
 * Invoice and Ticket. Editing one copy without the other made the discount apply to
 * a different subtotal than the one stored on the invoice, and only the servlet copy
 * was covered by unit tests.
 *
 * @author tuan6b
 */
public final class SeatPricing {

    private static final double VIP_MULTIPLIER = 1.5;
    private static final double COUPLE_MULTIPLIER = 2.0;

    private SeatPricing() {
    }

    /**
     * Returns the price of a single seat for a showtime with the given base price.
     * An unknown or missing seat type falls back to the base price.
     *
     * @param seat the seat being priced
     * @param basePrice the base ticket price of the showtime
     * @return the price of that seat
     */
    public static double priceFor(Seat seat, double basePrice) {
        String seatType = (seat != null) ? seat.getSeatType() : null;
        if ("VIP".equalsIgnoreCase(seatType)) {
            return basePrice * VIP_MULTIPLIER;
        }
        if ("Couple".equalsIgnoreCase(seatType)) {
            return basePrice * COUPLE_MULTIPLIER;
        }
        return basePrice;
    }

    /**
     * Returns the sum of priceFor over every seat in the selection.
     *
     * @param seats the selected seats
     * @param basePrice the base ticket price of the showtime
     * @return the subtotal before any discount
     */
    public static double subtotal(List<Seat> seats, double basePrice) {
        double total = 0.0;
        for (Seat seat : seats) {
            total += priceFor(seat, basePrice);
        }
        return total;
    }
}
