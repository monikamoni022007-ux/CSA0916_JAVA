package task09;

interface Chargeable {
    double chargingFee(int kWh);

    default boolean supportsCharging() {
        return true;
    }

    static boolean isValidEnergyUsage(int kWh) {
        return kWh >= 0;
    }
}

abstract class Vehicle {
    private final String regNo;
    protected Vehicle(String regNo) { this.regNo = regNo; }
    public String getRegNo() { return regNo; }
    protected abstract double ratePerHour();
}

class Car extends Vehicle {
    Car(String regNo) { super(regNo); }
    @Override protected double ratePerHour() { return 30.0; }
}

class ElectricCar extends Car implements Chargeable {
    ElectricCar(String regNo) { super(regNo); }
    @Override public double chargingFee(int kWh) { return kWh * 12.0; }
}

public class AbstractionDemo {
    public static void main(String[] args) {
        System.out.println("Task 9: abstract class and interface");
        ElectricCar ev = new ElectricCar("TN05D5");
        System.out.println(ev.getRegNo() + " -> Rs " + ev.chargingFee(5));
        System.out.println("Charging supported = " + ev.supportsCharging());
        System.out.println("Usage valid = " + Chargeable.isValidEnergyUsage(5));
    }
}
