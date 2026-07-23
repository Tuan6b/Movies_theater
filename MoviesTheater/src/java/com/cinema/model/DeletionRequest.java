package com.cinema.model;

import java.time.LocalDateTime;

public class DeletionRequest {

    private int requestId;
    private int accountId;
    private String reason;
    private String status;
    private Integer reviewedBy;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    public DeletionRequest() {}

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Integer reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
