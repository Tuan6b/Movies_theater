/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.dao;

import com.cinema.model.clsMovie;
import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Movie entity. Follows NetBeans-based tbDAO naming
 * standards.
 *
 * @author TBinh
 */
public class tbMovie {

    /**
     * Maps a ResultSet row to a clsMovie object.
     *
     * @param rs ResultSet containing movie row
     * @return clsMovie object
     * @throws SQLException if mapping fails
     */
    private clsMovie mapRow(ResultSet rs) throws SQLException {
        clsMovie movie = new clsMovie();
        movie.setMovieId(rs.getInt("MovieID"));
        movie.setMovieName(rs.getString("MovieName"));
        movie.setDescription(rs.getString("Description"));
        movie.setDuration(rs.getInt("Duration"));
        movie.setReleaseDate(rs.getDate("ReleaseDate"));
        movie.setPoster(rs.getString("Poster"));
        movie.setTrailer(rs.getString("Trailer"));
        movie.setLanguage(rs.getString("Language"));
        movie.setSubtitle(rs.getString("Subtitle"));
        movie.setDirector(rs.getString("Director"));
        movie.setCast(rs.getString("Cast"));
        movie.setCountry(rs.getString("Country"));
        movie.setAgeRestriction(rs.getInt("AgeRestriction"));
        movie.setActive(rs.getBoolean("IsActive"));

        return movie;
    }

