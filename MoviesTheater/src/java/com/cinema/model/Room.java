package com.cinema.model;

/**
 * Room model class represents a cinema room entity. Used to store information
 * about a movie room in the system.
 *
 * @author Tuan Phong Nguyen
 */
public class Room {

    // Unique identifier for the room
    private int roomId;

    // Room number/code (e.g., R01, A12)
    private String roomNumber;

    // Type of room (e.g., Standard, VIP, IMAX)
    private String roomType;

    // Maximum seating capacity of the room
    private int capacity;

    // Status of the room (true = active, false = inactive)
    private boolean active;

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

        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.active = active;
        this.numberOfRows = numberOfRows;
        this.seatsPerRow = seatsPerRow;
    }

    // Getter for roomId
    public int getRoomId() {
        return roomId;
    }

    // Setter for roomId
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    // Getter for roomNumber
    public String getRoomNumber() {
        return roomNumber;
    }

    // Setter for roomNumber
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    // Getter for roomType
    public String getRoomType() {
        return roomType;
    }

    // Setter for roomType
    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    // Getter for capacity
    public int getCapacity() {
        return capacity;
    }

    /**
     * Sets room capacity Note: should be validated (> 0) in service/controller
     * layer
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    // Getter for active status
    public boolean isActive() {
        return active;
    }

    // Setter for active status
    public void setActive(boolean active) {
        this.active = active;
    }

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
}
