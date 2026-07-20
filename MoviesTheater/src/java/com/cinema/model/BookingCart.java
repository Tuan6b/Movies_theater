/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BookingCart holds the current booking session state (seats and pricing).
 *
 * @author tuan6b
 */
public class BookingCart {
    private int scheduleId;
    private List<Integer> seatIds;
    private List<String> seatNames;
    private double ticketTotal;

    // Food items: foodId -> quantity
    private Map<Integer, Integer> foodQuantities = new HashMap<>();
    private double foodTotal;

    // Per-seat prices: seatId -> price
    private java.util.Map<Integer, Double> seatPrices = new java.util.HashMap<>();

    // Applied promotion
    private Integer appliedPromotionId;
    private String appliedPromotionCode;
    private double discountAmount;
    private double finalTotal;

    public java.util.Map<Integer, Double> getSeatPrices() {
        return seatPrices;
    }

    public void setSeatPrices(java.util.Map<Integer, Double> seatPrices) {
        this.seatPrices = seatPrices;
    }

    public Integer getAppliedPromotionId() {
        return appliedPromotionId;
    }

    public void setAppliedPromotionId(Integer appliedPromotionId) {
        this.appliedPromotionId = appliedPromotionId;
    }

    public String getAppliedPromotionCode() {
        return appliedPromotionCode;
    }

    public void setAppliedPromotionCode(String appliedPromotionCode) {
        this.appliedPromotionCode = appliedPromotionCode;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getFinalTotal() {
        return finalTotal > 0 ? finalTotal : getGrandTotal();
    }

    public void setFinalTotal(double finalTotal) {
        this.finalTotal = finalTotal;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public List<Integer> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Integer> seatIds) {
        this.seatIds = seatIds;
    }

    public List<String> getSeatNames() {
        return seatNames;
    }

    public void setSeatNames(List<String> seatNames) {
        this.seatNames = seatNames;
    }

    public double getTicketTotal() {
        return ticketTotal;
    }

    public void setTicketTotal(double ticketTotal) {
        this.ticketTotal = ticketTotal;
    }

    public Map<Integer, Integer> getFoodQuantities() {
        return foodQuantities;
    }

    public void setFoodQuantities(Map<Integer, Integer> foodQuantities) {
        this.foodQuantities = foodQuantities;
    }

    public double getFoodTotal() {
        return foodTotal;
    }

    public void setFoodTotal(double foodTotal) {
        this.foodTotal = foodTotal;
    }

    public double getGrandTotal() {
        return ticketTotal + foodTotal;
    }
}
