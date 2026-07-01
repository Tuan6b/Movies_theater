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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
        movie.setDateAdded(rs.getTimestamp("DateAdded"));
        movie.setBudget(rs.getString("Budget"));
        movie.setGlobalBoxOffice(rs.getString("GlobalBoxOffice"));
        movie.setWeeklyRevenueRank(rs.getInt("WeeklyRevenueRank"));
        movie.setTicketsSoldMilestone(rs.getInt("TicketsSoldMilestone"));
        movie.setPoster(rs.getString("Poster"));
        movie.setTrailer(rs.getString("Trailer"));
        movie.setLanguage(rs.getString("Language"));
        movie.setSubtitle(rs.getString("Subtitle"));
        movie.setDirector(rs.getString("Director"));
        movie.setCast(rs.getString("Cast"));
        movie.setCountry(rs.getString("Country"));
        movie.setAgeRestriction(rs.getInt("AgeRestriction"));
        movie.setIsActive(rs.getBoolean("IsActive"));

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
                sql = "SELECT m.* FROM Movie m WHERE m.IsActive = 1 "
                        + "AND EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.StartTime <= GETDATE()) "
                        + "AND EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.EndTime >= GETDATE()) "
                        + "ORDER BY m.MovieID DESC";
                break;
            case "ended":
                sql = "SELECT m.* FROM Movie m WHERE m.IsActive = 1 "
                        + "AND EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID) "
                        + "AND NOT EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.EndTime >= GETDATE()) "
                        + "ORDER BY m.MovieID DESC";
                break;
            case "unscheduled":
                sql = "SELECT m.* FROM Movie m WHERE m.IsActive = 1 AND NOT EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID) ORDER BY m.MovieID DESC";
                break;
            case "upcoming":
            default:
                sql = "SELECT m.* FROM Movie m WHERE m.IsActive = 1 "
                        + "AND EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.StartTime > GETDATE()) "
                        + "AND NOT EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.StartTime <= GETDATE()) "
                        + "ORDER BY m.MovieID DESC";
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
        String sql = "UPDATE Movie SET MovieName=?, Description=?, Duration=?, DateAdded=?, Budget=?, GlobalBoxOffice=?, WeeklyRevenueRank=?, TicketsSoldMilestone=?, Poster=?, Trailer=?, Language=?, "
                + "Subtitle=?, Director=?, Cast=?, Country=?, AgeRestriction=?, IsActive=? WHERE MovieID=?";

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, movie.getMovieName());
            ps.setString(2, movie.getDescription());
            ps.setInt(3, movie.getDuration());
            ps.setTimestamp(4, movie.getDateAdded() != null ? new Timestamp(movie.getDateAdded().getTime()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(5, movie.getBudget());
            ps.setString(6, movie.getGlobalBoxOffice());
            ps.setInt(7, movie.getWeeklyRevenueRank());
            ps.setInt(8, movie.getTicketsSoldMilestone());
            ps.setString(9, movie.getPoster());
            ps.setString(10, movie.getTrailer());
            ps.setString(11, movie.getLanguage());
            ps.setString(12, movie.getSubtitle());
            ps.setString(13, movie.getDirector());
            ps.setString(14, movie.getCast());
            ps.setString(15, movie.getCountry());
            ps.setInt(16, movie.getAgeRestriction());
            ps.setBoolean(17, movie.isIsActive());
            ps.setInt(18, movie.getMovieId());

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

    public boolean hasSchedule(int movieId) {
        String sql = "SELECT 1 FROM Schedule WHERE MovieID = ?";
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public int getTotalPublicMovies(String status, String genreId, String searchKeyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Movie m ");

        if (genreId != null && !genreId.trim().isEmpty()) {
            sql.append("JOIN MovieGenre mg ON m.MovieID = mg.MovieID ");
        }

        sql.append("WHERE m.IsActive = 1 ");

        if ("upcoming".equals(status)) {
            sql.append(" AND EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.StartTime > GETDATE()) AND NOT EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.StartTime <= GETDATE()) ");
        } else if ("showing".equals(status)) {
            sql.append(" AND EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.StartTime <= GETDATE()) AND EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.EndTime >= GETDATE()) ");
        }

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append(" AND m.MovieName LIKE ? ");
        }

        if (genreId != null && !genreId.trim().isEmpty()) {
            sql.append(" AND mg.GenreID = ?");
        }

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (genreId != null && !genreId.trim().isEmpty()) {
                int paramIndex = 1;
                if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                    ps.setString(paramIndex++, "%" + searchKeyword.trim() + "%");
                }
                if (genreId != null && !genreId.trim().isEmpty()) {
                    ps.setInt(paramIndex, Integer.parseInt(genreId));
                }
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

    public Map<String, Integer> getMovieStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("upcoming", 0);
        stats.put("showing", 0);
        stats.put("ended", 0);
        stats.put("hidden", 0);
        
        try (Connection conn = DBUtils.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Movie WHERE IsActive = 0"); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) stats.put("hidden", rs.getInt(1));
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Movie m WHERE IsActive = 1 AND EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.StartTime > GETDATE()) AND NOT EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.StartTime <= GETDATE())"); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) stats.put("upcoming", rs.getInt(1));
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Movie m WHERE IsActive = 1 AND EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.StartTime <= GETDATE()) AND EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.EndTime >= GETDATE())"); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) stats.put("showing", rs.getInt(1));
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Movie m WHERE IsActive = 1 AND EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID) AND NOT EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.EndTime >= GETDATE())"); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) stats.put("ended", rs.getInt(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return stats;
    }

    public List<clsMovie> getPublicMoviesByPage(String status, int offset, int limit, String genreId, String searchKeyword) {
        List<clsMovie> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT m.* FROM Movie m ");

        if (genreId != null && !genreId.trim().isEmpty()) {
            sql.append("JOIN MovieGenre mg ON m.MovieID = mg.MovieID ");
        }

        sql.append("WHERE m.IsActive = 1 ");

        if ("upcoming".equals(status)) {
            sql.append(" AND EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.StartTime > GETDATE()) AND NOT EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.StartTime <= GETDATE()) ");
        } else if ("showing".equals(status)) {
            sql.append(" AND EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.StartTime <= GETDATE()) AND EXISTS (SELECT 1 FROM Schedule s WHERE s.MovieID = m.MovieID AND s.EndTime >= GETDATE()) ");
        }

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append(" AND m.MovieName LIKE ? ");
        }

        if (genreId != null && !genreId.trim().isEmpty()) {
            sql.append(" AND mg.GenreID = ? ");
        }

        sql.append("ORDER BY m.MovieID DESC ");

        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                ps.setString(paramIndex++, "%" + searchKeyword.trim() + "%");
            }
            if (genreId != null && !genreId.trim().isEmpty()) {
                ps.setInt(paramIndex++, Integer.parseInt(genreId));
            }
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
        String sql = "INSERT INTO Movie (MovieName, Description, Duration, DateAdded, Budget, GlobalBoxOffice, WeeklyRevenueRank, TicketsSoldMilestone, Poster, Trailer, "
                + "Language, Subtitle, Director, Cast, Country, AgeRestriction, IsActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBUtils.getConnection(); // Yêu cầu trả về ID tự tăng
                 PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, movie.getMovieName());
            ps.setString(2, movie.getDescription());
            ps.setInt(3, movie.getDuration());
            ps.setTimestamp(4, movie.getDateAdded() != null ? new Timestamp(movie.getDateAdded().getTime()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(5, movie.getBudget());
            ps.setString(6, movie.getGlobalBoxOffice());
            ps.setInt(7, movie.getWeeklyRevenueRank());
            ps.setInt(8, movie.getTicketsSoldMilestone());
            ps.setString(9, movie.getPoster());
            ps.setString(10, movie.getTrailer());
            ps.setString(11, movie.getLanguage());
            ps.setString(12, movie.getSubtitle());
            ps.setString(13, movie.getDirector());
            ps.setString(14, movie.getCast());
            ps.setString(15, movie.getCountry());
            ps.setInt(16, movie.getAgeRestriction());
            ps.setBoolean(17, movie.isIsActive());
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
