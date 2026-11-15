package task04;

class Ticket {
    private static long counter = 1000;
    private final long id;
    private final String regNo;

    public Ticket(String regNo) {
        this(regNo, counter + 1);
    }

    public Ticket(String regNo, long id) {
        this.regNo = regNo;
        this.id = id;
        counter = id;
    }

    public Ticket(Ticket source) {
        this(source.regNo);
    }

    public String getRegNo() {
        return regNo;
    }

    public long getId() {
        return id;
    }
}

public class ClassesObjectsConstructorsDemo {
    public static void main(String[] args) {
        Ticket t1 = new Ticket("TN10A1");
        Ticket t2 = new Ticket(t1);
        System.out.println("Task 4: Constructor chaining and object creation");
        System.out.println("t1 => " + t1.getId() + " / " + t1.getRegNo());
        System.out.println("t2 => " + t2.getId() + " / " + t2.getRegNo());
    }
}
