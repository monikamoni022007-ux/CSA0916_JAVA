// File: Q4/ExecutorAlternativeScanner.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 4: Alternative Concept - Fixed Thread Pool via ExecutorService
 * 
 * Demonstrates:
 * 1. Running scanner stations using Executors.newFixedThreadPool(4).
 * 2. Submitting tasks as Callable<Integer> and aggregating Future results.
 * 3. Bounding thread stack native memory to O(p * StackSize) instead of O(n * StackSize).
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ExecutorAlternativeScanner {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("=========================================================");
        System.out.println("   ALTERNATIVE CONCEPT: EXECUTOR SERVICE THREAD POOL     ");
        System.out.println("=========================================================");

        int[] batchSizes = {25, 30, 20, 25};
        int poolSize = 4;
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        List<Future<Integer>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < batchSizes.length; i++) {
            final int stationId = i + 1;
            final int count = batchSizes[i];

            Callable<Integer> task = () -> {
                String threadName = Thread.currentThread().getName();
                System.out.printf("  [Pool Thread: %s] Processing station %d batch of %d scripts...%n",
                        threadName, stationId, count);
                for (int s = 1; s <= count; s++) {
                    Thread.sleep(10);
                }
                return count;
            };

            futures.add(executor.submit(task));
        }

        int totalScanned = 0;
        for (Future<Integer> f : futures) {
            totalScanned += f.get(); // Blocks until individual station callable completes
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("\n>>> All 4 Stations Finished via ExecutorService <<<");
        System.out.println("Total Scripts Scanned: " + totalScanned);
        System.out.println("Total Execution Time : " + duration + " ms");
    }
}

