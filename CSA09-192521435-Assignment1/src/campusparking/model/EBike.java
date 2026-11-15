package campusparking.model;

public class EBike extends Vehicle {
    private final double flatFee;

    public EBike(String regNo, double flatFee) {
        super(regNo);
        if (!Double.isFinite(flatFee) || flatFee <= 0.0) {
            throw new IllegalArgumentException("E-bike flat fee must be a positive finite amount");
        }
        this.flatFee = flatFee;
    }

    @Override
    protected double ratePerHour() {
        return flatFee;
    }

    @Override
    public double calculateFee(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Parking duration cannot be negative");
        }
        return minutes == 0 ? 0.0 : flatFee;
    }
}
