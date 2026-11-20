// File: Q6/AlternativeQueueAndTryLock.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 6: Alternative Concept - ArrayBlockingQueue & ReentrantLock.tryLock()
 * 
 * Demonstrates:
 * 1. ArrayBlockingQueue<Integer>(5) replacing manual wait()/notifyAll()
 * 2. ReentrantLock.tryLock(timeout) replacing intrinsic monitors and eliminating deadlocks
 * 3. Comparative complexity analysis between low-level monitors and modern java.util.concurrent abstractions
 */

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class AlternativeQueueAndTryLock {

    // --- Alternative Part B: ArrayBlockingQueue ---
    public static void runBlockingQueueDemo() throws InterruptedException {
        System.out.println("\n--- Alternative Part B: ArrayBlockingQueue(5) ---");
        BlockingQueue<Integer> tokenQueue = new ArrayBlockingQueue<>(5);
        int[] collected = new int[2];

        Thread cook = new Thread(() -> {
            try {
                for (int t = 1; t <= 20; t++) {
                    tokenQueue.put(t); // Automatically blocks when queue is full
                }
            } catch (InterruptedException ignored) {}
        });

        Thread[] students = new Thread[2];
        for (int s = 0; s < 2; s++) {
            final int id = s;
            students[s] = new Thread(() -> {
                try {
                    for (int k = 0; k < 10; k++) {
                        collected[id] += tokenQueue.take(); // Automatically blocks when empty
                    }
                } catch (InterruptedException ignored) {}
            });
        }

        cook.start();
        for (Thread t : students) t.start();

        cook.join();
        for (Thread t : students) t.join();

        System.out.println("ArrayBlockingQueue Tokens Collected Sum: " + (collected[0] + collected[1])
                + " (Expected: 210)");
    }

    // --- Alternative Part A: ReentrantLock.tryLock(timeout) ---
    static class LockWallet {
        final int id;
        double balance;
        final ReentrantLock lock = new ReentrantLock();

        LockWallet(int id, double balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    public static boolean transferWithTryLock(LockWallet from, LockWallet to, double amount)
            throws InterruptedException {
        long timeoutMs = 50;
        if (from.lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS)) {
            try {
                Thread.sleep(10);
                if (to.lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS)) {
                    try {
                        if (from.balance >= amount) {
                            from.balance -= amount;
                            to.balance += amount;
                            return true;
                        }
                        return false;
                    } finally {
                        to.lock.unlock();
                    }
                }
            } finally {
                from.lock.unlock();
            }
        }
        return false; // Timed out waiting for lock; avoided deadlock!
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=========================================================");
        System.out.println("   ALTERNATIVE CONCEPT: BLOCKING QUEUE & TRY-LOCK        ");
        System.out.println("=========================================================");

        // Run ArrayBlockingQueue Demo
        runBlockingQueueDemo();

        // Run tryLock Demo
        System.out.println("\n--- Alternative Part A: ReentrantLock.tryLock() Timeout ---");
        LockWallet w1 = new LockWallet(1, 500.0);
        LockWallet w2 = new LockWallet(2, 500.0);

        Thread t1 = new Thread(() -> {
            try {
                boolean ok = transferWithTryLock(w1, w2, 100.0);
                System.out.println("Transfer w1->w2: " + (ok ? "SUCCESS" : "TIMED OUT (Deadlock Avoided)"));
            } catch (InterruptedException ignored) {}
        });

        Thread t2 = new Thread(() -> {
            try {
                boolean ok = transferWithTryLock(w2, w1, 150.0);
                System.out.println("Transfer w2->w1: " + (ok ? "SUCCESS" : "TIMED OUT (Deadlock Avoided)"));
            } catch (InterruptedException ignored) {}
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("tryLock transfers safely completed with zero hanging threads.");
    }
}

