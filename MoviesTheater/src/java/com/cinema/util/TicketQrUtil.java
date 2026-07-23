package com.cinema.util;

import com.cinema.model.Ticket;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds and reads the payload used by the single QR code of a booking.
 *
 * The database still stores one Ticket row per seat so seat availability,
 * revenue and check-in history continue to work normally. The QR payload uses
 * the first ticket code as the secure booking lookup key and also carries the
 * invoice, schedule, seats and all ticket codes for the booking.
 */
public final class TicketQrUtil {

    private static final String PREFIX = "CGVBOOKING";
    private static final String CODE_FIELD = "CODE=";

    private TicketQrUtil() {
    }

    public static String getPrimaryTicketCode(List<Ticket> tickets) {
        if (tickets == null) {
            return "";
        }
        for (Ticket ticket : tickets) {
            if (ticket != null && ticket.getCode() != null && !ticket.getCode().trim().isEmpty()) {
                return ticket.getCode().trim();
            }
        }
        return "";
    }

    public static String buildBookingPayload(List<Ticket> tickets, List<String> seatNames) {
        String primaryCode = getPrimaryTicketCode(tickets);
        if (primaryCode.isEmpty()) {
            throw new IllegalArgumentException("Cannot build booking QR without a ticket code");
        }

        Ticket firstTicket = null;
        for (Ticket ticket : tickets) {
            if (ticket != null) {
                firstTicket = ticket;
                break;
            }
        }
        if (firstTicket == null) {
            throw new IllegalArgumentException("Cannot build booking QR without ticket data");
        }

        List<String> codes = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket != null && ticket.getCode() != null && !ticket.getCode().trim().isEmpty()) {
                codes.add(cleanField(ticket.getCode()));
            }
        }

        List<String> seats = new ArrayList<>();
        if (seatNames != null) {
            for (String seatName : seatNames) {
                if (seatName != null && !seatName.trim().isEmpty()) {
                    seats.add(cleanField(seatName));
                }
            }
        }

        return PREFIX
                + "|CODE=" + cleanField(primaryCode)
                + "|INVOICE=" + firstTicket.getInvoiceId()
                + "|SCHEDULE=" + firstTicket.getScheduleId()
                + "|SEATS=" + String.join(",", seats)
                + "|TICKETS=" + String.join(",", codes);
    }

    /**
     * Returns the database ticket code from either a normal ticket code or the
     * combined booking QR payload. This lets the employee check-in screen
     * accept both old single-ticket codes and the new grouped QR code.
     */
    public static String extractPrimaryTicketCode(String scannedValue) {
        if (scannedValue == null) {
            return "";
        }

        String value = scannedValue.trim();
        if (value.isEmpty()) {
            return "";
        }

        if (!value.startsWith(PREFIX + "|")) {
            return value;
        }

        String[] parts = value.split("\\|");
        for (String part : parts) {
            if (part.startsWith(CODE_FIELD)) {
                return part.substring(CODE_FIELD.length()).trim();
            }
        }
        return "";
    }

    private static String cleanField(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .replace("|", "/")
                .replace("\r", " ")
                .replace("\n", " ");
    }
}