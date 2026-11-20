// File: Q7/ParallelStreamAlternative.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 7: Alternative Concept - Parallel Streams & One-Thread-Per-File Analysis
 * 
 * Compares:
 * 1. ExecutorService FixedThreadPool (Controlled pool size p, bounded stack memory)
 * 2. Parallel Streams (files.parallelStream()) (Uses ForkJoinPool.commonPool())
 * 3. One-Thread-Per-File (Spawning 10,000 raw Thread objects -> risks OutOfMemoryError)
 */

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelStreamAlternative {

    public static void runParallelStreamDemo(List<Path> files) {
        System.out.println("\n--- Alternative Approach: files.parallelStream() ---");
        ConcurrentHashMap<String, Integer> results = new ConcurrentHashMap<>();
        ConcurrentLinkedQueue<String> failures = new ConcurrentLinkedQueue<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        long start = System.currentTimeMillis();

        files.parallelStream().forEach(filePath -> {
            try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                String id = reader.readLine();
                if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Empty file");
                int sum = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    int m = Integer.parseInt(line.trim());
                    if (m < 0 || m > 100) throw new IllegalArgumentException("Invalid mark");
                    sum += m;
                }
                results.put(id.trim(), sum);
                successCount.incrementAndGet();
            } catch (Exception ex) {
                failures.add(filePath.getFileName() + " -> " + ex.getClass().getSimpleName());
                failureCount.incrementAndGet();
            }
        });

        long duration = System.currentTimeMillis() - start;
        System.out.printf("Parallel Stream completed: Total=%d | Success=%d | Failed=%d | Time=%d ms%n",
                files.size(), successCount.get(), failureCount.get(), duration);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("   ALTERNATIVE ARCHITECTURES: PARALLEL STREAMS vs. ONE-THREAD-PER-FILE          ");
        System.out.println("================================================================================");

        Path tempDir = Files.createTempDirectory("stream_test_sample");
        List<Path> sampleFiles = new ArrayList<>();
        for (int i = 1; i <= 500; i++) {
            Path p = tempDir.resolve("sample_" + i + ".txt");
            if (i % 50 == 0) {
                Files.write(p, Arrays.asList("REG_" + i, "90", "CORRUPT", "80"));
            } else {
                Files.write(p, Arrays.asList("REG_" + i, "85", "92", "78"));
            }
            sampleFiles.add(p);
        }

        runParallelStreamDemo(sampleFiles);

        System.out.println("\n[ARCHITECTURAL EVALUATION]:");
        System.out.println("1. Parallel Streams:");
        System.out.println("   - Simple functional syntax; uses ForkJoinPool.commonPool().");
        System.out.println("   - DANGER in Production: Blocking file I/O monopolizes the shared commonPool,");
        System.out.println("     starving all other stream operations throughout the entire JVM.");
        System.out.println("2. One-Thread-Per-File (10,000 Threads):");
        System.out.println("   - Spawning 10,000 raw OS platform threads allocates: 10,000 * 1 MB stack = 10 GB RAM!");
        System.out.println("   - Leads directly to 'java.lang.OutOfMemoryError: unable to create native thread'.");
        System.out.println("3. ExecutorService FixedThreadPool (Our Chosen Architecture):");
        System.out.println("   - Strictly bounds thread stack memory to O(p * 1 MB) (e.g. 8 MB for p=8).");
        System.out.println("   - Provides dedicated thread management with graceful timeout and shutdown controls.");
    }
}

