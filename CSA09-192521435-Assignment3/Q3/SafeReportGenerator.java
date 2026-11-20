// File: Q3/SafeReportGenerator.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 3: Resource Safety & Debugging
 * Implementation 2: Robust try-with-resources with AutoCloseable & Suppressed Exception logging
 * 
 * Demonstrates:
 * 1. Java 7+ try-with-resources ensuring deterministic resource closure.
 * 2. Nested resource declaration (outer writer, inner reader).
 * 3. Handling exceptions mid-stream without leaking any OS file handles.
 * 4. Suppressed exception detection and logging.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SafeReportGenerator {
    private static final Logger LOGGER = Logger.getLogger(SafeReportGenerator.class.getName());

    /**
     * Try-With-Resources implementation:
     * Guarantees deterministic closing of both BufferedReader and BufferedWriter
     * even if an exception occurs mid-parse, ensuring 0 leaked file descriptors.
     */
    public static void generateReportTryWithResources(List<String> storeFiles, String summaryPath) {
        double grandTotal = 0.0;
        int successFiles = 0;
        int failedFiles = 0;

        // Outer try-with-resources: writer is automatically closed at method end
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(summaryPath))) {
            writer.write("=== DAILY RETAIL CHAIN SALES SUMMARY ===\n");

            for (String file : storeFiles) {
                double storeSubtotal = 0.0;
                
                // Inner try-with-resources: automatically closes reader before catch block runs!
                try (BufferedReader reader = Files.newBufferedReader(Paths.get(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        storeSubtotal += Double.parseDouble(line.trim());
                    }
                    grandTotal += storeSubtotal;
                    successFiles++;
                    writer.write(String.format("Store File %-22s : Rs. %.2f%n", file, storeSubtotal));

                } catch (IOException | NumberFormatException e) {
                    failedFiles++;
                    LOGGER.log(Level.WARNING,
                            "[TRY-WITH-RESOURCES LOG] Error in file '" + file + "': "
                                    + e.getClass().getSimpleName() + " - " + e.getMessage()
                                    + " (Stream was safely auto-closed!)");

                    // Inspect for suppressed exceptions
                    for (Throwable suppressed : e.getSuppressed()) {
                        LOGGER.log(Level.SEVERE, "  -> Suppressed Close Exception: " + suppressed);
                    }
                    writer.write(String.format("Store File %-22s : FAILED (%s)%n",
                            file, e.getClass().getSimpleName()));
                }
            }

            writer.write("--------------------------------------------------\n");
            writer.write(String.format("Processed: %d OK, %d Failed | Grand Total: Rs. %.2f%n",
                    successFiles, failedFiles, grandTotal));
            writer.flush();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Critical failure writing summary file: " + summaryPath, e);
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("=========================================================");
        System.out.println("   QUESTION 3: DAILY SALES REPORT - RESOURCE SAFETY      ");
        System.out.println("=========================================================");

        List<String> storeFiles = Arrays.asList(
                "data/store1.txt",
                "data/store2_corrupt.txt",
                "data/store3.txt",
                "data/store4_missing.txt"
        );

        System.out.println("\n[PART 1: Testing Naive Manual-Close Version]");
        int leakedHandles = ManualSalesReportGenerator.generateReportLeaky(storeFiles, "summary_leaky.txt");
        System.out.println("Files left open (leaked) in naive manual version: " + leakedHandles);

        System.out.println("\n[PART 2: Testing Robust Try-With-Resources Version]");
        generateReportTryWithResources(storeFiles, "summary_twr.txt");

        System.out.println("\n--- Generated Try-With-Resources Summary Report ---");
        Files.lines(Paths.get("summary_twr.txt")).forEach(System.out::println);

        System.out.println("\n[PART 3: Resource Handle Audit]");
        System.out.println("Naive Manual Approach Leaked Descriptors : " + leakedHandles);
        System.out.println("Try-With-Resources Leaked Descriptors     : 0 (Guaranteed Closed)");
    }
}

