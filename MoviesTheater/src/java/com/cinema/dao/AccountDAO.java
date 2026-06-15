package com.cinema.dao;

import com.cinema.model.Account;
import com.cinema.model.UserProfile;
import com.cinema.util.DBUtils;
import com.cinema.util.PasswordHash;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    public Account login(String email, String password) {
        String sql = "SELECT a.AccountID, a.Email, a.Password, a.RoleID, a.IsBlocked, a.CreatedAt, "
                + "r.RoleName, u.FullName, u.PhoneNumber, u.DoB, u.Address, u.AvatarURL "
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
                            UserProfile p = account.getProfile();
                            psProfile.setNString(2, p != null ? p.getFullName() : null);
                            psProfile.setString(3, p != null ? p.getPhoneNumber() : null);
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
        String sql = "SELECT a.AccountID, a.Email, a.Password, a.RoleID, a.IsBlocked, a.CreatedAt, "
                + "r.RoleName, u.FullName, u.PhoneNumber, u.DoB, u.Address, u.AvatarURL "
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
        String sql = "SELECT a.AccountID, a.Email, a.Password, a.RoleID, a.IsBlocked, a.CreatedAt, "
                + "r.RoleName, u.FullName, u.PhoneNumber, u.DoB, u.Address, u.AvatarURL "
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
        String sql = "SELECT a.AccountID, a.Email, a.Password, a.RoleID, a.IsBlocked, a.CreatedAt, "
                + "r.RoleName, u.FullName, u.PhoneNumber, u.DoB, u.Address, u.AvatarURL "
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

    public List<Account> getAllAccounts(int page, int pageSize, String search, String roleFilter) {
        StringBuilder sql = new StringBuilder(
                "SELECT a.AccountID, a.Email, a.Password, a.RoleID, a.IsBlocked, a.CreatedAt, "
                + "r.RoleName, u.FullName, u.PhoneNumber, u.DoB, u.Address, u.AvatarURL "
                + "FROM Account a "
                + "JOIN Role r ON a.RoleID = r.RoleID "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (a.Email LIKE ? OR u.FullName LIKE ?)");
            String like = "%" + search.trim() + "%";
            params.add(like);
            params.add(like);
        }

        if (roleFilter != null && !roleFilter.trim().isEmpty()) {
            if ("ADMIN".equalsIgnoreCase(roleFilter)) {
                sql.append(" AND r.RoleName = 'Admin'");
            } else if ("MANAGER".equalsIgnoreCase(roleFilter)) {
                sql.append(" AND r.RoleName = 'Manager'");
            } else if ("STAFF".equalsIgnoreCase(roleFilter)) {
                sql.append(" AND r.RoleName = 'Employee'");
            } else if ("CUSTOMER".equalsIgnoreCase(roleFilter)) {
                sql.append(" AND r.RoleName = 'Customer'");
            }
        }

        sql.append(" ORDER BY a.CreatedAt DESC");
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add((page - 1) * pageSize);
        params.add(pageSize);

        List<Account> list = new ArrayList<>();
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
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

    public int countAccounts(String search, String roleFilter) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM Account a "
                + "JOIN Role r ON a.RoleID = r.RoleID "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (a.Email LIKE ? OR u.FullName LIKE ?)");
            String like = "%" + search.trim() + "%";
            params.add(like);
            params.add(like);
        }

        if (roleFilter != null && !roleFilter.trim().isEmpty()) {
            if ("ADMIN".equalsIgnoreCase(roleFilter)) {
                sql.append(" AND r.RoleName = 'Admin'");
            } else if ("MANAGER".equalsIgnoreCase(roleFilter)) {
                sql.append(" AND r.RoleName = 'Manager'");
            } else if ("STAFF".equalsIgnoreCase(roleFilter)) {
                sql.append(" AND r.RoleName = 'Employee'");
            } else if ("CUSTOMER".equalsIgnoreCase(roleFilter)) {
                sql.append(" AND r.RoleName = 'Customer'");
            }
        }

        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
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

    public boolean toggleBlock(int accountId) {
        String sql = "UPDATE Account SET IsBlocked = CASE WHEN IsBlocked = 1 THEN 0 ELSE 1 END WHERE AccountID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countStaff() {
        String sql = "SELECT COUNT(*) FROM Account WHERE RoleID >= 3";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countLocked() {
        String sql = "SELECT COUNT(*) FROM Account WHERE IsBlocked = 1";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Account> getRecentAccounts(int limit) {
        String sql = "SELECT a.AccountID, a.Email, a.Password, a.RoleID, a.IsBlocked, a.CreatedAt, "
                + "r.RoleName, u.FullName, u.PhoneNumber, u.DoB, u.Address, u.AvatarURL "
                + "FROM Account a "
                + "JOIN Role r ON a.RoleID = r.RoleID "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "ORDER BY a.CreatedAt DESC";

        List<Account> list = new ArrayList<>();
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setMaxRows(limit);
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

    public boolean updateRole(int accountId, int roleId) {
        String sql = "UPDATE Account SET RoleID = ? WHERE AccountID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
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
        account.setPassword(rs.getString("Password"));
        account.setRoleId(rs.getInt("RoleID"));
        account.setRoleName(rs.getNString("RoleName"));
        account.setIsBlocked(rs.getBoolean("IsBlocked"));

        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) {
            account.setCreatedAt(ts.toLocalDateTime());
        }

        UserProfile profile = new UserProfile();
        profile.setFullName(rs.getNString("FullName"));
        profile.setPhoneNumber(rs.getString("PhoneNumber"));
        java.sql.Date dob = rs.getDate("DoB");
        profile.setDob(dob != null ? dob.toString() : null);
        profile.setAddress(rs.getNString("Address"));
        profile.setAvatarUrl(rs.getString("AvatarURL"));
        account.setProfile(profile);
        return account;
    }
}
