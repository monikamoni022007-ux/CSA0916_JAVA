// File: Q5/UnsynchronizedSeatBooking.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 5: PDF Requirement - Unsynchronized Execution (10 Runs)
 * 
 * "Run the program first with TODO 1 written WITHOUT the synchronized keyword
 *  and record the result over 10 runs."
 * 
 * Demonstrates:
 * 1. The check-then-act race condition on boolean[] booked and bookingsMade++.
 * 2. Why more than 10 seats are sold when 100 threads compete for 10 seats.
 */

public class UnsynchronizedSeatBooking {
    private final boolean[] booked;
    private int bookingsMade = 0;

    UnsynchronizedSeatBooking(int seats) {
        booked = new boolean[seats];
    }

    // Written WITHOUT synchronized keyword as instructed by PDF
    public void book(int seat) {
        if (!booked[seat]) {
            // Context switch or thread interleaving window
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {}
            booked[seat] = true;
            bookingsMade++;
        }
    }

    public int getBookingsMade() {
        return bookingsMade;
    }

    public static int runSingleTest() throws InterruptedException {
        UnsynchronizedSeatBooking screen = new UnsynchronizedSeatBooking(10);
        Thread[] users = new Thread[100];
        for (int i = 0; i < users.length; i++) {
            final int seat = i % 10;
            users[i] = new Thread(() -> screen.book(seat));
            users[i].start();
        }
        for (Thread t : users) {
            t.join();
        }
        return screen.getBookingsMade();
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=========================================================");
        System.out.println("   QUESTION 5: UNSYNCHRONIZED RUNS (10 REPEATED RUNS)    ");
        System.out.println("=========================================================");
        System.out.println("Expected correct seats sold: 10 (out of 10 available seats)");
        System.out.println("Compromised results due to race condition:\n");

        int doubleBookingRuns = 0;
        for (int run = 1; run <= 10; run++) {
            int sold = runSingleTest();
            boolean overbooked = sold > 10;
            if (overbooked) {
                doubleBookingRuns++;
            }
            System.out.printf("  Run %2d: Seats sold = %2d / 10 %s%n",
                    run, sold, overbooked ? "<- DOUBLE BOOKING OCCURRED! (Race Condition)" : "<- Normal");
        }

        System.out.println("\n[SUMMARY]:");
        System.out.println("Double booking detected in " + doubleBookingRuns + " / 10 runs.");
        System.out.println("Cause: Non-atomic check-then-act (if (!booked[seat])) and non-atomic counter (bookingsMade++).");
    }
}

