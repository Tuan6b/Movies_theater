package com.cinema.model;

public class Food {
    private int foodId;
    private String foodName;
    private double price;
    private String image;
    private boolean isActive;
    private boolean isCombo;

    public Food() {
    }

    public Food(int foodId, String foodName, double price, String image, boolean isActive, boolean isCombo) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.price = price;
        this.image = image;
        this.isActive = isActive;
        this.isCombo = isCombo;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isCombo() {
        return isCombo;
    }

    public void setCombo(boolean isCombo) {
        this.isCombo = isCombo;
    }

    @Override
    public String toString() {
        return "Food{" + "foodId=" + foodId + ", foodName=" + foodName + ", price=" + price + '}';
    }
}
