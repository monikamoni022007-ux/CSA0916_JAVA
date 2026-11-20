// File: Q3/ManualSalesReportGenerator.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 3: Resource Safety & Debugging
 * Implementation 1: Manual try-catch-finally with simulated resource leakage & logging
 * 
 * Demonstrates:
 * 1. The classic bug: placing reader.close() at the end of the try block.
 * 2. When NumberFormatException occurs on corrupt data, close() is bypassed, leaking the file handle.
 * 3. The defensive manual approach using nested finally blocks.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManualSalesReportGenerator {
    private static final Logger LOGGER = Logger.getLogger(ManualSalesReportGenerator.class.getName());

    /**
     * Naive Manual Approach (Buggy / Leaky):
     * When an exception occurs on line 2 (NumberFormatException), reader.close() at the end
     * of the try block is bypassed. This leaks the open file handle.
     *
     * @param storeFiles  List of store sales file paths
     * @param summaryPath Destination summary file path
     * @return Number of file handles leaked
     */
    public static int generateReportLeaky(List<String> storeFiles, String summaryPath) {
        int leakedHandles = 0;
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(summaryPath));
            double grandTotal = 0.0;

            for (String file : storeFiles) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        double sale = Double.parseDouble(line.trim());
                        grandTotal += sale;
                    }
                    // FLAW: If an exception occurs above, this close() is BYPASSED!
                    reader.close();
                } catch (Exception e) {
                    leakedHandles++;
                    LOGGER.log(Level.WARNING, "[MANUAL-LEAK LOG] Failure processing file '" + file
                            + "': " + e.getClass().getSimpleName() + " - " + e.getMessage()
                            + " (File descriptor LEAKED!)");
                }
            }
            writer.write("Grand Total Sales: Rs." + grandTotal + "\n");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Summary writer error: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {}
            }
        }
        return leakedHandles;
    }

    /**
     * Defensive Manual Approach:
     * Uses nested try-finally to guarantee close() is called, but requires verbose
     * and error-prone boilerplate code.
     */
    public static void generateReportManualSafe(List<String> storeFiles, String summaryPath) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(summaryPath));
            double grandTotal = 0.0;
            for (String file : storeFiles) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        grandTotal += Double.parseDouble(line.trim());
                    }
                } catch (IOException | NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "[MANUAL-SAFE LOG] Error in file " + file + ": " + e.getMessage());
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException closeEx) {
                            LOGGER.log(Level.SEVERE, "Close failed for " + file, closeEx);
                        }
                    }
                }
            }
            writer.write("Grand Total Sales: Rs." + grandTotal + "\n");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Writer initialization failed", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    LOGGER.severe(e.getMessage());
                }
            }
        }
    }
}

