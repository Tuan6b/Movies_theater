package com.cinema.dao;

import com.cinema.model.Food;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodDAO {

    public List<Food> getAllActiveFoods() {
        List<Food> list = new ArrayList<>();
        String sql = "SELECT FoodID, FoodName, Price, Image, IsCombo, IsActive FROM Food WHERE IsActive = 1 ORDER BY FoodName";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<Food> getActiveCombos() {
        List<Food> list = new ArrayList<>();
        String sql = "SELECT FoodID, FoodName, Price, Image, IsActive, IsCombo FROM Food WHERE IsCombo = 1 AND IsActive = 1 ORDER BY FoodName";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<Food> getActiveIndividualItems() {
        List<Food> list = new ArrayList<>();
        String sql = "SELECT FoodID, FoodName, Price, Image, IsActive, IsCombo FROM Food WHERE IsCombo = 0 AND IsActive = 1 ORDER BY FoodName";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<Food> getFoodsByType(boolean isCombo) {
        List<Food> list = new ArrayList<>();
        String sql = "SELECT FoodID, FoodName, Price, Image, IsCombo, IsActive FROM Food WHERE IsCombo = ? ORDER BY FoodName";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isCombo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public Map<Integer, Food> getFoodMapByIds(List<Integer> ids) {
        Map<Integer, Food> map = new HashMap<>();
        if (ids == null || ids.isEmpty()) return map;
        StringBuilder sql = new StringBuilder("SELECT FoodID, FoodName, Price, Image, IsActive, IsCombo FROM Food WHERE FoodID IN (");
        for (int i = 0; i < ids.size(); i++) {
            sql.append(i > 0 ? ",?" : "?");
        }
        sql.append(")");
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Food food = mapRow(rs);
                    map.put(food.getFoodId(), food);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return map;
    }

    public Food getFoodById(int id) {
        String sql = "SELECT FoodID, FoodName, Price, Image, IsCombo, IsActive FROM Food WHERE FoodID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void addFood(Food food) {
        String sql = "INSERT INTO Food (FoodName, Price, Image, IsCombo, IsActive) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, food.getFoodName());
            ps.setDouble(2, food.getPrice());
            ps.setString(3, food.getImage());
            ps.setBoolean(4, food.isCombo());
            ps.setBoolean(5, food.isActive());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void updateFood(Food food) {
        String sql = "UPDATE Food SET FoodName = ?, Price = ?, Image = ?, IsCombo = ? WHERE FoodID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, food.getFoodName());
            ps.setDouble(2, food.getPrice());
            ps.setString(3, food.getImage());
            ps.setBoolean(4, food.isCombo());
            ps.setInt(5, food.getFoodId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void restoreFood(int id) {
        String sql = "UPDATE Food SET IsActive = 1 WHERE FoodID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void deleteFood(int id) {
        String sql = "UPDATE Food SET IsActive = 0 WHERE FoodID = ?";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private Food mapRow(ResultSet rs) throws SQLException {
        Food food = new Food();
        food.setFoodId(rs.getInt("FoodID"));
        food.setFoodName(rs.getNString("FoodName"));
        food.setPrice(rs.getDouble("Price"));
        food.setImage(rs.getString("Image"));
        food.setCombo(rs.getBoolean("IsCombo"));
        food.setActive(rs.getBoolean("IsActive"));
        return food;
    }
}
