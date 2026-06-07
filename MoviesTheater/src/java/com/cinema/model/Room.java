package com.cinema.model;

/**
 * Room model class represents a cinema room entity.
 * Used to store information about a movie room in the system.
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

    /**
     * Default constructor
     */
    public Room() {
    }

    /**
     * Parameterized constructor to initialize all fields
     *
     * @param roomId      unique room ID
     * @param roomNumber   room number/code
     * @param roomType     type of room
     * @param capacity     seating capacity (must be > 0 in business logic)
     * @param active       room status
     */
    public Room(int roomId, String roomNumber,
                String roomType, int capacity,
                boolean active) {

        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.active = active;
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
     * Sets room capacity
     * Note: should be validated (> 0) in service/controller layer
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

}


