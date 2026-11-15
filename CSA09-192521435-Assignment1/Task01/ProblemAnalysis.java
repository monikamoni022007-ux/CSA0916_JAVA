package task01;

public class ProblemAnalysis {
    public static void main(String[] args) {
        System.out.println("Task 1: Problem analysis and candidate classes");
        System.out.println("Candidate classes: ParkingFacility, ParkingFloor, ParkingSlot, Vehicle, Car, TwoWheeler, ElectricCar, ParkingTicket, ParkingUser, MonthlyPass");
        System.out.println("Business rules: one vehicle per slot, unique ticket IDs, no duplicate active registration numbers, fee by type and duration");
        System.out.println("Assumption: staff/students with valid monthly pass park free; visitors pay fully.");
    }
}
