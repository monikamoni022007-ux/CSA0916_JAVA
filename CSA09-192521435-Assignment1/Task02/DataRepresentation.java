package task02;

public class DataRepresentation {
    public static void main(String[] args) {
        int slotNumber = 5;
        String regNo = "TN01AB1234";
        int minutes = 130;
        int startedHours = minutes / 60 + (minutes % 60 == 0 ? 0 : 1);
        double fee = startedHours * 30.0;
        System.out.println("Task 2: Data representation and integer division protection");
        System.out.println("slotNumber = " + slotNumber);
        System.out.println("regNo = " + regNo);
        System.out.println("fee for 130 minutes and Rs 30/hr = Rs " + fee);
        System.out.println("Use jagged arrays to support unequal floor capacities.");
    }
}
