/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.model;

import java.sql.Timestamp;

/**
 * Model class representing the Schedule entity.
 * Uses the cls prefix following the project standards.
 *
 * @author TBinh
 */
public class clsSchedule {

    private int scheduleId;
    private int roomId;
    private int movieId;
    private Timestamp startTime;
    private Timestamp endTime;
    private double baseTicketPrice;
    private String status;

    // Helper references to bind related room and movie details
    private clsMovie movie;
    private Room room;

    /**
     * Default constructor.
     */
    public clsSchedule() {
    }

    /**
     * Full constructor.
     *
     * @param scheduleId ID of the schedule
     * @param roomId ID of the room
     * @param movieId ID of the movie
     * @param startTime Start time of showtime
     * @param endTime End time of showtime
     * @param baseTicketPrice Standard price of ticket
     * @param status Status of showtime
     */
    public clsSchedule(int scheduleId, int roomId, int movieId, Timestamp startTime,
            Timestamp endTime, double baseTicketPrice, String status) {
        this.scheduleId = scheduleId;
        this.roomId = roomId;
        this.movieId = movieId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.baseTicketPrice = baseTicketPrice;
        this.status = status;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
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

    public clsMovie getMovie() {
        return movie;
    }

    public void setMovie(clsMovie movie) {
        this.movie = movie;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public String toString() {
        return "clsSchedule{" + "scheduleId=" + scheduleId + ", startTime=" + startTime + ", baseTicketPrice=" + baseTicketPrice + '}';
    }
}
