// File: Q3/SuppressedExceptionDemo.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 3: Student B Requirement - Suppressed Exception Demonstration
 * 
 * Demonstrates:
 * 1. An AutoCloseable resource whose close() method throws an exception.
 * 2. When the try block throws an exception AND close() throws an exception,
 *    try-with-resources attaches the close exception to the primary exception
 *    as a "suppressed exception", accessible via e.getSuppressed().
 * 3. Shows why this is superior to legacy try-catch-finally (where close() exceptions masked primary exceptions).
 */

import java.io.IOException;

public class SuppressedExceptionDemo {

    static class NetworkStoreFile implements AutoCloseable {
        private final String resourceId;

        public NetworkStoreFile(String resourceId) {
            this.resourceId = resourceId;
        }

        public void readData() {
            System.out.println("  [" + resourceId + "] Reading sales records from remote store...");
            // Primary exception thrown during business logic
            throw new IllegalStateException("Primary Exception: Malformed binary payload in " + resourceId);
        }

        @Override
        public void close() throws IOException {
            System.out.println("  [" + resourceId + "] Closing network connection stream...");
            // Secondary exception thrown during automatic resource cleanup
            throw new IOException("Secondary Exception: Network connection reset during close() of " + resourceId);
        }
    }

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("   STUDENT B: SUPPRESSED EXCEPTION DEMONSTRATION         ");
        System.out.println("=========================================================");

        try (NetworkStoreFile file = new NetworkStoreFile("Store#42_Server")) {
            file.readData();
        } catch (Exception e) {
            System.out.println("\n[PRIMARY EXCEPTION CAUGHT]: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());

            Throwable[] suppressed = e.getSuppressed();
            System.out.println("\n[SUPPRESSED EXCEPTIONS RETRIEVED]: Count = " + suppressed.length);
            for (int i = 0; i < suppressed.length; i++) {
                System.out.println("  -> Suppressed[" + i + "]: " + suppressed[i].getClass().getName()
                        + " | Message: " + suppressed[i].getMessage());
            }

            System.out.println("\n[ARCHITECTURAL EXPLANATION]:");
            System.out.println("In pre-Java 7 finally blocks, if close() threw an exception, it replaced and");
            System.out.println("DESTROYED the primary exception from try! Developers lost the root cause.");
            System.out.println("Try-with-resources preserves the primary exception and attaches secondary");
            System.out.println("closing exceptions via Throwable.addSuppressed().");
        }
    }
}

