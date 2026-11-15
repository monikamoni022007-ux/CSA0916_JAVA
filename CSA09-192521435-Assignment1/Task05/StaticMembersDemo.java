package task05;

public class StaticMembersDemo {
    private static long ticketCounter = 1;

    public static long nextTicketId() {
        return ticketCounter++;
    }

    public static void main(String[] args) {
        System.out.println("Task 5: static ticket numbering");
        System.out.println("Ticket 1: " + nextTicketId());
        System.out.println("Ticket 2: " + nextTicketId());
        System.out.println("Ticket 3: " + nextTicketId());
    }
}
