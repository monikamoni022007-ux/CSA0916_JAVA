// File: Q4/ParallelScannerSimulation.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 4: Parallel Answer-Script Scanning Simulation
 * 
 * Demonstrates:
 * 1. Creating threads by extending java.lang.Thread
 * 2. Creating threads by implementing java.lang.Runnable
 * 3. Thread lifecycle control methods: start(), sleep(), join(), isAlive()
 * 4. Tracking and reporting scripts scanned across 4 concurrent scanner stations
 */

// Approach 1: Extending Thread
class ScannerStationThread extends Thread {
    private final int stationId;
    private final int batchSize;
    private int scannedCount = 0;

    public ScannerStationThread(int stationId, int batchSize) {
        super("Station-Thread-" + stationId);
        this.stationId = stationId;
        this.batchSize = batchSize;
    }

    @Override
    public void run() {
        System.out.println("  [" + getName() + "] Started scanning batch of " + batchSize + " scripts...");
        for (int i = 1; i <= batchSize; i++) {
            scannedCount++;
            try {
                // Simulate optical scanning latency per script
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println("  [" + getName() + "] Interrupted!");
                Thread.currentThread().interrupt();
                return;
            }
        }
        System.out.println("  [" + getName() + "] COMPLETED scanning " + scannedCount + " scripts.");
    }

    public int getScannedCount() {
        return scannedCount;
    }
}

// Approach 2: Implementing Runnable
class ScannerStationRunnable implements Runnable {
    private final int stationId;
    private final int batchSize;
    private int scannedCount = 0;

    public ScannerStationRunnable(int stationId, int batchSize) {
        this.stationId = stationId;
        this.batchSize = batchSize;
    }

    @Override
    public void run() {
        String workerName = Thread.currentThread().getName();
        System.out.println("  [" + workerName + " (Runnable)] Started processing " + batchSize + " scripts...");
        for (int i = 1; i <= batchSize; i++) {
            scannedCount++;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println("  [" + workerName + "] Interrupted!");
                Thread.currentThread().interrupt();
                return;
            }
        }
        System.out.println("  [" + workerName + " (Runnable)] COMPLETED processing " + scannedCount + " scripts.");
    }

    public int getScannedCount() {
        return scannedCount;
    }
}

public class ParallelScannerSimulation {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=========================================================");
        System.out.println("   QUESTION 4: PARALLEL ANSWER-SCRIPT SCANNING SYSTEM    ");
        System.out.println("=========================================================");

        int[] batchSizes = {25, 30, 20, 25}; // Total = 100 scripts

        // -------------------------------------------------------------
        // PART 1: EXTENDING THREAD
        // -------------------------------------------------------------
        System.out.println("\n--- PART 1: Scanner Stations via Extending java.lang.Thread ---");
        ScannerStationThread[] threadStations = new ScannerStationThread[4];
        for (int i = 0; i < 4; i++) {
            threadStations[i] = new ScannerStationThread(i + 1, batchSizes[i]);
        }

        // Check isAlive() before start()
        System.out.println("Station 1 isAlive() before start(): " + threadStations[0].isAlive());

        // Launch all 4 station threads concurrently
        for (ScannerStationThread st : threadStations) {
            st.start();
        }

        // Check isAlive() immediately after start()
        System.out.println("Station 1 isAlive() immediately after start(): " + threadStations[0].isAlive());

        // Wait for all station threads to finish using join()
        int totalFromThreads = 0;
        for (ScannerStationThread st : threadStations) {
            st.join(); // Main thread pauses until st enters TERMINATED state
            totalFromThreads += st.getScannedCount();
        }

        // Check isAlive() after join()
        System.out.println("Station 1 isAlive() after join(): " + threadStations[0].isAlive());
        System.out.println(">>> Consolidated Total Scanned (Thread Approach): " + totalFromThreads + " scripts <<<\n");

        // -------------------------------------------------------------
        // PART 2: IMPLEMENTING RUNNABLE
        // -------------------------------------------------------------
        System.out.println("--- PART 2: Scanner Stations via Implementing java.lang.Runnable ---");
        ScannerStationRunnable[] tasks = new ScannerStationRunnable[4];
        Thread[] workers = new Thread[4];

        for (int i = 0; i < 4; i++) {
            tasks[i] = new ScannerStationRunnable(i + 1, batchSizes[i]);
            workers[i] = new Thread(tasks[i], "Station-Worker-" + (i + 1));
            workers[i].start();
        }

        int totalFromRunnable = 0;
        for (int i = 0; i < 4; i++) {
            workers[i].join();
            totalFromRunnable += tasks[i].getScannedCount();
        }

        System.out.println(">>> Consolidated Total Scanned (Runnable Approach): " + totalFromRunnable + " scripts <<<");
    }
}

