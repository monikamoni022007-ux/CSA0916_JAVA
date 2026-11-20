// File: Q4/StudentBExperiments.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 4: Student B Requirements - Behavioural Experiments
 * 
 * Includes:
 * 1. 10 Repeated Runs: Demonstrates non-deterministic finish order across OS scheduler time slices.
 * 2. Thread Priority Experiment: MIN_PRIORITY (1) vs MAX_PRIORITY (10) to observe scheduler bias.
 * 3. Daemon Thread Experiment: setDaemon(true) showing abrupt thread termination upon main exit.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StudentBExperiments {

    // Simple worker that appends its ID to finishOrder upon completion
    static class StationWorker extends Thread {
        private final int id;
        private final int scripts;
        private final List<Integer> finishOrder;

        public StationWorker(int id, int scripts, List<Integer> finishOrder) {
            super("Station-" + id);
            this.id = id;
            this.scripts = scripts;
            this.finishOrder = finishOrder;
        }

        @Override
        public void run() {
            for (int i = 0; i < scripts; i++) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ignored) {}
            }
            synchronized (finishOrder) {
                finishOrder.add(id);
            }
        }
    }

    public static void runTenIterations() throws InterruptedException {
        System.out.println("\n[EXPERIMENT 1: Ten Repeated Runs to Record Completion Order]");
        for (int run = 1; run <= 10; run++) {
            List<Integer> finishOrder = Collections.synchronizedList(new ArrayList<>());
            StationWorker[] workers = {
                new StationWorker(1, 20, finishOrder),
                new StationWorker(2, 20, finishOrder),
                new StationWorker(3, 20, finishOrder),
                new StationWorker(4, 20, finishOrder)
            };

            for (StationWorker w : workers) w.start();
            for (StationWorker w : workers) w.join();

            System.out.println("  Run " + String.format("%2d", run) + " Finish Order: " + finishOrder);
        }
    }

    public static void runPriorityExperiment() throws InterruptedException {
        System.out.println("\n[EXPERIMENT 2: Thread Priorities (Station 1: MIN, Station 4: MAX)]");
        List<Integer> finishOrder = Collections.synchronizedList(new ArrayList<>());
        StationWorker s1 = new StationWorker(1, 25, finishOrder);
        StationWorker s2 = new StationWorker(2, 25, finishOrder);
        StationWorker s3 = new StationWorker(3, 25, finishOrder);
        StationWorker s4 = new StationWorker(4, 25, finishOrder);

        // Set priorities
        s1.setPriority(Thread.MIN_PRIORITY); // 1
        s2.setPriority(Thread.NORM_PRIORITY); // 5
        s3.setPriority(Thread.NORM_PRIORITY); // 5
        s4.setPriority(Thread.MAX_PRIORITY); // 10

        s1.start(); s2.start(); s3.start(); s4.start();
        s1.join(); s2.join(); s3.join(); s4.join();

        System.out.println("  Priority Experiment Finish Order: " + finishOrder);
        System.out.println("  Observation: Thread priority is only a hint to OS scheduler; it does not guarantee execution order.");
    }

    public static void runDaemonExperiment() throws InterruptedException {
        System.out.println("\n[EXPERIMENT 3: Daemon Thread Behaviour]");
        System.out.println("  Station 4 is configured as DAEMON (setDaemon(true)).");
        System.out.println("  Main thread will join only on Stations 1, 2, and 3, and then exit.");

        Thread daemonStation = new Thread(() -> {
            int count = 0;
            while (count < 50) {
                count++;
                System.out.println("    [Daemon Station 4] Processed script " + count + "...");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("    [Daemon Station 4] COMPLETED ALL 50!");
        }, "DaemonStation4");

        daemonStation.setDaemon(true); // Marked as daemon before start()
        daemonStation.start();

        // Let non-daemon worker run briefly (e.g., 30 ms) then finish
        Thread userWorker = new Thread(() -> {
            try {
                Thread.sleep(30);
            } catch (InterruptedException ignored) {}
            System.out.println("  [User Worker Thread] Non-daemon task finished.");
        }, "UserWorker");

        userWorker.start();
        userWorker.join(); // Wait only for non-daemon worker

        System.out.println("  [Main Thread] Exiting now. Watch Daemon Station 4 terminate prematurely!");
        // As main exits, JVM terminates immediately, killing daemonStation
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=========================================================");
        System.out.println("   STUDENT B: THREAD BEHAVIOURAL EXPERIMENTS             ");
        System.out.println("=========================================================");

        runTenIterations();
        runPriorityExperiment();
        runDaemonExperiment();
    }
}

