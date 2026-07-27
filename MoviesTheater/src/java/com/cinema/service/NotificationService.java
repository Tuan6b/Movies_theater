/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.service;

import com.cinema.dao.AccountDAO;
import com.cinema.dao.NotificationDAO;
import com.cinema.model.Notification;
import com.cinema.model.ShiftExchangeRequest;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Business logic for creating and reading in-app notifications.
 * Currently used by the Shift Exchange feature to notify the Manager
 * when a hand-off request needs approval, and the two employees involved
 * when it is approved, declined, or cancelled.
 *
 * @author tuan6b
 */
public class NotificationService {

    private static final DateTimeFormatter SHIFT_DATE_FMT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter SHIFT_TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final AccountDAO accountDAO = new AccountDAO();

    /** Manager, per the role mapping in AuthFilter. */
    private static final int ROLE_MANAGER = 4;

    /**
     * A new hand-off request. The Manager decides it, so the queue notification goes
     * to every Manager; the proposed recipient gets a heads-up but nothing to act on.
     */
    public void notifyShiftExchangeRequested(ShiftExchangeRequest req) {
        String managerMessage = req.getRequesterName() + " wants to hand off the shift on "
                + formatShift(req) + " to " + req.getTargetName() + ". Awaiting your approval.";
        for (int managerId : accountDAO.getActiveAccountIdsByRole(ROLE_MANAGER)) {
            create(managerId, "SHIFT_EXCHANGE_REQUESTED", "Shift Exchange Needs Approval",
                    managerMessage, req.getRequestId());
        }

        String targetMessage = req.getRequesterName() + " asked to hand off the shift on "
                + formatShift(req) + " to you. It takes effect once the manager approves it.";
        create(req.getTargetEmpId(), "SHIFT_EXCHANGE_REQUESTED", "Shift Exchange Proposed",
                targetMessage, req.getRequestId());
    }

    /** Manager approved: the shift has already moved, so both sides need telling. */
    public void notifyShiftExchangeAccepted(ShiftExchangeRequest req) {
        String title = "Shift Exchange Approved";
        create(req.getRequesterId(), "SHIFT_EXCHANGE_ACCEPTED", title,
                "The manager approved your shift exchange for " + formatShift(req)
                + ". " + req.getTargetName() + " now covers it.",
                req.getRequestId());
        create(req.getTargetEmpId(), "SHIFT_EXCHANGE_ACCEPTED", title,
                "The manager approved the hand-off from " + req.getRequesterName()
                + ". The shift on " + formatShift(req) + " is now yours.",
                req.getRequestId());
    }

    public void notifyShiftExchangeRejected(ShiftExchangeRequest req) {
        String title = "Shift Exchange Declined";
        create(req.getRequesterId(), "SHIFT_EXCHANGE_REJECTED", title,
                "The manager declined your shift exchange for " + formatShift(req)
                + ". The shift stays with you.",
                req.getRequestId());
        create(req.getTargetEmpId(), "SHIFT_EXCHANGE_REJECTED", title,
                "The manager declined the hand-off from " + req.getRequesterName()
                + " for " + formatShift(req) + ".",
                req.getRequestId());
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
