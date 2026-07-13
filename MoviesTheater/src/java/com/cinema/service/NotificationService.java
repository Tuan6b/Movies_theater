/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.service;

import com.cinema.dao.NotificationDAO;
import com.cinema.model.Notification;
import com.cinema.model.ShiftExchangeRequest;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Business logic for creating and reading in-app notifications.
 * Currently used by the Shift Exchange feature to notify employees
 * when a hand-off request is sent, accepted, rejected, or cancelled.
 *
 * @author tuan6b
 */
public class NotificationService {

    private static final DateTimeFormatter SHIFT_DATE_FMT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter SHIFT_TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final NotificationDAO notificationDAO = new NotificationDAO();

    public void notifyShiftExchangeRequested(ShiftExchangeRequest req) {
        String title = "New Shift Exchange Request";
        String message = req.getRequesterName() + " wants to hand off the shift on "
                + formatShift(req) + " to you.";
        create(req.getTargetEmpId(), "SHIFT_EXCHANGE_REQUESTED", title, message, req.getRequestId());
    }

    public void notifyShiftExchangeAccepted(ShiftExchangeRequest req) {
        String title = "Shift Exchange Accepted";
        String message = req.getTargetName() + " accepted your shift exchange request for "
                + formatShift(req) + ".";
        create(req.getRequesterId(), "SHIFT_EXCHANGE_ACCEPTED", title, message, req.getRequestId());
    }

    public void notifyShiftExchangeRejected(ShiftExchangeRequest req) {
        String title = "Shift Exchange Declined";
        String message = req.getTargetName() + " declined your shift exchange request for "
                + formatShift(req) + ".";
        create(req.getRequesterId(), "SHIFT_EXCHANGE_REJECTED", title, message, req.getRequestId());
    }

    public void notifyShiftExchangeCancelled(ShiftExchangeRequest req) {
        String title = "Shift Exchange Cancelled";
        String message = req.getRequesterName() + " cancelled the shift exchange request for "
                + formatShift(req) + ".";
        create(req.getTargetEmpId(), "SHIFT_EXCHANGE_CANCELLED", title, message, req.getRequestId());
    }

    public List<Notification> getRecent(int accountId, int limit) {
        return notificationDAO.getRecentByAccount(accountId, limit);
    }

    public int getUnreadCount(int accountId) {
        return notificationDAO.countUnread(accountId);
    }

    public boolean markAsRead(int notificationId, int accountId) {
        return notificationDAO.markAsRead(notificationId, accountId);
    }

    public int markAllAsRead(int accountId) {
        return notificationDAO.markAllAsRead(accountId);
    }

    private void create(int accountId, String type, String title, String message, int referenceId) {
        Notification n = new Notification();
        n.setAccountId(accountId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setReferenceId(referenceId);
        notificationDAO.insert(n);
    }

    private String formatShift(ShiftExchangeRequest req) {
        if (req.getShiftDate() == null) {
            return "your shift";
        }
        String datePart = req.getShiftDate().format(SHIFT_DATE_FMT);
        if (req.getShiftStart() == null || req.getShiftEnd() == null) {
            return datePart;
        }
        return datePart + " (" + req.getShiftStart().format(SHIFT_TIME_FMT)
                + " - " + req.getShiftEnd().format(SHIFT_TIME_FMT) + ")";
    }
}
