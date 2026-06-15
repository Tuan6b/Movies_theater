package com.cinema.model;

import java.sql.Timestamp;

/**
 * Model class representing the Ticket entity.
 *
 * @author Antigravity
 */
public class Ticket {
    private int ticketId;
    private int scheduleId;
    private int seatId;
    private int invoiceId;
    private double priceAtBooking;
    private String code;
    private boolean isCheckedIn;
    private Timestamp checkedInAt;

    // Helper references to bind related seat, schedule and invoice details
    private Seat seat;
    private clsSchedule schedule;
    private Invoice invoice;

    public Ticket() {
    }

    public Ticket(int ticketId, int scheduleId, int seatId, int invoiceId, double priceAtBooking,
                  String code, boolean isCheckedIn, Timestamp checkedInAt) {
        this.ticketId = ticketId;
        this.scheduleId = scheduleId;
        this.seatId = seatId;
        this.invoiceId = invoiceId;
        this.priceAtBooking = priceAtBooking;
        this.code = code;
        this.isCheckedIn = isCheckedIn;
        this.checkedInAt = checkedInAt;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public double getPriceAtBooking() {
        return priceAtBooking;
    }

    public void setPriceAtBooking(double priceAtBooking) {
        this.priceAtBooking = priceAtBooking;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isCheckedIn() {
        return isCheckedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        isCheckedIn = checkedIn;
    }

    public Timestamp getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(Timestamp checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public clsSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(clsSchedule schedule) {
        this.schedule = schedule;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
}
