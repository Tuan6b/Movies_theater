import com.cinema.util.DBUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckMovieStatus {
    public static void main(String[] args) {
        try(Connection conn = DBUtils.getConnection();
            Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT Status FROM Movie");
            while (rs.next()) {
                System.out.println("Status: " + rs.getString(1));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
