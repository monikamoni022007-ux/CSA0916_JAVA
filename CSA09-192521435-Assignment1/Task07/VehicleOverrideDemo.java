package task07;

class Vehicle {
    private final String regNo;
    Vehicle(String regNo) { this.regNo = regNo; }
    public String getRegNo() { return regNo; }
    public String toString() { return regNo; }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Vehicle && regNo.equals(((Vehicle) obj).regNo);
    }
    @Override
    public int hashCode() { return regNo.hashCode(); }
}
class Car extends Vehicle {
    Car(String regNo) { super(regNo); }
    @Override
    public String toString() { return "Car(" + getRegNo() + ")"; }
}
public class VehicleOverrideDemo {
    public static void main(String[] args) {
        System.out.println("Task 7: overriding and equality");
        Vehicle a = new Car("TN02B2");
        Vehicle b = new Car("TN02B2");
        System.out.println(a.equals(b));
        System.out.println(a.hashCode() == b.hashCode());
        System.out.println(a);
    }
}
