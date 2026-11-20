// File: Q2/UnchainedExceptionDemo.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 2: Student B Requirement - Evidence of What Is Lost Without Chaining
 * 
 * Demonstrates:
 * 1. What happens if the lower-level IOException is NOT passed to the SubmissionStorageException constructor.
 * 2. e.getCause() returns null.
 * 3. Calling e.getCause().getMessage() triggers an unexpected NullPointerException.
 * 4. The root cause ("Disk quota exceeded") and its origin line number are permanently erased.
 */

import java.io.IOException;

public class UnchainedExceptionDemo {

    // Storage class that FAILS to chain the underlying cause
    static class BrokenStorage {
        void saveWithoutCause(String fileName, long sizeKb) throws SubmissionStorageException {
            try {
                if (sizeKb > 500) {
                    throw new IOException("Disk quota exceeded (file size " + sizeKb + " KB)");
                }
            } catch (IOException e) {
                // FLAW: The original exception 'e' is discarded and not passed to the constructor!
                throw new SubmissionStorageException("Could not store " + fileName, null);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("   STUDENT B: OMITTING THE EXCEPTION CAUSE EXPERIMENT    ");
        System.out.println("=========================================================");

        BrokenStorage storage = new BrokenStorage();
        try {
            storage.saveWithoutCause("answer02.pdf", 800);
        } catch (ExamException e) {
            System.out.println("Captured Exception: " + e.getClass().getName());
            System.out.println("Error Message: " + e.getMessage());
            System.out.println("getCause() return value: " + e.getCause());

            if (e.getCause() == null) {
                System.out.println("\n[CRITICAL FAILURE OBSERVATION]:");
                System.out.println("1. e.getCause() returned NULL because the cause was omitted.");
                System.out.println("2. Attempting to call e.getCause().getMessage() will throw NullPointerException!");
                System.out.println("3. The technical root cause ('Disk quota exceeded') is completely lost to developers.");
            }
        }
    }
}

