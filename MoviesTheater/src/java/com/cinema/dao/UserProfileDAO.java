package com.cinema.dao;

import com.cinema.model.UserProfile;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserProfileDAO {

    public UserProfile getProfileByAccountId(int accountId) {
        String sql = "SELECT AccountID, FullName, PhoneNumber, DoB, Address, AvatarURL FROM UserProfile WHERE AccountID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapProfile(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateAvatar(int accountId, String avatarUrl) {
        String sql = "UPDATE UserProfile SET AvatarURL = ? WHERE AccountID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, avatarUrl);
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateProfile(int accountId, String fullName, String phoneNumber, String dob, String address) {
        String sql = "UPDATE UserProfile SET FullName = ?, PhoneNumber = ?, DoB = ?, Address = ? WHERE AccountID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, fullName);
            ps.setString(2, phoneNumber);
            if (dob != null && !dob.isEmpty()) {
                ps.setDate(3, java.sql.Date.valueOf(dob));
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }
            ps.setNString(4, address);
            ps.setInt(5, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private UserProfile mapProfile(ResultSet rs) throws SQLException {
        UserProfile profile = new UserProfile();
        profile.setAccountId(rs.getInt("AccountID"));
        profile.setFullName(rs.getNString("FullName"));
        profile.setPhoneNumber(rs.getString("PhoneNumber"));
        java.sql.Date dob = rs.getDate("DoB");
        profile.setDob(dob != null ? dob.toString() : null);
        profile.setAddress(rs.getNString("Address"));
        profile.setAvatarUrl(rs.getString("AvatarURL"));
        return profile;
    }
}
