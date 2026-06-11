/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tuan6b
 */
public class EmployeeDAO {

    private static final int ROLE_EMPLOYEE = 3;

    public List<Account> getAll(String keyword, int page, int pageSize) {
        List<Account> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.AccountID, a.Email, a.RoleID, a.IsBlocked, a.CreatedAt, "
                + "r.RoleName, u.FullName, u.PhoneNumber "
                + "FROM Account a "
                + "JOIN Role r ON a.RoleID = r.RoleID "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "WHERE a.RoleID = " + ROLE_EMPLOYEE);

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (a.Email LIKE ? OR u.FullName LIKE ?)");
        }
        sql.append(" ORDER BY a.AccountID DESC");
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(idx++, like);
                ps.setNString(idx++, like);
            }
            ps.setInt(idx++, (page - 1) * pageSize);
            ps.setInt(idx, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAccount(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countAll(String keyword) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM Account a "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "WHERE a.RoleID = " + ROLE_EMPLOYEE);

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (a.Email LIKE ? OR u.FullName LIKE ?)");
        }

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(1, like);
                ps.setNString(2, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Account getById(int accountId) {
        String sql = "SELECT a.AccountID, a.Email, a.RoleID, a.IsBlocked, a.CreatedAt, "
                + "r.RoleName, u.FullName, u.PhoneNumber "
                + "FROM Account a "
                + "JOIN Role r ON a.RoleID = r.RoleID "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "WHERE a.AccountID = ? AND a.RoleID = " + ROLE_EMPLOYEE;

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

    public boolean isEmailExist(String email, int excludeAccountId) {
        String sql = "SELECT COUNT(*) FROM Account WHERE Email = ? AND AccountID != ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, excludeAccountId);
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

    public int add(Account account) {
        String sqlAccount = "INSERT INTO Account (Email, Password, RoleID, IsBlocked) VALUES (?, ?, ?, 0)";
        String sqlProfile = "INSERT INTO UserProfile (AccountID, FullName, PhoneNumber) VALUES (?, ?, ?)";

        try (Connection conn = DBUtils.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlAccount, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, account.getEmail());
                ps.setString(2, PasswordHash.hash(account.getPassword()));
                ps.setInt(3, ROLE_EMPLOYEE);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newId = keys.getInt(1);
                        try (PreparedStatement psProfile = conn.prepareStatement(sqlProfile)) {
                            psProfile.setInt(1, newId);
                            psProfile.setNString(2, account.getFullName());
                            psProfile.setString(3, account.getPhoneNumber());
                            psProfile.executeUpdate();
                        }
                        conn.commit();
                        return newId;
                    }
                }
            }
            conn.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(Account account) {
        try (Connection conn = DBUtils.getConnection()) {
            conn.setAutoCommit(false);

            if (account.getPassword() != null && !account.getPassword().isEmpty()) {
                String sqlAccount = "UPDATE Account SET Email = ?, Password = ? WHERE AccountID = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlAccount)) {
                    ps.setString(1, account.getEmail());
                    ps.setString(2, PasswordHash.hash(account.getPassword()));
                    ps.setInt(3, account.getAccountId());
                    ps.executeUpdate();
                }
            } else {
                String sqlAccount = "UPDATE Account SET Email = ? WHERE AccountID = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlAccount)) {
                    ps.setString(1, account.getEmail());
                    ps.setInt(2, account.getAccountId());
                    ps.executeUpdate();
                }
            }

            String sqlCheck = "SELECT COUNT(*) FROM UserProfile WHERE AccountID = ?";
            boolean profileExists;
            try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                ps.setInt(1, account.getAccountId());
                try (ResultSet rs = ps.executeQuery()) {
                    profileExists = rs.next() && rs.getInt(1) > 0;
                }
            }

            if (profileExists) {
                String sqlProfile = "UPDATE UserProfile SET FullName = ?, PhoneNumber = ? WHERE AccountID = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlProfile)) {
                    ps.setNString(1, account.getFullName());
                    ps.setString(2, account.getPhoneNumber());
                    ps.setInt(3, account.getAccountId());
                    ps.executeUpdate();
                }
            } else {
                String sqlProfile = "INSERT INTO UserProfile (AccountID, FullName, PhoneNumber) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlProfile)) {
                    ps.setInt(1, account.getAccountId());
                    ps.setNString(2, account.getFullName());
                    ps.setString(3, account.getPhoneNumber());
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setBlocked(int accountId, boolean blocked) {
        String sql = "UPDATE Account SET IsBlocked = ? WHERE AccountID = ? AND RoleID = " + ROLE_EMPLOYEE;
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, blocked);
            ps.setInt(2, accountId);
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
        account.setRoleId(rs.getInt("RoleID"));
        account.setRoleName(rs.getNString("RoleName"));
        account.setIsBlocked(rs.getBoolean("IsBlocked"));

        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) {
            account.setCreatedAt(ts.toLocalDateTime());
        }

        account.setFullName(rs.getNString("FullName"));
        account.setPhoneNumber(rs.getString("PhoneNumber"));
        return account;
    }
}
