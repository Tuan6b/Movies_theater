package com.cinema.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Schedule model class represents a movie screening schedule.
 * Internally stores start/end as java.sql.Timestamp for type safety.
 * Provides backward-compatible String getters for View/Controller code.
 *
 * @author Tuan Phong Nguyen
 */
public class Schedule {

    private int scheduleID;
    private int movieID;
    private int roomID;
    private double baseTicketPrice;
    private Timestamp startTimestamp;
    private Timestamp endTimestamp;
    private String status;

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat DATETIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public Schedule() {
    }

    /**
     * New constructor using Timestamp directly.
     */
    public Schedule(int scheduleID, int movieID, int roomID,
                    double baseTicketPrice, Timestamp startTimestamp,
                    Timestamp endTimestamp, String status) {
        this.scheduleID = scheduleID;
        this.movieID = movieID;
        this.roomID = roomID;
        this.baseTicketPrice = baseTicketPrice;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.status = status;
    }

    /**
     * Backward-compatible constructor using separate date and time strings.
     * Combines date + time into Timestamp objects.
     */
    public Schedule(int scheduleID, int movieID, int roomID,
                    double baseTicketPrice, String showDate, String startTime,
                    String endTime, String endDate, String status) {
        this.scheduleID = scheduleID;
        this.movieID = movieID;
        this.roomID = roomID;
        this.baseTicketPrice = baseTicketPrice;
        this.startTimestamp = toTimestamp(showDate, startTime);
        this.endTimestamp = toTimestamp(endDate, endTime);
        this.status = status;
    }

    // ===== Core Timestamp getters/setters =====

    public Timestamp getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Timestamp startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Timestamp getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Timestamp endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    // ===== Backward-compatible String getters =====

    /** Returns date part of start timestamp as "yyyy-MM-dd" */
    public String getShowDate() {
        if (startTimestamp == null) return null;
        synchronized (DATE_FMT) { return DATE_FMT.format(startTimestamp); }
    }

    /** Returns time part of start timestamp as "HH:mm" */
    public String getStartTime() {
        if (startTimestamp == null) return null;
        synchronized (TIME_FMT) { return TIME_FMT.format(startTimestamp); }
    }

    /** Returns time part of end timestamp as "HH:mm" */
    public String getEndTime() {
        if (endTimestamp == null) return null;
        synchronized (TIME_FMT) { return TIME_FMT.format(endTimestamp); }
    }

    /** Returns date part of end timestamp as "yyyy-MM-dd" */
    public String getEndDate() {
        if (endTimestamp == null) return null;
        synchronized (DATE_FMT) { return DATE_FMT.format(endTimestamp); }
    }

    // ===== Backward-compatible String setters =====

    public void setShowDate(String showDate) {
        this.startTimestamp = toTimestamp(showDate, getStartTime());
    }

    public void setStartTime(String startTime) {
        this.startTimestamp = toTimestamp(getShowDate(), startTime);
    }

    public void setEndTime(String endTime) {
        this.endTimestamp = toTimestamp(getEndDate(), endTime);
    }

    public void setEndDate(String endDate) {
        this.endTimestamp = toTimestamp(endDate, getEndTime());
    }

    // ===== Other getters/setters =====

    public int getScheduleID() {
        return scheduleID;
    }

    public void setScheduleID(int scheduleID) {
        this.scheduleID = scheduleID;
    }

    public int getMovieID() {
        return movieID;
    }

    public void setMovieID(int movieID) {
        this.movieID = movieID;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public double getBaseTicketPrice() {
        return baseTicketPrice;
    }

    public void setBaseTicketPrice(double baseTicketPrice) {
        this.baseTicketPrice = baseTicketPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ===== Helper =====

    private static Timestamp toTimestamp(String date, String time) {
        if (date == null || date.isEmpty()) return null;
        try {
            String timePart = (time != null) ? time : "00:00";
            if (timePart.contains(".")) {
                timePart = timePart.substring(0, timePart.indexOf('.'));
            }
            if (timePart.length() > 5) {
                timePart = timePart.substring(0, 5);
            }
            synchronized (DATETIME_FMT) {
                return new Timestamp(DATETIME_FMT.parse(date + " " + timePart).getTime());
            }
        } catch (Exception e) {
            return null;
        }
    }
}
