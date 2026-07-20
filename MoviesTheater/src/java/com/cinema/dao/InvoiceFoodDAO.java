package com.cinema.dao;

import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class InvoiceFoodDAO {

    public void insert(int invoiceId, Map<Integer, Integer> foodQuantities,
            Map<Integer, Double> foodPrices) throws SQLException {
        if (foodQuantities == null || foodQuantities.isEmpty()) return;
        String sql = "INSERT INTO InvoiceFood (InvoiceID, FoodID, Quantity, PriceAtBooking) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map.Entry<Integer, Integer> entry : foodQuantities.entrySet()) {
                int foodId = entry.getKey();
                int qty = entry.getValue();
                double price = foodPrices.getOrDefault(foodId, 0.0);
                ps.setInt(1, invoiceId);
                ps.setInt(2, foodId);
                ps.setInt(3, qty);
                ps.setDouble(4, price);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public Map<Integer, Integer> getByInvoiceId(int invoiceId) {
        Map<Integer, Integer> result = new HashMap<>();
        String sql = "SELECT FoodID, Quantity FROM InvoiceFood WHERE InvoiceID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("FoodID"), rs.getInt("Quantity"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void deleteByInvoiceId(int invoiceId) {
        String sql = "DELETE FROM InvoiceFood WHERE InvoiceID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
