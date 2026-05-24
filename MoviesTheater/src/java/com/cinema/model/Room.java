/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.model;

/**
 * Represents a cinema room entity mapped to the Room table in the database.
 * Follows standard JavaBean conventions with encapsulation.
 * * @author Tuan Phong Nguyen
 */
public class Room {
    
    private int roomId;
    private String roomNumber;
    private String roomType;
    private int capacity;
    private boolean isActive;

    /**
     * Default constructor required for JavaBean standard.
     */
    public Room() {
    }

    /**
     * Overloaded constructor to initialize a complete Room object.
     * * @param roomId     the unique identifier of the room
     * @param roomNumber the display name or number of the room
     * @param roomType   the type format (e.g., 2D, 3D, IMAX)
     * @param capacity   the total number of seats configured
     * @param isActive   the operational status of the room
     */
    public Room(int roomId, String roomNumber, String roomType, int capacity, boolean isActive) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.isActive = isActive;
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

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
