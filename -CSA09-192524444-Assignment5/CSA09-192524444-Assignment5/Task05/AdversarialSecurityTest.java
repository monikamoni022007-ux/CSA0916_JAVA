import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class AdversarialSecurityTest {

    public static boolean unsafeLogin(Connection con, String id, String pwd) throws SQLException {
        String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
        System.out.println("[UNSAFE SQL] : " + sql);
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next();
        }
    }

    public static boolean safeLogin(Connection con, String id, String pwd) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ? AND pwd = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, pwd);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static String[] findStrict(Path file, String passId) throws IOException {
        try (BufferedReader r = Files.newBufferedReader(file)) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] f = line.split(",", -1);
                if (f.length != 5) continue;
                try {
                    Integer.parseInt(f[3].trim());
                    Double.parseDouble(f[4].trim());
                } catch (NumberFormatException ex) {
                    continue;
                }
                if (f[0].equals(passId)) return f;
            }
        }
        return null;
    }

    public static void runDamagedFileTests() throws IOException {
        Path f = Files.createTempFile("damaged_passes_", ".csv");
        String content =
                "P1001,192311001,Arun,12,4500.0\n" +
                "P1002,192311002,Bala\n" +
                "\n" +
                "P1003,192311003,Charan,15,NOT_A_NUMBER\n" +
                "P1004,192311004,Divya,7,3800.0\n";
        Files.writeString(f, content);
        System.out.println("=== Damaged Bus-Pass File Tests ===");
        System.out.println("1. Wrong field count (P1002) : " + Arrays.toString(findStrict(f, "P1002")));
        System.out.println("2. Non-numeric amount (P1003): " + Arrays.toString(findStrict(f, "P1003")));
        System.out.println("3. Valid after empty (P1004) : " + Arrays.toString(findStrict(f, "P1004")));
        Files.deleteIfExists(f);
    }

    public static void main(String[] args) throws IOException {
        runDamagedFileTests();

        String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/csa09_assign5_test");
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String pass = System.getenv().getOrDefault("DB_PASS", "root");
        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            String craftedId = "admin";
            String craftedPwd = "' OR '1'='1";
            System.out.println("=== SQL Injection Login Comparison ===");
            System.out.println("Unsafe Login with SQLi payload        : " + unsafeLogin(con, craftedId, craftedPwd));
            System.out.println("PreparedStatement Login with SQLi     : " + safeLogin(con, craftedId, craftedPwd));
            System.out.println("=== Wallet Transfer Transaction Test ===");
            System.out.println("Transfer ₹500 for 192311001 (Valid)   : " + WalletTransfer.transferFunds(con, "192311001", 500.0));
            System.out.println("Transfer ₹500 to Missing Canteen Acct : " + WalletTransfer.transferFunds(con, "INVALID_REG", 500.0));
        } catch (SQLException e) {
            System.err.println("DB Note (run Task05/setup_users_test_db.sql in MySQL first): " + e.getMessage());
        }
    }
}
