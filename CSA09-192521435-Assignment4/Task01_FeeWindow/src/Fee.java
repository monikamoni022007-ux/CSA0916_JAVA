public class Fee {
    private final String registrationNumber;
    private final double amount;

    public Fee(String registrationNumber, double amount) {
        this.registrationNumber = registrationNumber;
        this.amount = amount;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public double getAmount() {
        return amount;
    }
}
