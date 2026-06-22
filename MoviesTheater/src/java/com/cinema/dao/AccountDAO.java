package com.cinema.dao;

import com.cinema.model.Account;
import com.cinema.util.DBUtils;
import com.cinema.util.PasswordHash;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class AccountDAO {

    public Account login(String email, String password) {
        String sql = "SELECT a.AccountID, a.Email, a.Password, a.RoleID, a.IsBlocked, a.AccountStatus, a.CreatedAt, "
                + "r.RoleName, u.FullName, u.PhoneNumber "
                + "FROM Account a "
                + "JOIN Role r ON a.RoleID = r.RoleID "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "WHERE a.Email = ?";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("Password");
                    if (PasswordHash.verify(password, storedHash)) {
                        return mapAccount(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("[LOGIN_DEBUG] SQLException: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean isEmailExist(String email) {
        String sql = "SELECT COUNT(*) FROM Account WHERE Email = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int register(Account account) {
        String sqlAccount = "INSERT INTO Account (Email, Password, RoleID, IsBlocked) VALUES (?, ?, ?, 0)";
        String sqlProfile = "INSERT INTO UserProfile (AccountID, FullName, PhoneNumber) VALUES (?, ?, ?)";

        try (Connection conn = DBUtils.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlAccount, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, account.getEmail());
                ps.setString(2, PasswordHash.hash(account.getPassword()));
                ps.setInt(3, account.getRoleId() > 0 ? account.getRoleId() : 2);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int accountId = keys.getInt(1);
                        try (PreparedStatement psProfile = conn.prepareStatement(sqlProfile)) {
                            psProfile.setInt(1, accountId);
                            psProfile.setNString(2, account.getFullName());
                            psProfile.setString(3, account.getPhoneNumber());
                            psProfile.executeUpdate();
                        }
                        conn.commit();
                        return accountId;
                    }
                }
            }
            conn.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Account getAccountById(int accountId) {
        String sql = "SELECT a.AccountID, a.Email, a.Password, a.RoleID, a.IsBlocked, a.AccountStatus, a.CreatedAt, "
                + "r.RoleName, u.FullName, u.PhoneNumber "
                + "FROM Account a "
                + "JOIN Role r ON a.RoleID = r.RoleID "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "WHERE a.AccountID = ?";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAccount(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Account getAccountByEmail(String email) {
        String sql = "SELECT a.AccountID, a.Email, a.Password, a.RoleID, a.IsBlocked, a.AccountStatus, a.CreatedAt, "
                + "r.RoleName, u.FullName, u.PhoneNumber "
                + "FROM Account a "
                + "JOIN Role r ON a.RoleID = r.RoleID "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "WHERE a.Email = ?";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAccount(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updatePassword(int accountId, String newPassword) {
        String sql = "UPDATE Account SET Password = ? WHERE AccountID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PasswordHash.hash(newPassword));
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateResetToken(String email, String token, LocalDateTime expiry) {
        String sql = "UPDATE Account SET ResetToken = ?, ResetTokenExpiry = ? WHERE Email = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setTimestamp(2, Timestamp.valueOf(expiry));
            ps.setString(3, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Account getAccountByResetToken(String token) {
        String sql = "SELECT a.AccountID, a.Email, a.Password, a.RoleID, a.IsBlocked, a.AccountStatus, a.CreatedAt, "
                + "r.RoleName, u.FullName, u.PhoneNumber "
                + "FROM Account a "
                + "JOIN Role r ON a.RoleID = r.RoleID "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "WHERE a.ResetToken = ? AND a.ResetTokenExpiry > GETDATE()";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAccount(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean clearResetToken(int accountId) {
        String sql = "UPDATE Account SET ResetToken = NULL, ResetTokenExpiry = NULL WHERE AccountID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean clearNeedsSetup(int accountId) {
        String sql = "UPDATE Account SET AccountStatus = 'active' WHERE AccountID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Account mapAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setAccountId(rs.getInt("AccountID"));
        account.setEmail(rs.getString("Email"));
        account.setPassword(rs.getString("Password"));
        account.setRoleId(rs.getInt("RoleID"));
        account.setRoleName(rs.getNString("RoleName"));
        account.setIsBlocked(rs.getBoolean("IsBlocked"));
        account.setNeedsSetup("pending".equals(rs.getString("AccountStatus")));

        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) {
            account.setCreatedAt(ts.toLocalDateTime());
        }

        account.setFullName(rs.getNString("FullName"));
        account.setPhoneNumber(rs.getString("PhoneNumber"));
        return account;
    }
}
