package campusparking.model;

import java.util.Locale;

public final class VehicleFactory {
    private VehicleFactory() {}

    public static Vehicle create(String type, String regNo, Double eBikeFlatFee) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle type is required");
        }
        String normalizedType = type.trim().replaceAll("[-_\\s]+", "_").toUpperCase(Locale.ROOT);
        switch (normalizedType) {
            case "TWOWHEELER":
            case "TWO_WHEELER":
                return new TwoWheeler(regNo);
            case "CAR":
                return new Car(regNo);
            case "ELECTRIC_CAR":
            case "EV":
                return new ElectricCar(regNo);
            case "DELIVERY_VAN":
            case "VAN":
                return new DeliveryVan(regNo);
            case "EBIKE":
            case "E_BIKE":
                if (eBikeFlatFee == null) {
                    throw new IllegalArgumentException("Configure the e-bike flat fee from the assignment/course rule");
                }
                return new EBike(regNo, eBikeFlatFee);
            default:
                throw new IllegalArgumentException("Unknown vehicle type: " + type);
        }
    }
}
