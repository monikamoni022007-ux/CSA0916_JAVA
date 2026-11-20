import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class WalletTransfer {
    public static boolean transferFunds(Connection con, String regNo, double amount) throws SQLException {
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        String debitSql = "UPDATE hostel_wallet SET balance = balance - ? WHERE reg_no = ? AND balance >= ?";
        String creditSql = "UPDATE canteen_wallet SET balance = balance + ? WHERE reg_no = ?";
        try (PreparedStatement debitPs = con.prepareStatement(debitSql);
             PreparedStatement creditPs = con.prepareStatement(creditSql)) {

            debitPs.setDouble(1, amount);
            debitPs.setString(2, regNo);
            debitPs.setDouble(3, amount);
            if (debitPs.executeUpdate() != 1) {
                throw new SQLException("Debit failed: insufficient hostel balance or missing account.");
            }

            creditPs.setDouble(1, amount);
            creditPs.setString(2, regNo);
            if (creditPs.executeUpdate() != 1) {
                throw new SQLException("Credit failed: destination canteen wallet missing.");
            }

            con.commit();
            return true;
        } catch (SQLException | RuntimeException ex) {
            con.rollback();
            System.out.println("Transaction rolled back safely: " + ex.getMessage());
            return false;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }
}
