/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.model;

<<<<<<< Updated upstream
=======
/**
 * Room model class represents a cinema room entity. Used to store information
 * about a movie room in the system.
 *
 * @author Tuan Phong Nguyen
 */
>>>>>>> Stashed changes
public class Room {

    private int roomId;
    private String roomNumber;
    private String roomType;
    private int capacity;
    private boolean active;

<<<<<<< Updated upstream
    public Room() {
    }

    public Room(int roomId, String roomNumber,
            String roomType, int capacity,
            boolean active) {
=======
    /*
     * Total number of seat rows in the room
     * Example:
     * 5 rows -> A, B, C, D, E
     */
    private int numberOfRows;

    /*
     * Number of seats in each row
     * Example:
     * 8 seats -> A1 to A8
     */
    private int seatsPerRow;

    /**
     * Default constructor
     */
    public Room() {
    }

    /**
     * Parameterized constructor to initialize all fields
     *
     * @param roomId unique room ID
     * @param roomNumber room number/code
     * @param roomType type of room
     * @param capacity seating capacity (must be > 0 in business logic)
     * @param active room status
     * @param numberOfRows total row count
     * @param seatsPerRow seats per row
     */
    public Room(int roomId, String roomNumber,
            String roomType, int capacity,
            boolean active,
            int numberOfRows,
            int seatsPerRow) {
>>>>>>> Stashed changes

        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.active = active;
        this.numberOfRows = numberOfRows;
        this.seatsPerRow = seatsPerRow;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public int getCapacity() {
        return capacity;
    }

<<<<<<< Updated upstream
=======
    /**
     * Sets room capacity Note: should be validated (> 0) in service/controller
     * layer
     */
>>>>>>> Stashed changes
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
<<<<<<< Updated upstream
=======

    /**
     * Get number of rows in room
     *
     * @return total row count
     */
    public int getNumberOfRows() {
        return numberOfRows;
    }

    /**
     * Set number of rows in room
     *
     * @param numberOfRows total row count
     */
    public void setNumberOfRows(int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    /**
     * Get number of seats per row
     *
     * @return seats per row
     */
    public int getSeatsPerRow() {
        return seatsPerRow;
    }

    /**
     * Set number of seats per row
     *
     * @param seatsPerRow seats per row
     */
    public void setSeatsPerRow(int seatsPerRow) {
        this.seatsPerRow = seatsPerRow;
    }
>>>>>>> Stashed changes
}
