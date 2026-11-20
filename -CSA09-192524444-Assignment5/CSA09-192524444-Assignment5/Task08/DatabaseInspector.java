import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseInspector {
    public static void inspectDatabase(Connection con) throws SQLException {
        DatabaseMetaData dbMeta = con.getMetaData();
        System.out.println("=== B4.2 DATABASE METADATA INSPECTION REPORT ===");
        System.out.println("Database Product Name    : " + dbMeta.getDatabaseProductName());
        System.out.println("Database Product Version : " + dbMeta.getDatabaseProductVersion());
        System.out.println("JDBC Driver Name         : " + dbMeta.getDriverName());
        System.out.println("JDBC Driver Version      : " + dbMeta.getDriverVersion());
        System.out.println("Supports Transactions    : " + dbMeta.supportsTransactions());
        System.out.println("Supports Batch Updates   : " + dbMeta.supportsBatchUpdates());
        System.out.println("Tables Present in Schema : ");
        try (ResultSet tables = dbMeta.getTables(con.getCatalog(), null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                System.out.println("  -> " + tables.getString("TABLE_NAME")
                        + " (Type: " + tables.getString("TABLE_TYPE") + ")");
            }
        }
    }
}
