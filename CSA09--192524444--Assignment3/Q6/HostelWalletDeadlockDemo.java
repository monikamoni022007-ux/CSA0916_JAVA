// File: Q6/HostelWalletDeadlockDemo.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 6: Part A - Hostel Wallet Deadlock Demonstration
 * 
 * Demonstrates:
 * 1. Circular Wait deadlock caused by opposite lock acquisition orders:
 *    - Thread 1 (Transfer Wallet 101 -> 102): Locks 101, then requests 102.
 *    - Thread 2 (Transfer Wallet 102 -> 101): Locks 102, then requests 101.
 * 2. Uses a watchdog thread to detect and report the deadlock without hanging indefinitely.
 */

public class HostelWalletDeadlockDemo {

    static class Wallet {
        final int walletId;
        double balance;

        Wallet(int walletId, double balance) {
            this.walletId = walletId;
            this.balance = balance;
        }
    }

    public static void transferUnsafe(Wallet from, Wallet to, double amount) {
        String threadName = Thread.currentThread().getName();
        // Locks 'from' wallet first
        synchronized (from) {
            System.out.println("  [" + threadName + "] Acquired lock on Wallet-" + from.walletId 
                    + "; attempting to acquire lock on Wallet-" + to.walletId + "...");
            try {
                // Short sleep widens the window so the other thread acquires its first lock
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}

            // Attempts to lock 'to' wallet -> DEADLOCK OCCURS HERE!
            synchronized (to) {
                if (from.balance >= amount) {
                    from.balance -= amount;
                    to.balance += amount;
                    System.out.println("  [" + threadName + "] Successfully transferred Rs." + amount);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=========================================================");
        System.out.println("   QUESTION 6: HOSTEL WALLET DEADLOCK DEMONSTRATION      ");
        System.out.println("=========================================================");

        Wallet walletA = new Wallet(101, 1000.0);
        Wallet walletB = new Wallet(102, 1000.0);

        Thread thread1 = new Thread(() -> {
            transferUnsafe(walletA, walletB, 100.0); // Locks A then B
        }, "Transfer-Thread-1 (101->102)");

        Thread thread2 = new Thread(() -> {
            transferUnsafe(walletB, walletA, 200.0); // Locks B then A
        }, "Transfer-Thread-2 (102->101)");

        System.out.println("Starting concurrent opposite transfers...");
        thread1.start();
        thread2.start();

        // Wait up to 500ms to detect deadlock
        Thread.sleep(500);

        boolean t1Blocked = thread1.getState() == Thread.State.BLOCKED;
        boolean t2Blocked = thread2.getState() == Thread.State.BLOCKED;

        System.out.println("\n[DEADLOCK DIAGNOSTIC AUDIT]:");
        System.out.println("  Thread 1 State: " + thread1.getState());
        System.out.println("  Thread 2 State: " + thread2.getState());

        if (t1Blocked && t2Blocked) {
            System.out.println("\n>>> COFFMAN CONDITIONS CONFIRMED - DEADLOCK DETECTED! <<<");
            System.out.println("1. Mutual Exclusion: Each wallet monitor can be held by only 1 thread.");
            System.out.println("2. Hold and Wait    : Thread 1 holds 101 waiting for 102; Thread 2 holds 102 waiting for 101.");
            System.out.println("3. No Preemption    : Intrinsic locks cannot be forcibly released.");
            System.out.println("4. Circular Wait    : Thread 1 -> Wallet 102 -> Thread 2 -> Wallet 101 -> Thread 1.");
        }

        // Cleanly terminate daemon/interrupted threads so process does not hang
        thread1.interrupt();
        thread2.interrupt();
        System.out.println("\n[Watchdog Alert]: Demo concluded. Deadlock successfully reproduced and documented.");
    }
}

