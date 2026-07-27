/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.dao;

/**
 * Raised when a booking is refused by a business rule rather than by a system
 * fault, so the caller can tell the two apart.
 *
 * The manual booking flow used to signal every failure the same way, by returning
 * an empty list of ticket codes. A seat sold at another counter, a promotion that
 * ran out of redemptions, and a dropped database connection all produced the same
 * "Ghe co the da duoc dat truoc" message. The reason carried here lets the servlet
 * name the actual cause and lets the counter staff act on it.
 *
 * The reason is an enum rather than a message so that no user-facing text has to
 * live in the DAO layer.
 *
 * @author tuan6b
 */
public class BookingConflictException extends Exception {

    /**
     * Which business rule refused the booking.
     */
    public enum Reason {
        /**
         * BR-19: the seat already belongs to another ticket of the same showtime.
         * Detected as a violation of UQ_Ticket_Seat_Schedule.
         */
        SEAT_TAKEN,
        /**
         * The promotion reached its UsageLimit between validation and redemption.
         */
        PROMOTION_EXHAUSTED
    }

    private final Reason reason;

    public BookingConflictException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
