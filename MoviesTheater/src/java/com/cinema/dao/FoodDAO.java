package com.cinema.dao;

import com.cinema.model.Food;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FoodDAO {

    public List<Food> getAllActiveFoods() {
        List<Food> list = new ArrayList<>();
        String sql = "SELECT FoodID, FoodName, Price, Image, IsActive FROM Food WHERE IsActive = 1";
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

    private Food mapRow(ResultSet rs) throws SQLException {
        Food food = new Food();
        food.setFoodId(rs.getInt("FoodID"));
        food.setFoodName(rs.getNString("FoodName"));
        food.setPrice(rs.getDouble("Price"));
        food.setImage(rs.getString("Image"));
        food.setIsActive(rs.getBoolean("IsActive"));
        return food;
    }
}
