// File: Q5/CinemaSyncComparison.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 5: Student B Requirement - Performance & Strategy Comparison
 * 
 * Compares:
 * 1. Synchronized Method (locking 'this' for entire method)
 * 2. Synchronized Block (locking only the critical section)
 * 3. Measures execution time to process 10,000 bookings under high contention
 */

public class CinemaSyncComparison {

    // Strategy 1: Synchronized Method
    static class SyncMethodCinema {
        private final boolean[] booked;
        private int bookingsMade = 0;

        SyncMethodCinema(int seats) {
            this.booked = new boolean[seats];
        }

        // Intrinsic lock on 'this' held for the entire method execution
        public synchronized void book(int seat) {
            if (!booked[seat]) {
                booked[seat] = true;
                bookingsMade++;
            }
        }

        public int getBookingsMade() { return bookingsMade; }
    }

    // Strategy 2: Synchronized Block (Critical Section Only)
    static class SyncBlockCinema {
        private final boolean[] booked;
        private int bookingsMade = 0;

        SyncBlockCinema(int seats) {
            this.booked = new boolean[seats];
        }

        public void book(int seat) {
            // Non-critical operations (boundary validation) can run concurrently outside the lock
            if (seat < 0 || seat >= booked.length) return;

            // Critical section only: acquire lock on 'this'
            synchronized (this) {
                if (!booked[seat]) {
                    booked[seat] = true;
                    bookingsMade++;
                }
            }
        }

        public int getBookingsMade() { return bookingsMade; }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=========================================================");
        System.out.println("   STUDENT B: 10,000 BOOKINGS PERFORMANCE BENCHMARK     ");
        System.out.println("=========================================================");

        int totalBookings = 10000;
        int seats = 50;

        // 1. Benchmark Synchronized Method
        SyncMethodCinema methodCinema = new SyncMethodCinema(seats);
        Thread[] methodThreads = new Thread[totalBookings];
        for (int i = 0; i < totalBookings; i++) {
            final int seat = i % seats;
            methodThreads[i] = new Thread(() -> methodCinema.book(seat));
        }

        long startMethod = System.nanoTime();
        for (Thread t : methodThreads) t.start();
        for (Thread t : methodThreads) t.join();
        long durationMethodMs = (System.nanoTime() - startMethod) / 1_000_000;

        System.out.println("\n[Strategy 1: Synchronized Method]");
        System.out.println("  Seats Booked : " + methodCinema.getBookingsMade() + " / " + seats);
        System.out.println("  Total Threads: " + totalBookings);
        System.out.println("  Execution Time: " + durationMethodMs + " ms");

        // 2. Benchmark Synchronized Block
        SyncBlockCinema blockCinema = new SyncBlockCinema(seats);
        Thread[] blockThreads = new Thread[totalBookings];
        for (int i = 0; i < totalBookings; i++) {
            final int seat = i % seats;
            blockThreads[i] = new Thread(() -> blockCinema.book(seat));
        }

        long startBlock = System.nanoTime();
        for (Thread t : blockThreads) t.start();
        for (Thread t : blockThreads) t.join();
        long durationBlockMs = (System.nanoTime() - startBlock) / 1_000_000;

        System.out.println("\n[Strategy 2: Synchronized Block (Critical Section Only)]");
        System.out.println("  Seats Booked : " + blockCinema.getBookingsMade() + " / " + seats);
        System.out.println("  Total Threads: " + totalBookings);
        System.out.println("  Execution Time: " + durationBlockMs + " ms");

        System.out.println("\n[ARCHITECTURAL ANALYSIS]:");
        System.out.println("- Synchronized Method holds the monitor for the entire call.");
        System.out.println("- Synchronized Block holds the monitor ONLY for the critical section.");
        System.out.println("- When methods contain logging, calculations, or validations, Synchronized Block");
        System.out.println("  significantly reduces lock hold time and lock contention.");
    }
}

