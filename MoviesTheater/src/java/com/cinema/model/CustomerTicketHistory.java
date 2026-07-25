package com.cinema.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only view model used by the customer "My Tickets" pages.
 * One instance represents one paid/refunded invoice and all tickets that
 * belong to that booking.
 */
public class CustomerTicketHistory {

    private int invoiceId;
    private int scheduleId;
    private int movieId;
    private String movieName;
    private String poster;
    private Timestamp startTime;
    private Timestamp endTime;
    private String scheduleStatus;
    private String roomNumber;
    private String roomType;
    private double subTotal;
    private double discountAmount;
    private double totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private Timestamp createdAt;
    private String transactionRef;
    private String bankCode;
    private String payDate;
    private boolean valid;
    private boolean allCheckedIn;
    private String bookingCode;
    private String qrDataUri;
    private double ticketTotal;
    private double foodTotal;
    private final List<TicketItem> tickets = new ArrayList<>();
    private final List<FoodItem> foods = new ArrayList<>();

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
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

    public String getScheduleStatus() {
        return scheduleStatus;
    }

    public void setScheduleStatus(String scheduleStatus) {
        this.scheduleStatus = scheduleStatus;
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

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getPayDate() {
        return payDate;
    }

    public void setPayDate(String payDate) {
        this.payDate = payDate;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isAllCheckedIn() {
        return allCheckedIn;
    }

    public void setAllCheckedIn(boolean allCheckedIn) {
        this.allCheckedIn = allCheckedIn;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public String getQrDataUri() {
        return qrDataUri;
    }

    public void setQrDataUri(String qrDataUri) {
        this.qrDataUri = qrDataUri;
    }

    public double getTicketTotal() {
        return ticketTotal;
    }

    public void setTicketTotal(double ticketTotal) {
        this.ticketTotal = ticketTotal;
    }

    public double getFoodTotal() {
        return foodTotal;
    }

    public void setFoodTotal(double foodTotal) {
        this.foodTotal = foodTotal;
    }

    public List<TicketItem> getTickets() {
        return tickets;
    }

    public List<FoodItem> getFoods() {
        return foods;
    }

    public int getTicketCount() {
        return tickets.size();
    }

    public String getSeatNames() {
        StringBuilder result = new StringBuilder();
        for (TicketItem ticket : tickets) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(ticket.getSeatName());
        }
        return result.toString();
    }

    public static class TicketItem {

        private int ticketId;
        private int seatId;
        private String seatName;
        private String seatType;
        private double price;
        private String code;
        private boolean checkedIn;
        private Timestamp checkedInAt;

        public int getTicketId() {
            return ticketId;
        }

        public void setTicketId(int ticketId) {
            this.ticketId = ticketId;
        }

        public int getSeatId() {
            return seatId;
        }

        public void setSeatId(int seatId) {
            this.seatId = seatId;
        }

        public String getSeatName() {
            return seatName;
        }

        public void setSeatName(String seatName) {
            this.seatName = seatName;
        }

        public String getSeatType() {
            return seatType;
        }

        public void setSeatType(String seatType) {
            this.seatType = seatType;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public boolean isCheckedIn() {
            return checkedIn;
        }

        public void setCheckedIn(boolean checkedIn) {
            this.checkedIn = checkedIn;
        }

        public Timestamp getCheckedInAt() {
            return checkedInAt;
        }

        public void setCheckedInAt(Timestamp checkedInAt) {
            this.checkedInAt = checkedInAt;
        }
    }

    public static class FoodItem {

        private int foodId;
        private String foodName;
        private int quantity;
        private double priceAtBooking;

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

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getPriceAtBooking() {
            return priceAtBooking;
        }

        public void setPriceAtBooking(double priceAtBooking) {
            this.priceAtBooking = priceAtBooking;
        }

        public double getLineTotal() {
            return priceAtBooking * quantity;
        }
    }
}
    