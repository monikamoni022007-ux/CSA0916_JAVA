import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Migration {
    public record FailedRecord(int lineNumber, String rawLine, String reason) {}

    public record MigrationReport(int totalLinesRead, int validLoadedCount, List<FailedRecord> failures) {
        public void printReport() {
            System.out.println("====================================================================");
            System.out.println("              BUS-PASS FILE-TO-DATABASE MIGRATION REPORT            ");
            System.out.println("====================================================================");
            System.out.printf("Total Lines Read     : %d%n", totalLinesRead);
            System.out.printf("Successfully Loaded  : %d%n", validLoadedCount);
            System.out.printf("Failed Validation    : %d%n", failures.size());
            System.out.println("--------------------------------------------------------------------");
            if (!failures.isEmpty()) {
                System.out.printf("%-8s | %-34s | %s%n", "Line #", "Raw Record Content", "Failure Reason");
                System.out.println("--------------------------------------------------------------------");
                for (FailedRecord f : failures) {
                    System.out.printf("%-8d | %-34s | %s%n", f.lineNumber(), f.rawLine(), f.reason());
                }
            }
            System.out.println("====================================================================");
        }
    }

    public static MigrationReport migrateFileToDatabase(Path csvFile, Connection con)
            throws IOException, SQLException {
        String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES (?, ?, ?, ?, ?)";
        List<FailedRecord> failures = new ArrayList<>();
        int lineNo = 0;
        int validCount = 0;

        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try (BufferedReader reader = Files.newBufferedReader(csvFile, StandardCharsets.UTF_8);
             PreparedStatement ps = con.prepareStatement(sql)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) {
                    failures.add(new FailedRecord(lineNo, "<EMPTY LINE>", "Blank or whitespace-only line"));
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length != 5) {
                    failures.add(new FailedRecord(lineNo, line, "Expected 5 fields but found " + parts.length));
                    continue;
                }
                String passId = parts[0].trim();
                String regNo = parts[1].trim();
                String name = parts[2].trim();
                if (passId.isEmpty() || regNo.isEmpty() || name.isEmpty()) {
                    failures.add(new FailedRecord(lineNo, line, "Missing mandatory identifier or student name"));
                    continue;
                }
                int routeNo;
                double amount;
                try {
                    routeNo = Integer.parseInt(parts[3].trim());
                    if (routeNo <= 0) {
                        failures.add(new FailedRecord(lineNo, line, "Route number must be positive: " + routeNo));
                        continue;
                    }
                } catch (NumberFormatException ex) {
                    failures.add(new FailedRecord(lineNo, line, "Invalid integer route_no: '" + parts[3] + "'"));
                    continue;
                }
                try {
                    amount = Double.parseDouble(parts[4].trim());
                    if (amount < 0.0) {
                        failures.add(new FailedRecord(lineNo, line, "Negative amount not permitted: " + amount));
                        continue;
                    }
                } catch (NumberFormatException ex) {
                    failures.add(new FailedRecord(lineNo, line, "Non-numeric amount: '" + parts[4] + "'"));
                    continue;
                }

                ps.setString(1, passId);
                ps.setString(2, regNo);
                ps.setString(3, name);
                ps.setInt(4, routeNo);
                ps.setDouble(5, amount);
                ps.addBatch();
                validCount++;
                if (validCount % 1000 == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
            con.commit();
            return new MigrationReport(lineNo, validCount, failures);
        } catch (SQLException | RuntimeException ex) {
            con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    public static void main(String[] args) throws IOException {
        Path csvPath = Paths.get("Task08/legacy_passes.csv");
        if (!Files.exists(csvPath)) {
            csvPath = Paths.get("legacy_passes.csv");
        }
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/csa09_assign5_test");
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String pass = System.getenv().getOrDefault("DB_PASS", "root");

        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            DatabaseInspector.inspectDatabase(con);
            try (Statement st = con.createStatement()) {
                st.executeUpdate("TRUNCATE TABLE bus_pass");
            }
            MigrationReport report = migrateFileToDatabase(csvPath, con);
            report.printReport();
        } catch (SQLException e) {
            System.err.println("DB Error (run Task08/database.sql in MySQL first): " + e.getMessage());
        }
    }
}
