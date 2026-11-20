// File: Q4/ThreadLifecycleStateTracker.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 4: Student A Requirement - Thread Lifecycle State Tracking
 * 
 * Demonstrates the six thread states defined in java.lang.Thread.State:
 * 1. NEW: Thread object instantiated, start() not yet called.
 * 2. RUNNABLE: Executing in JVM or ready in OS run queue.
 * 3. TIMED_WAITING: Paused with sleep() or timed join().
 * 4. WAITING: Waiting indefinitely on join() or wait().
 * 5. BLOCKED: Waiting to acquire an intrinsic monitor lock.
 * 6. TERMINATED: run() method completed.
 */

public class ThreadLifecycleStateTracker {

    private static final Object LOCK = new Object();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=========================================================");
        System.out.println("   STUDENT A: THREAD LIFECYCLE STATE TRANSITIONS         ");
        System.out.println("=========================================================");

        Thread worker = new Thread(() -> {
            try {
                // RUNNABLE -> TIMED_WAITING
                Thread.sleep(150);

                // RUNNABLE -> BLOCKED
                synchronized (LOCK) {
                    // Critical section
                }
            } catch (InterruptedException ignored) {}
        }, "LifecycleWorker");

        // State 1: NEW
        System.out.println("State 1 [After Instantation]     : " + worker.getState());

        // Lock monitor from main thread to force worker into BLOCKED later
        synchronized (LOCK) {
            worker.start();
            // State 2: RUNNABLE (or transitioning immediately)
            System.out.println("State 2 [Immediately After start()]: " + worker.getState());

            // Wait a moment for worker to enter sleep
            Thread.sleep(50);
            // State 3: TIMED_WAITING
            System.out.println("State 3 [During Thread.sleep()]  : " + worker.getState());

            // Wait for worker to finish sleep and attempt acquiring LOCK
            Thread.sleep(150);
            // State 4: BLOCKED
            System.out.println("State 4 [Waiting on Monitor Lock]: " + worker.getState());
        } // Main releases LOCK here, allowing worker to proceed

        // Wait for worker to complete
        worker.join();
        // State 5: TERMINATED
        System.out.println("State 5 [After join() completes] : " + worker.getState());
    }
}

