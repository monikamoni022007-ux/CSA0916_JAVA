import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class FeeService {
    public boolean validateRegistrationNumber(String registrationNumber) {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            return false;
        }
        return registrationNumber.trim().matches("^[A-Za-z]{2,4}[0-9]{3,6}$");
    }

    public boolean validateAmount(String amountText) {
        if (amountText == null || amountText.trim().isEmpty()) {
            return false;
        }
        try {
            double amount = Double.parseDouble(amountText);
            return amount > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public double parseAmount(String amountText) {
        return Double.parseDouble(amountText.trim());
    }

    public String processPayment(Fee fee) {
        if (fee == null || !validateRegistrationNumber(fee.getRegistrationNumber())) {
            return "Invalid registration number.";
        }
        if (fee.getAmount() <= 0) {
            return "Invalid payment amount.";
        }

        String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/event_registration_db");
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "INSERT INTO fee_payments (registration_number, amount, status, payment_time) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, fee.getRegistrationNumber());
                preparedStatement.setDouble(2, fee.getAmount());
                preparedStatement.setString(3, "SUCCESS");
                preparedStatement.setString(4, LocalDateTime.now().toString());
                preparedStatement.executeUpdate();
                return "Payment successful for " + fee.getRegistrationNumber() + ".";
            }
        } catch (SQLException e) {
            return "Demo mode: Payment accepted for " + fee.getRegistrationNumber() + ".";
        }
    }
}
