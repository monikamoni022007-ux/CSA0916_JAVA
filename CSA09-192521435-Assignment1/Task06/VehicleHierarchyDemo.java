package task06;

class Vehicle {
    private final String regNo;
    protected Vehicle(String regNo) { this.regNo = regNo; }
    public String getRegNo() { return regNo; }
}
class TwoWheeler extends Vehicle {
    TwoWheeler(String regNo) { super(regNo); }
}
class Car extends Vehicle {
    Car(String regNo) { super(regNo); }
}
class ElectricCar extends Car {
    ElectricCar(String regNo) { super(regNo); }
}
public class VehicleHierarchyDemo {
    public static void main(String[] args) {
        System.out.println("Task 6: Inheritance hierarchy");
        System.out.println(new TwoWheeler("TN01A1").getRegNo());
        System.out.println(new ElectricCar("TN02B2").getRegNo());
    }
}
