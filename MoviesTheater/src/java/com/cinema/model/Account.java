package com.cinema.model;

import java.time.LocalDateTime;

public class Account {

    private int accountId;
    private String email;
    private String password;
    private int roleId;
    private String roleName;
    private boolean isBlocked;
    private LocalDateTime createdAt;
    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    private UserProfile profile;

    public Account() {
    }

    public Account(int accountId, String email, String password, int roleId,
            String roleName, boolean isBlocked, LocalDateTime createdAt) {
        this.accountId = accountId;
        this.email = email;
        this.password = password;
        this.roleId = roleId;
        this.roleName = roleName;
        this.isBlocked = isBlocked;
        this.createdAt = createdAt;
    }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }

    public UserProfile getProfile() { return profile; }
    public void setProfile(UserProfile profile) { this.profile = profile; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public boolean isIsBlocked() { return isBlocked; }
    public void setIsBlocked(boolean isBlocked) { this.isBlocked = isBlocked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
