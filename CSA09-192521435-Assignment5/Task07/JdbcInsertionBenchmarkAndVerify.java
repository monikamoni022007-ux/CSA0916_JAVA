import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcInsertionBenchmarkAndVerify {

    public static List<String[]> generatePasses(int count) {
        List<String[]> list = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            list.add(new String[]{
                    "P" + String.format("%06d", i),
                    "1923" + String.format("%05d", i),
                    "Student_" + i,
                    String.valueOf((i % 50) + 1),
                    String.valueOf(3500.0 + (i % 10) * 150.0)
            });
        }
        return list;
    }

    public static void clearTable(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("TRUNCATE TABLE bus_pass");
        }
    }

    public static int countRows(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM bus_pass")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static long insertWithStatement(Connection con, List<String[]> passes) throws SQLException {
        long start = System.nanoTime();
        try (Statement st = con.createStatement()) {
            for (String[] p : passes) {
                String sql = String.format(
                        "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES ('%s','%s','%s',%d,%s)",
                        p[0], p[1], p[2], Integer.parseInt(p[3]), Double.parseDouble(p[4]));
                st.executeUpdate(sql);
            }
        }
        return System.nanoTime() - start;
    }

    public static long insertWithPreparedStatement(Connection con, List<String[]> passes) throws SQLException {
        long start = System.nanoTime();
        String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (String[] p : passes) {
                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setString(3, p[2]);
                ps.setInt(4, Integer.parseInt(p[3]));
                ps.setDouble(5, Double.parseDouble(p[4]));
                ps.executeUpdate();
            }
        }
        return System.nanoTime() - start;
    }

    public static void verifyTenRecordsAndMetaData(Connection con) throws SQLException {
        String sql = "SELECT pass_id, reg_no, name, route_no, amount FROM bus_pass ORDER BY pass_id";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            System.out.println("=== B3.1 ResultSetMetaData Verification (Columns: " + colCount + ") ===");
            for (int i = 1; i <= colCount; i++) {
                System.out.printf("Col %d: Name=%-10s | Type=%-10s | JavaClass=%s%n",
                        i, meta.getColumnName(i), meta.getColumnTypeName(i), meta.getColumnClassName(i));
            }
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
            }
            System.out.println("Total Verified Rows in bus_pass: " + rowCount);
        }
    }

    public static void main(String[] args) {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/csa09_assign5_test");
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String pass = System.getenv().getOrDefault("DB_PASS", "root");

        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            // B3.1: 10-record verification
            clearTable(con);
            PassLoader.loadAll(con, generatePasses(10));
            verifyTenRecordsAndMetaData(con);

            // B3.2: Benchmark 1,000, 10,000, and 100,000 passes
            int[] sizes = {1_000, 10_000, 100_000};
            System.out.printf("%n%-12s | %-18s | %-22s | %-18s%n",
                    "Record Count", "Statement (ms)", "PreparedStatement (ms)", "Batch (ms)");
            System.out.println("-----------------------------------------------------------------------------");
            for (int n : sizes) {
                List<String[]> data = generatePasses(n);
                clearTable(con);
                double stMs = insertWithStatement(con, data) / 1_000_000.0;
                clearTable(con);
                double psMs = insertWithPreparedStatement(con, data) / 1_000_000.0;
                clearTable(con);
                long bStart = System.nanoTime();
                PassLoader.loadAll(con, data);
                double bMs = (System.nanoTime() - bStart) / 1_000_000.0;
                System.out.printf("%-12d | %-18.2f | %-22.2f | %-18.2f%n", n, stMs, psMs, bMs);
            }

            // B3.3: Mid-way failure rollback verification
            clearTable(con);
            List<String[]> poisoned = generatePasses(2_000);
            poisoned.get(1_000)[4] = "INVALID_AMOUNT";
            try {
                PassLoader.loadAll(con, poisoned);
            } catch (RuntimeException expected) {
                System.out.println("\nB3.3 Mid-way failure caught (" + expected.getClass().getSimpleName()
                        + "). Rows remaining after rollback: " + countRows(con));
            }
        } catch (SQLException e) {
            System.err.println("DB Error (run Task07/database.sql in MySQL first): " + e.getMessage());
        }
    }
}
