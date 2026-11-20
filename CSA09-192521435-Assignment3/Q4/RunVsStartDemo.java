// File: Q4/RunVsStartDemo.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 4: Student A Requirement - start() vs. run() Experimental Evidence
 * 
 * Demonstrates:
 * 1. Calling run() directly executes on the CALLER thread (main thread) as a normal synchronous method.
 *    No new OS thread is spawned. Operations execute sequentially with zero parallelism.
 * 2. Calling start() requests the JVM and OS to spawn a new native thread, allocate an independent
 *    call stack, and asynchronously invoke run() in parallel.
 */

public class RunVsStartDemo {

    static class DemonstrationWorker extends Thread {
        private final int id;

        DemonstrationWorker(int id) {
            super("WorkerThread-" + id);
            this.id = id;
        }

        @Override
        public void run() {
            System.out.printf("  [Task %d] Executing on Thread: '%s' (Thread ID: %d)%n",
                    id, Thread.currentThread().getName(), Thread.currentThread().threadId());
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=========================================================");
        System.out.println("   STUDENT A: start() vs. run() EXPERIMENTAL EVIDENCE    ");
        System.out.println("=========================================================");

        System.out.println("\n[EXPERIMENT 1: Calling run() Directly]");
        System.out.println("Notice: All tasks report Thread Name = 'main'!");
        long startRun = System.currentTimeMillis();
        for (int i = 1; i <= 3; i++) {
            DemonstrationWorker worker = new DemonstrationWorker(i);
            // Calling run() directly - ordinary method call on current stack
            worker.run();
        }
        long durationRun = System.currentTimeMillis() - startRun;
        System.out.println("Sequential run() execution time: " + durationRun + " ms (Strictly Serial)");

        System.out.println("\n[EXPERIMENT 2: Calling start() to Spawn Real Threads]");
        System.out.println("Notice: Tasks report distinct WorkerThread names and IDs!");
        DemonstrationWorker[] asyncWorkers = new DemonstrationWorker[3];
        long startAsync = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            asyncWorkers[i] = new DemonstrationWorker(i + 1);
            // Calling start() - requests JVM to allocate new stack and spawn OS thread
            asyncWorkers[i].start();
        }
        for (DemonstrationWorker w : asyncWorkers) {
            w.join();
        }
        long durationAsync = System.currentTimeMillis() - startAsync;
        System.out.println("Parallel start() execution time: " + durationAsync + " ms (Truly Concurrent)");

        System.out.println("\n[CONCLUSION]:");
        System.out.println("- Calling run() DOES NOT create a new thread; it executes synchronously in 'main'.");
        System.out.println("- Calling start() invokes native start0(), allocating a new thread stack.");
    }
}

