// File: Q5/SeatBooking.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 5: "Complete the Code" Program from Assignment PDF Page 7
 * 
 * Context: Cinema Seat-Booking
 * 100 user threads compete for 10 seats (10 users per seat).
 * TODO 1: Make the check-then-book operation atomic so that a seat can never be sold twice.
 */

public class SeatBooking {
    private final boolean[] booked;
    private int bookingsMade = 0;

    SeatBooking(int seats) {
        booked = new boolean[seats];
    }

    // TODO 1: Make the check-then-book operation atomic so that a seat can never be
    // sold twice
    public synchronized void book(int seat) {
        if (!booked[seat]) {
            booked[seat] = true;
            bookingsMade++;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SeatBooking screen = new SeatBooking(10);
        Thread[] users = new Thread[100];
        for (int i = 0; i < users.length; i++) {
            final int seat = i % 10; // 10 users compete for every seat
            users[i] = new Thread(() -> screen.book(seat));
            users[i].start();
        }
        for (Thread t : users) t.join();
        System.out.println("Seats sold: " + screen.bookingsMade);
    }
}
