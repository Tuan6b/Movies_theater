/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.dao;

import com.cinema.model.Account;
import com.cinema.util.DBUtils;
import com.cinema.util.PasswordHash;
import java.sql.Connection;
import java.sql.Date;
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

    private static final String BASE_SELECT =
            "SELECT a.AccountID, a.Email, a.RoleID, a.IsBlocked, a.CreatedAt, "
            + "r.RoleName, u.FullName, u.PhoneNumber, u.Address, u.DoB, "
            + "COALESCE((SELECT COUNT(*) FROM WorkShift ws WHERE ws.EmployeeID = a.AccountID AND ws.Status = 'Completed'), 0) AS WorkingDays "
            + "FROM Account a "
            + "JOIN Role r ON a.RoleID = r.RoleID "
            + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
            + "WHERE a.RoleID = " + ROLE_EMPLOYEE;

    private String safeOrderBy(String sortField, String sortDir) {
        String dir = "DESC".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        switch (sortField != null ? sortField : "") {
            case "name":    return "u.FullName " + dir;
            case "email":   return "a.Email " + dir;
            case "status":  return "a.IsBlocked " + dir;
            case "created": return "a.CreatedAt " + dir;
            default:        return "a.AccountID DESC";
        }
    }

    public List<Account> getAll(String keyword, int page, int pageSize, String sortField, String sortDir) {
        List<Account> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT);

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (a.Email LIKE ? OR u.FullName LIKE ?)");
        }
        sql.append(" ORDER BY ").append(safeOrderBy(sortField, sortDir));
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
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Account getById(int accountId) {
        String sql = BASE_SELECT + " AND a.AccountID = ?";

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapAccount(rs);
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
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int add(Account account) {
        // Generate a random password; store plaintext back in account so servlet can show it once
        String plainPassword = generatePassword(8);
        String sqlAccount = "INSERT INTO Account (Email, Password, RoleID, IsBlocked, AccountStatus) VALUES (?, ?, ?, 0, 'pending')";
        String sqlProfile  = "INSERT INTO UserProfile (AccountID, FullName, PhoneNumber, Address, DoB) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBUtils.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlAccount, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, account.getEmail());
                ps.setString(2, PasswordHash.hash(plainPassword));
                ps.setInt(3, ROLE_EMPLOYEE);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newId = keys.getInt(1);
                        try (PreparedStatement psP = conn.prepareStatement(sqlProfile)) {
                            psP.setInt(1, newId);
                            psP.setNString(2, account.getFullName());
                            psP.setString(3, account.getPhoneNumber());
                            psP.setNString(4, account.getAddress());
                            if (account.getDateOfBirth() != null && !account.getDateOfBirth().isEmpty()) {
                                psP.setDate(5, Date.valueOf(account.getDateOfBirth()));
                            } else {
                                psP.setNull(5, java.sql.Types.DATE);
                            }
                            psP.executeUpdate();
                        }
                        conn.commit();
                        account.setPassword(plainPassword);
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

    /**
     * Generates a random password using an alphabet with the look-alike characters
     * (I, l, O, 0, 1) removed, since temp passwords get read out loud or copied by
     * hand. Public because the counter-sales flow needs the same generator when it
     * creates a walk-in customer account.
     */
    public static String generatePassword(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        java.security.SecureRandom rng = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rng.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Updates an employee account and its profile.
     *
     * BR-14: every statement is scoped to RoleID = ROLE_EMPLOYEE, matching the rest
     * of this DAO (getById, setBlocked, resetPassword). Without that scope a
     * hand-edited accountId in the edit form would let a Manager rewrite the email
     * and password of a Manager or Admin row. The Account update runs first and its
     * row count doubles as the authorisation check for the UserProfile writes that
     * follow, so a non-employee id aborts before any profile data is touched.
     */
    public boolean update(Account account) {
        try (Connection conn = DBUtils.getConnection()) {
            conn.setAutoCommit(false);

            int accountRows;
            if (account.getPassword() != null && !account.getPassword().trim().isEmpty()) {
                String sqlAccount = "UPDATE Account SET Email = ?, Password = ? "
                        + "WHERE AccountID = ? AND RoleID = " + ROLE_EMPLOYEE;
                try (PreparedStatement ps = conn.prepareStatement(sqlAccount)) {
                    ps.setString(1, account.getEmail());
                    ps.setString(2, PasswordHash.hash(account.getPassword().trim()));
                    ps.setInt(3, account.getAccountId());
                    accountRows = ps.executeUpdate();
                }
            } else {
                String sqlAccount = "UPDATE Account SET Email = ? "
                        + "WHERE AccountID = ? AND RoleID = " + ROLE_EMPLOYEE;
                try (PreparedStatement ps = conn.prepareStatement(sqlAccount)) {
                    ps.setString(1, account.getEmail());
                    ps.setInt(2, account.getAccountId());
                    accountRows = ps.executeUpdate();
                }
            }

            // Target is not an employee row (or does not exist): abort before the
            // profile writes, which have no RoleID column of their own to filter on.
            if (accountRows == 0) {
                conn.rollback();
                return false;
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
                String sqlProfile = "UPDATE UserProfile SET FullName = ?, PhoneNumber = ?, Address = ?, DoB = ? WHERE AccountID = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlProfile)) {
                    ps.setNString(1, account.getFullName());
                    ps.setString(2, account.getPhoneNumber());
                    ps.setNString(3, account.getAddress());
                    if (account.getDateOfBirth() != null && !account.getDateOfBirth().isEmpty()) {
                        ps.setDate(4, Date.valueOf(account.getDateOfBirth()));
                    } else {
                        ps.setNull(4, java.sql.Types.DATE);
                    }
                    ps.setInt(5, account.getAccountId());
                    ps.executeUpdate();
                }
            } else {
                String sqlProfile = "INSERT INTO UserProfile (AccountID, FullName, PhoneNumber, Address, DoB) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlProfile)) {
                    ps.setInt(1, account.getAccountId());
                    ps.setNString(2, account.getFullName());
                    ps.setString(3, account.getPhoneNumber());
                    ps.setNString(4, account.getAddress());
                    if (account.getDateOfBirth() != null && !account.getDateOfBirth().isEmpty()) {
                        ps.setDate(5, Date.valueOf(account.getDateOfBirth()));
                    } else {
                        ps.setNull(5, java.sql.Types.DATE);
                    }
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

    public boolean resetPassword(int accountId, String newPassword) {
        String sql = "UPDATE Account SET Password = ? WHERE AccountID = ? AND RoleID = " + ROLE_EMPLOYEE;
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
        if (ts != null) account.setCreatedAt(ts.toLocalDateTime());

        account.setFullName(rs.getNString("FullName"));
        account.setPhoneNumber(rs.getString("PhoneNumber"));
        account.setAddress(rs.getNString("Address"));

        Date dob = rs.getDate("DoB");
        if (dob != null) account.setDateOfBirth(dob.toString());

        account.setWorkingDays(rs.getInt("WorkingDays"));
        return account;
    }
}
