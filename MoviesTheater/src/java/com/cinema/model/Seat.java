/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.model;

/**
 * Seat model class represents a seat in a cinema room.
 *
 * @author Tuan Phong Nguyen
 */
public class Seat {

    // Unique seat ID
    private int seatId;

    // Room ID that owns this seat
    private int roomId;

    // Seat row character (A, B, C...)
    private String rowChar;

    // Seat column number
    private int colNumber;

    // Seat type (Normal, VIP, Couple)
    private String seatType;

    // Seat status
    private boolean active;

    /**
     * Default constructor
     */
    public Seat() {
    }

    /**
     * Full constructor
     */
    public Seat(int seatId,
            int roomId,
            String rowChar,
            int colNumber,
            String seatType,
            boolean active) {

        this.seatId = seatId;
        this.roomId = roomId;
        this.rowChar = rowChar;
        this.colNumber = colNumber;
        this.seatType = seatType;
        this.active = active;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRowChar() {
        return rowChar;
    }

    public void setRowChar(String rowChar) {
        this.rowChar = rowChar;
    }

    public int getColNumber() {
        return colNumber;
    }

    public void setColNumber(int colNumber) {
        this.colNumber = colNumber;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
