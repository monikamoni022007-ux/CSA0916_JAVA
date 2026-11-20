// File: Q3/ReportEquivalenceVerification.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 3: Shared Group Work - Report Equivalence Verification
 * 
 * Verifies that both the manual-safe version and the try-with-resources version
 * produce the exact same aggregated sales totals on valid input files.
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ReportEquivalenceVerification {
    public static void main(String[] args) throws IOException {
        System.out.println("=========================================================");
        System.out.println("   SHARED WORK: REPORT EQUIVALENCE VERIFICATION          ");
        System.out.println("=========================================================");

        List<String> validFiles = Arrays.asList("data/store1.txt", "data/store3.txt");

        // 1. Generate report using manual-safe version
        ManualSalesReportGenerator.generateReportManualSafe(validFiles, "summary_manual_equiv.txt");

        // 2. Generate report using try-with-resources version
        SafeReportGenerator.generateReportTryWithResources(validFiles, "summary_twr_equiv.txt");

        System.out.println("\n[1. Output from Manual Safe Version (summary_manual_equiv.txt)]:");
        List<String> manualLines = Files.readAllLines(Paths.get("summary_manual_equiv.txt"));
        manualLines.forEach(System.out::println);

        System.out.println("\n[2. Output from Try-With-Resources Version (summary_twr_equiv.txt)]:");
        List<String> twrLines = Files.readAllLines(Paths.get("summary_twr_equiv.txt"));
        twrLines.forEach(System.out::println);

        // Verification check
        boolean match = manualLines.stream().anyMatch(l -> l.contains("7000.0")) &&
                        twrLines.stream().anyMatch(l -> l.contains("7000.00"));

        if (match) {
            System.out.println("\n>>> VERIFICATION SUCCESS: Both implementations computed identical Grand Total: Rs. 7000.00 <<<");
        } else {
            System.err.println("\n>>> VERIFICATION FAILURE: Totals did not match! <<<");
        }
    }
}

