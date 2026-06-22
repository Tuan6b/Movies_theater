package com.cinema.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;

public class SystemLogService {

    private static final String SQL =
            "INSERT INTO SystemLog (AccountID, ActionType, Description, IPAddress) VALUES (?, ?, ?, ?)";

    public static void log(Integer accountId, String actionType, String description, String ipAddress) {
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            if (accountId != null) {
                ps.setInt(1, accountId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, actionType);
            ps.setString(3, description);
            ps.setString(4, ipAddress);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
