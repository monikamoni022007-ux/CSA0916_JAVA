package task10;

class Vehicle {
    private String regNo;
    private int minutesParked;

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) {
        if (regNo == null || regNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Register number invalid");
        }
        this.regNo = regNo.trim();
    }
    public int getMinutesParked() { return minutesParked; }
    public void setMinutesParked(int minutesParked) {
        if (minutesParked < 0) {
            throw new IllegalArgumentException("Minutes cannot be negative");
        }
        this.minutesParked = minutesParked;
    }
}

public class EncapsulationDemo {
    public static void main(String[] args) {
        Vehicle v = new Vehicle();
        v.setRegNo("TN88Z8");
        v.setMinutesParked(120);
        System.out.println("Task 10: encapsulation");
        System.out.println(v.getRegNo() + " parked for " + v.getMinutesParked() + " minutes");
    }
}