    /**
     * Retrieve all active movies from database.
     *
     * @return list of active movies
     */
    public List<clsMovie> getAllActiveMovies() {
        List<clsMovie> list = new ArrayList<>();
        String sql = "SELECT * FROM Movie WHERE IsActive = 1";

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Find a movie by its unique MovieID.
     *
     * @param movieId ID of the movie
     * @return clsMovie object if found, else null
     */
    public clsMovie getMovieById(int movieId) {
        String sql = "SELECT * FROM Movie WHERE MovieID = ?";

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, movieId);
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

    /**
     * Get movies based on a specific filter
     *
     * @param filter 'upcoming', 'showing', 'ended', 'hidden'
     * @return list of filtered movies
     */
    public List<clsMovie> getMoviesByFilter(String filter) {
        List<clsMovie> list = new ArrayList<>();
        String sql = "";

        switch (filter) {
            case "hidden":
                sql = "SELECT * FROM Movie WHERE IsActive = 0 ORDER BY MovieID DESC";
                break;
            case "showing":
                sql = "SELECT m.* FROM Movie m WHERE m.IsActive = 1 AND m.ReleaseDate <= CAST(GETDATE() AS DATE) "
                        + "AND (NOT EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID) "
                        + "OR EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.EndTime >= GETDATE())) "
                        + "ORDER BY m.MovieID DESC";
                break;
            case "ended":
                sql = "SELECT m.* FROM Movie m WHERE m.IsActive = 1 AND m.ReleaseDate <= CAST(GETDATE() AS DATE) "
                        + "AND EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID) "
                        + "AND NOT EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.EndTime >= GETDATE()) "
                        + "ORDER BY m.MovieID DESC";
                break;
            case "upcoming":
            default:
                sql = "SELECT * FROM Movie WHERE IsActive = 1 AND ReleaseDate > CAST(GETDATE() AS DATE) ORDER BY MovieID DESC";
                break;
        }

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public boolean updateMovie(clsMovie movie) {
        String sql = "UPDATE Movie SET MovieName=?, Description=?, Duration=?, ReleaseDate=?, Poster=?, Trailer=?, Language=?, "
                + "Subtitle=?, Director=?, Cast=?, Country=?, AgeRestriction=?, IsActive=? WHERE MovieID=?";

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, movie.getMovieName());
            ps.setString(2, movie.getDescription());
            ps.setInt(3, movie.getDuration());
            ps.setDate(4, movie.getReleaseDate());
            ps.setString(5, movie.getPoster());
            ps.setString(6, movie.getTrailer());
            ps.setString(7, movie.getLanguage());
            ps.setString(8, movie.getSubtitle());
            ps.setString(9, movie.getDirector());
            ps.setString(10, movie.getCast());
            ps.setString(11, movie.getCountry());
            ps.setInt(12, movie.getAgeRestriction());
            ps.setBoolean(13, movie.isActive());
            ps.setInt(14, movie.getMovieId());

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean toggleMovieStatus(int movieId) {
        // Revert movie status (0 to 1, 1 to 0)
        String sql = "UPDATE Movie SET IsActive = CASE WHEN IsActive = 1 THEN 0 ELSE 1 END WHERE MovieID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public int getTotalPublicMovies(String status, String genreId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Movie m ");

        if (genreId != null && !genreId.trim().isEmpty()) {
            sql.append("JOIN MovieGenre mg ON m.MovieID = mg.MovieID ");
        }

        sql.append("WHERE m.IsActive = 1 ");

        if ("upcoming".equals(status)) {
            sql.append(" AND m.ReleaseDate > CAST(GETDATE() AS DATE) ");
        } else if ("showing".equals(status)) {
            // update: Loại trừ các phim đã hết hạn chiếu
            sql.append(" AND m.ReleaseDate <= CAST(GETDATE() AS DATE) ");
            sql.append(" AND (NOT EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID) ");
            sql.append(" OR EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.EndTime >= GETDATE())) ");
            // end update code
        }

        if (genreId != null && !genreId.trim().isEmpty()) {
            sql.append(" AND mg.GenreID = ?");
        }

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (genreId != null && !genreId.trim().isEmpty()) {
                ps.setInt(1, Integer.parseInt(genreId));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public List<clsMovie> getPublicMoviesByPage(String status, int offset, int limit, String genreId) {
        List<clsMovie> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT m.* FROM Movie m ");

        if (genreId != null && !genreId.trim().isEmpty()) {
            sql.append("JOIN MovieGenre mg ON m.MovieID = mg.MovieID ");
        }

        sql.append("WHERE m.IsActive = 1 ");

        if ("upcoming".equals(status)) {
            sql.append(" AND m.ReleaseDate > CAST(GETDATE() AS DATE) ");
        } else if ("showing".equals(status)) {
            // update: Loại trừ các phim đã hết hạn chiếu
            sql.append(" AND m.ReleaseDate <= CAST(GETDATE() AS DATE) ");
            sql.append(" AND (NOT EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID) ");
            sql.append(" OR EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.EndTime >= GETDATE())) ");
            // end update code
        }

        if (genreId != null && !genreId.trim().isEmpty()) {
            sql.append(" AND mg.GenreID = ? ");
        }

        if ("upcoming".equals(status)) {
            sql.append("ORDER BY m.ReleaseDate ASC ");
        } else {
            sql.append("ORDER BY m.ReleaseDate DESC ");
        }

        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            // Tham số 1: GenreID (nếu có)
            if (genreId != null && !genreId.trim().isEmpty()) {
                ps.setInt(paramIndex++, Integer.parseInt(genreId));
            }

            // Tham số 2 & 3: Phân trang
            ps.setInt(paramIndex++, offset);
            ps.setInt(paramIndex, limit);

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

    public int insertMovieAndGetId(clsMovie movie) {
        String sql = "INSERT INTO Movie (MovieName, Description, Duration, ReleaseDate, Poster, Trailer, "
                + "Language, Subtitle, Director, Cast, Country, AgeRestriction, IsActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBUtils.getConnection(); // Yêu cầu trả về ID tự tăng
                 PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setNString(1, movie.getMovieName());
            ps.setNString(2, movie.getDescription());
            ps.setInt(3, movie.getDuration());
            ps.setDate(4, movie.getReleaseDate());
            ps.setString(5, movie.getPoster());
            ps.setString(6, movie.getTrailer());
            ps.setNString(7, movie.getLanguage());
            ps.setNString(8, movie.getSubtitle());
            ps.setNString(9, movie.getDirector());
            ps.setNString(10, movie.getCast());
            ps.setNString(11, movie.getCountry());
            ps.setInt(12, movie.getAgeRestriction());
            ps.setBoolean(13, movie.isActive());
            ps.executeUpdate();

            // Lấy ID vừa tạo ra
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Cập nhật bảng MovieGenre (Xóa thể loại cũ, chèn thể loại mới vào)
    public void updateMovieGenres(int movieId, String[] genreIds) {
        try (Connection conn = DBUtils.getConnection()) {
            String deleteSql = "DELETE FROM MovieGenre WHERE MovieID = ?";
            try (PreparedStatement psDel = conn.prepareStatement(deleteSql)) {
                psDel.setInt(1, movieId);
                psDel.executeUpdate();
            }
            if (genreIds != null && genreIds.length > 0) {
                String insertSql = "INSERT INTO MovieGenre (MovieID, GenreID) VALUES (?, ?)";
                try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                    for (String gId : genreIds) {
                        psIns.setInt(1, movieId);
                        psIns.setInt(2, Integer.parseInt(gId));
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Lấy danh sách ID thể loại của một bộ phim
    public List<Integer> getGenreIdsByMovie(int movieId) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT GenreID FROM MovieGenre WHERE MovieID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("GenreID"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
