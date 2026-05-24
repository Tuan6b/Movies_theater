package dao;

import model.Genre;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenreDAO extends DBContext {
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
    
    public boolean addGenre(String genreName) throws Exception {
        String sql = "INSERT INTO Genre (GenreName) VALUES (?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            int result = st.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 2627) {
                throw new Exception("Genre existed!");
            }
            throw e;
        }
    }
    
    public boolean deleteGenre (int genreID) throws Exception {
        String checkSql = "SELECT COUNT(*) FROM MovieGenre WHERE GenreID = ?";
        try {
            PreparedStatement checkSt = connection.prepareStatement(checkSql);
            checkSt.setInt(1, genreID);
            ResultSet rs = checkSt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new Exception("Cannot delete, this genre is being used by a movie");              
            }
            
            String sql = "DELETE FROM Genre WHERE GenreID = ?";
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, genreID);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw e;
        }
    }
    
    public boolean updateGenre(int genreID, String newName) throws Exception {
        String sql = "UPDATE Genre SET GenreName = ? WHERE GenreID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, newName);
            st.setInt(2, genreID);
            
            return st.executeUpdate() > 0;
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 2627) {
                throw new Exception("Tên thể loại này đã tồn tại!");
            }
            System.out.println("Lỗi tại updateGenre: " + e.getMessage());
        }
        return false;
    }
}
