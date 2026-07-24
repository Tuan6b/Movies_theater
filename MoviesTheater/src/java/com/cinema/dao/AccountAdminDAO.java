package com.cinema.dao;

import com.cinema.model.Account;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AccountAdminDAO {

    private Account mapAccount(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setAccountId(rs.getInt("AccountID"));
        a.setEmail(rs.getString("Email"));
        a.setRoleId(rs.getInt("RoleID"));
        String rn = rs.getNString("RoleName");
        a.setRoleName(rn != null ? rn : "Unknown");
        a.setIsBlocked(rs.getBoolean("IsBlocked"));
        a.setNeedsSetup("pending".equals(rs.getString("AccountStatus")));
        a.setFullName(rs.getNString("FullName"));
        a.setPhoneNumber(rs.getString("PhoneNumber"));
        a.setAvatarUrl(rs.getString("AvatarURL"));
        java.sql.Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) a.setCreatedAt(ts.toLocalDateTime());
        return a;
    }

    public List<Account> getAllUsers(int page, int size) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT a.AccountID, a.Email, a.Password, a.RoleID, a.IsBlocked, a.AccountStatus, a.CreatedAt, "
                    + "r.RoleName, u.FullName, u.PhoneNumber, u.AvatarURL "
                + "FROM Account a "
                + "LEFT JOIN Role r ON a.RoleID = r.RoleID "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "ORDER BY a.CreatedAt DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, (page - 1) * size);
            ps.setInt(2, size);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapAccount(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countAllUsers() {
        String sql = "SELECT COUNT(*) FROM Account";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Account> searchUsers(String query, int page, int size) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT a.AccountID, a.Email, a.Password, a.RoleID, a.IsBlocked, a.AccountStatus, a.CreatedAt, "
                + "r.RoleName, u.FullName, u.PhoneNumber, u.AvatarURL "
                + "FROM Account a "
                + "LEFT JOIN Role r ON a.RoleID = r.RoleID "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "WHERE a.Email LIKE ? OR u.FullName LIKE ? "
                + "ORDER BY a.CreatedAt DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + query.trim() + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setInt(3, (page - 1) * size);
            ps.setInt(4, size);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapAccount(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countSearch(String query) {
        String sql = "SELECT COUNT(*) FROM Account a "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "WHERE a.Email LIKE ? OR u.FullName LIKE ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + query.trim() + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean setBlocked(int accountId, boolean blocked) {
        String sql = "UPDATE Account SET IsBlocked = ? WHERE AccountID = ?";
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

    public boolean isPhoneExist(String phone) {
        String sql = "SELECT COUNT(*) FROM UserProfile WHERE PhoneNumber = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isEmailExist(String email) {
        String sql = "SELECT COUNT(*) FROM Account WHERE Email = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int createAccount(String email, String password, String fullName, String phoneNumber, int roleId) {
        String sqlAccount = "INSERT INTO Account (Email, Password, RoleID, IsBlocked, AccountStatus) VALUES (?, ?, ?, 0, 'active')";
        String sqlProfile = "INSERT INTO UserProfile (AccountID, FullName, PhoneNumber) VALUES (?, ?, ?)";
        try (Connection conn = DBUtils.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlAccount, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, email.trim());
                ps.setString(2, com.cinema.util.PasswordHash.hash(password));
                ps.setInt(3, roleId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int accountId = keys.getInt(1);
                        try (PreparedStatement psProfile = conn.prepareStatement(sqlProfile)) {
                            psProfile.setInt(1, accountId);
                            psProfile.setNString(2, fullName.trim());
                            psProfile.setString(3, phoneNumber != null ? phoneNumber.trim() : null);
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

    public List<Account> getByRole(int roleId) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT a.AccountID, a.Email, a.Password, a.RoleID, a.IsBlocked, a.AccountStatus, a.CreatedAt, "
                + "r.RoleName, u.FullName, u.PhoneNumber, u.AvatarURL "
                + "FROM Account a "
                + "LEFT JOIN Role r ON a.RoleID = r.RoleID "
                + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
                + "WHERE a.RoleID = ? ORDER BY a.CreatedAt DESC";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapAccount(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Account> getByRoles(List<Integer> roleIds) {
        List<Account> list = new ArrayList<>();
        if (roleIds == null || roleIds.isEmpty()) return list;
        StringBuilder sql = new StringBuilder(
            "SELECT a.AccountID, a.Email, a.Password, a.RoleID, a.IsBlocked, a.AccountStatus, a.CreatedAt, "
            + "r.RoleName, u.FullName, u.PhoneNumber, u.AvatarURL "
            + "FROM Account a "
            + "LEFT JOIN Role r ON a.RoleID = r.RoleID "
            + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
            + "WHERE a.RoleID IN (");
        for (int i = 0; i < roleIds.size(); i++) {
            sql.append(i > 0 ? ",?" : "?");
        }
        sql.append(") ORDER BY a.CreatedAt DESC");
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < roleIds.size(); i++) {
                ps.setInt(i + 1, roleIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapAccount(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Account> searchByRoles(List<Integer> roleIds, String query, String statusFilter, String sortBy, String sortOrder) {
        List<Account> list = new ArrayList<>();
        if (roleIds == null || roleIds.isEmpty()) return list;

        StringBuilder sql = new StringBuilder(
            "SELECT a.AccountID, a.Email, a.Password, a.RoleID, a.IsBlocked, a.AccountStatus, a.CreatedAt, "
            + "r.RoleName, u.FullName, u.PhoneNumber, u.AvatarURL "
            + "FROM Account a "
            + "LEFT JOIN Role r ON a.RoleID = r.RoleID "
            + "LEFT JOIN UserProfile u ON a.AccountID = u.AccountID "
            + "WHERE a.RoleID IN (");

        for (int i = 0; i < roleIds.size(); i++) {
            sql.append(i > 0 ? ",?" : "?");
        }
        sql.append(")");

        List<Object> params = new ArrayList<>();
        params.addAll(roleIds);

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (a.Email LIKE ? OR u.FullName LIKE ?)");
            String like = "%" + query.trim() + "%";
            params.add(like);
            params.add(like);
        }

        if ("blocked".equals(statusFilter)) {
            sql.append(" AND a.IsBlocked = 1");
        } else if ("active".equals(statusFilter)) {
            sql.append(" AND a.IsBlocked = 0");
        }

        sql.append(" ORDER BY ");
        if ("name".equals(sortBy)) {
            sql.append("u.FullName");
        } else if ("email".equals(sortBy)) {
            sql.append("a.Email");
        } else if ("phone".equals(sortBy)) {
            sql.append("u.PhoneNumber");
        } else {
            sql.append("a.CreatedAt");
        }
        sql.append(" ").append("DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC");

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapAccount(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Map<String, Integer> getRoleStats() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String sql = "SELECT ISNULL(r.RoleName, 'Unknown') AS RoleName, COUNT(*) AS Cnt FROM Account a "
                + "LEFT JOIN Role r ON a.RoleID = r.RoleID GROUP BY r.RoleName, r.RoleID ORDER BY r.RoleID";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) stats.put(rs.getNString("RoleName"), rs.getInt("Cnt"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
}
