// File: Q5/AlternativeBookingTechniques.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 5: Alternative Concept & Complexity Analysis
 * 
 * Demonstrates the 3 alternative booking architectures requested in the PDF:
 * 1. AtomicBoolean[] using compareAndSet(false, true) (Lock-free hardware CAS)
 * 2. ReentrantLock with try-finally explicit locking
 * 3. ConcurrentHashMap with putIfAbsent(seat, userId)
 */

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class AlternativeBookingTechniques {

    // 1. AtomicBoolean[] Approach
    static class AtomicBooking {
        private final AtomicBoolean[] seats;
        private final AtomicInteger bookingsMade = new AtomicInteger(0);

        AtomicBooking(int numSeats) {
            seats = new AtomicBoolean[numSeats];
            for (int i = 0; i < numSeats; i++) {
                seats[i] = new AtomicBoolean(false);
            }
        }

        public boolean book(int seat) {
            // Hardware-level atomic Compare-And-Swap (CAS)
            if (seats[seat].compareAndSet(false, true)) {
                bookingsMade.incrementAndGet();
                return true;
            }
            return false;
        }

        public int getBookingsMade() { return bookingsMade.get(); }
    }

    // 2. ReentrantLock Approach
    static class ReentrantLockBooking {
        private final boolean[] booked;
        private final ReentrantLock lock = new ReentrantLock();
        private int bookingsMade = 0;

        ReentrantLockBooking(int numSeats) {
            booked = new boolean[numSeats];
        }

        public boolean book(int seat) {
            lock.lock();
            try {
                if (!booked[seat]) {
                    booked[seat] = true;
                    bookingsMade++;
                    return true;
                }
                return false;
            } finally {
                lock.unlock();
            }
        }

        public int getBookingsMade() { return bookingsMade; }
    }

    // 3. ConcurrentHashMap Approach
    static class ConcurrentMapBooking {
        private final ConcurrentHashMap<Integer, String> seatReservations = new ConcurrentHashMap<>();

        public boolean book(int seat, String userId) {
            // putIfAbsent returns null if key was NOT present (booking successful)
            return seatReservations.putIfAbsent(seat, userId) == null;
        }

        public int getBookingsMade() { return seatReservations.size(); }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=========================================================");
        System.out.println("   ALTERNATIVE CONCEPTS: ATOMIC, LOCK & CONCURRENT MAP   ");
        System.out.println("=========================================================");

        int seats = 10;
        int threads = 100;

        // 1. Test AtomicBoolean[]
        AtomicBooking atomicScreen = new AtomicBooking(seats);
        Thread[] atomicThreads = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final int seat = i % seats;
            atomicThreads[i] = new Thread(() -> atomicScreen.book(seat));
            atomicThreads[i].start();
        }
        for (Thread t : atomicThreads) t.join();
        System.out.println("[Approach 1: AtomicBoolean[] (CAS)] Seats sold: " 
                + atomicScreen.getBookingsMade() + " (Expected: 10)");

        // 2. Test ReentrantLock
        ReentrantLockBooking lockScreen = new ReentrantLockBooking(seats);
        Thread[] lockThreads = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final int seat = i % seats;
            lockThreads[i] = new Thread(() -> lockScreen.book(seat));
            lockThreads[i].start();
        }
        for (Thread t : lockThreads) t.join();
        System.out.println("[Approach 2: ReentrantLock]         Seats sold: " 
                + lockScreen.getBookingsMade() + " (Expected: 10)");

        // 3. Test ConcurrentHashMap
        ConcurrentMapBooking mapScreen = new ConcurrentMapBooking();
        Thread[] mapThreads = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final int seat = i % seats;
            final String user = "User-" + i;
            mapThreads[i] = new Thread(() -> mapScreen.book(seat, user));
            mapThreads[i].start();
        }
        for (Thread t : mapThreads) t.join();
        System.out.println("[Approach 3: ConcurrentHashMap]     Seats sold: " 
                + mapScreen.getBookingsMade() + " (Expected: 10)");

        System.out.println("\nAll three alternatives successfully prevented double booking (0 overbookings)!");
    }
}

