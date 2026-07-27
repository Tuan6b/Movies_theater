package com.cinema.dao;

import com.cinema.util.DBUtils;
import com.cinema.model.Genre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenreDAO {

    public List<Genre> getAllGenres() {
        List<Genre> list = new ArrayList<>();
        String sql = "SELECT * FROM Genre ORDER BY GenreID ASC";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement st = conn.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(new Genre(rs.getInt("GenreID"), rs.getString("GenreName")));
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }

    public List<Genre> getGenresByMovieId(int movieId) {
        List<Genre> list = new ArrayList<>();
        String sql = "SELECT g.GenreID, g.GenreName FROM Genre g JOIN MovieGenre mg ON g.GenreID = mg.GenreID WHERE mg.MovieID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, movieId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(new Genre(rs.getInt("GenreID"), rs.getString("GenreName")));
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }

    public boolean addGenre(String genreName) throws Exception {
        String sql = "INSERT INTO Genre (GenreName) VALUES (?)";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, genreName);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 2627) {
                throw new Exception("Thể loại này đã tồn tại!");
            }
            throw e;
        }
    }

    public boolean deleteGenre(int genreID) throws Exception {
        String checkSql = "SELECT COUNT(*) FROM MovieGenre WHERE GenreID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement checkSt = conn.prepareStatement(checkSql)) {
            checkSt.setInt(1, genreID);
            try (ResultSet rs = checkSt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new Exception("Không thể xoá — thể loại này đang được sử dụng bởi một phim.");
                }
            }
            String sql = "DELETE FROM Genre WHERE GenreID = ?";
            try (PreparedStatement st = conn.prepareStatement(sql)) {
                st.setInt(1, genreID);
                return st.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public boolean updateGenre(int genreID, String newName) throws Exception {
        String sql = "UPDATE Genre SET GenreName = ? WHERE GenreID = ?";
        try (Connection conn = DBUtils.getConnection();
                PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, newName);
            st.setInt(2, genreID);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 2627) {
                throw new Exception("Tên thể loại này đã tồn tại!");
            }
            System.out.println("Lỗi tại updateGenre: " + e.getMessage());
            return false;
        }
    }
}