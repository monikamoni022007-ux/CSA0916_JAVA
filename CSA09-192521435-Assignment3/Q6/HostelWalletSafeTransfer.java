// File: Q6/HostelWalletSafeTransfer.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 6: Part A - Deadlock Prevention via Consistent Lock Ordering
 * 
 * Solution:
 * Implements a global lock-ordering rule based on unique immutable wallet IDs.
 * Both threads ALWAYS lock min(idFrom, idTo) first, then max(idFrom, idTo).
 * This mathematically eliminates the 4th Coffman condition (Circular Wait),
 * guaranteeing that deadlocks can NEVER occur.
 */

public class HostelWalletSafeTransfer {

    static class SafeWallet {
        final int walletId;
        double balance;

        SafeWallet(int walletId, double balance) {
            this.walletId = walletId;
            this.balance = balance;
        }
    }

    /**
     * Deadlock-free transfer using global lock ordering by walletId.
     */
    public static void transferSafe(SafeWallet from, SafeWallet to, double amount) {
        String threadName = Thread.currentThread().getName();

        if (from.walletId == to.walletId) {
            System.out.println("Cannot transfer to the same wallet.");
            return;
        }

        // Global Lock-Ordering Rule: Always acquire lock with smaller ID first
        SafeWallet firstLock = (from.walletId < to.walletId) ? from : to;
        SafeWallet secondLock = (from.walletId < to.walletId) ? to : from;

        synchronized (firstLock) {
            System.out.printf("  [%s] Acquired first lock on Wallet-%d%n", threadName, firstLock.walletId);
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}

            synchronized (secondLock) {
                System.out.printf("  [%s] Acquired second lock on Wallet-%d%n", threadName, secondLock.walletId);
                if (from.balance >= amount) {
                    from.balance -= amount;
                    to.balance += amount;
                    System.out.printf("  [%s] SUCCESS: Transferred Rs.%.2f from Wallet-%d to Wallet-%d%n",
                            threadName, amount, from.walletId, to.walletId);
                } else {
                    System.out.printf("  [%s] FAILED: Insufficient balance in Wallet-%d%n",
                            threadName, from.walletId);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=========================================================");
        System.out.println("   QUESTION 6: DEADLOCK-FREE LOCK ORDERING TRANSFERS     ");
        System.out.println("=========================================================");

        SafeWallet wallet101 = new SafeWallet(101, 1000.0);
        SafeWallet wallet102 = new SafeWallet(102, 1000.0);

        // Thread 1 transfers 101 -> 102
        Thread t1 = new Thread(() -> {
            transferSafe(wallet101, wallet102, 150.0);
        }, "Worker-1 (101->102)");

        // Thread 2 transfers 102 -> 101 concurrently
        Thread t2 = new Thread(() -> {
            transferSafe(wallet102, wallet101, 300.0);
        }, "Worker-2 (102->101)");

        System.out.println("Executing bidirectional transfers concurrently with consistent lock order...");
        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("\n>>> TRANSFERS COMPLETED SMOOTHLY WITH ZERO DEADLOCK <<<");
        System.out.printf("Final Wallet-101 Balance: Rs.%.2f (Expected: Rs.1150.00)%n", wallet101.balance);
        System.out.printf("Final Wallet-102 Balance: Rs.%.2f (Expected: Rs.850.00)%n", wallet102.balance);
    }
}

