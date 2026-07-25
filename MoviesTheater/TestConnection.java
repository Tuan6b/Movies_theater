import com.cinema.util.DBUtils;
import com.cinema.util.PasswordHash;
import com.cinema.dao.AccountDAO;
import com.cinema.model.Account;
import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        // 1. Test DB connection
        try {
            Connection conn = DBUtils.getConnection();
            System.out.println("DB Connection: SUCCESS - " + conn.getMetaData().getDatabaseProductName());
            conn.close();
        } catch (Exception e) {
            System.out.println("DB Connection: FAILED - " + e.getMessage());
            e.printStackTrace();
        }

        // 2. Test password hash
        String hash = PasswordHash.hash("123456");
        boolean verify = PasswordHash.verify("123456", hash);
        System.out.println("PasswordHash self-test: " + (verify ? "PASS" : "FAIL"));

        // 3. Test seed password hash
        String seedHash = "GxBf2JiV8tjQ8Va47w2dSN5/j3WSWL+1a3KSEDF3M16MFlGFj84AJfS2IW/J8XbL";
        boolean seedVerify = PasswordHash.verify("123456", seedHash);
        System.out.println("Seed password '123456' verify: " + (seedVerify ? "PASS" : "FAIL"));

        // 4. Test login
        AccountDAO dao = new AccountDAO();
        Account account = dao.login("admin@cinema.vn", "123456");
        System.out.println("Login admin@cinema.vn: " + (account != null ? "SUCCESS (" + account.getRoleName() + ")" : "FAILED"));

        // 5. Test getAccountByEmail
        Account byEmail = dao.getAccountByEmail("admin@cinema.vn");
        System.out.println("getAccountByEmail admin@cinema.vn: " + (byEmail != null ? "FOUND" : "NOT FOUND"));
    }
}
