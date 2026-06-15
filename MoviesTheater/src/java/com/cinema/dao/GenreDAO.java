package com.cinema.dao;

import com.cinema.model.Genre;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.cinema.util.DBContext;

/**
 * Data Access Object (DAO) for the Genre entity.
 * Handles all database operations related to movie genres including CRUD functionalities.
 *
 * @author CuongPVHE204336
 * @version 1.0 24/05/2026
 */
public class GenreDAO extends DBContext {
    
    /**
     * Retrieves all movie genres from the database.
     *
     * @return A list of Genre objects ordered by their ID in descending order.
     */
    public List<Genre> getAllGenres() {
        List<Genre> list = new ArrayList<>();
        String sql = "SELECT * FROM Genre ORDER BY GenreID DESC";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                int genreID = rs.getInt("GenreID");
                String genreName = rs.getString("GenreName");
                list.add(new Genre(genreID, genreName));
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }
    
    /**
     * Inserts a new genre into the database.
     *
     * @param genreName The name of the new genre to be added.
     * @return true if the insertion is successful, false otherwise.
     * @throws Exception If the genre name already exists (UNIQUE constraint violation).
     */
    public boolean addGenre(String genreName) throws Exception {
        String sql = "INSERT INTO Genre (GenreName) VALUES (?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, genreName);
            int result = st.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            // Error code 2627 indicates a UNIQUE constraint violation.
            if (e.getErrorCode() == 2627) {
                throw new Exception("Genre existed!");
            }
            throw e;
        }
    }
    
    /**
     * Deletes a genre from the database.
     * Ensures that the genre is not currently assigned to any movie before deletion.
     *
     * @param genreID The ID of the genre to delete.
     * @return true if the deletion is successful, false otherwise.
     * @throws Exception If the genre is tied to a movie via the MovieGenre table.
     */
    public boolean deleteGenre (int genreID) throws Exception {
        
        /*
        Check if the genre is currently used in the MovieGenre junction table or not.
        */
        String checkSql = "SELECT COUNT(*) FROM MovieGenre WHERE GenreID = ?";
        try {
            PreparedStatement checkSt = connection.prepareStatement(checkSql);
            checkSt.setInt(1, genreID);
            ResultSet rs = checkSt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new Exception("Cannot delete, this genre is being used by a movie");              
            }
            
            // Delete the genre if no dependencies are found.
            String sql = "DELETE FROM Genre WHERE GenreID = ?";
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, genreID);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw e;
        }
    }
    
    /**
     * Updates the name of an existing genre.
     *
     * @param genreID   The ID of the genre to update.
     * @param newName   The new name for the genre.
     * @return true if the update is successful, false otherwise.
     * @throws Exception If the new name conflicts with an existing genre.
     */
    public boolean updateGenre(int genreID, String newName) throws Exception {
        String sql = "UPDATE Genre SET GenreName = ? WHERE GenreID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, newName);
            st.setInt(2, genreID);
            
            return st.executeUpdate() > 0;
            
        } catch (SQLException e) {
            // Error code 2627 indicates a UNIQUE constraint violation.
            if (e.getErrorCode() == 2627) {
                throw new Exception("Tên thể loại này đã tồn tại!");
            }
            System.out.println("Lỗi tại updateGenre: " + e.getMessage());
        }
        return false;
    }
}
