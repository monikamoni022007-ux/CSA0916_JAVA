// File: Q2/UncheckedAlternativeDemo.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 2: Alternative Concept - Unchecked Custom Exceptions (RuntimeException)
 * 
 * Demonstrates:
 * 1. How method signatures change: 'throws' clauses are NO LONGER mandatory.
 * 2. Calling code is not forced to catch or declare the exceptions.
 * 3. Trade-offs: cleaner signatures vs. risk of forgotten error handlers leading to crashes.
 */

// Unchecked custom exception extending RuntimeException
class UncheckedExamException extends RuntimeException {
    public UncheckedExamException(String message) { super(message); }
    public UncheckedExamException(String message, Throwable cause) { super(message, cause); }
}

class UncheckedStorageException extends UncheckedExamException {
    public UncheckedStorageException(String message, Throwable cause) { super(message, cause); }
}

public class UncheckedAlternativeDemo {

    // Notice: NO 'throws UncheckedStorageException' required in method signature!
    public static void saveUnchecked(String fileName, long sizeKb) {
        if (sizeKb > 500) {
            throw new UncheckedStorageException("Storage quota exceeded", new java.io.IOException("No disk space"));
        }
        System.out.println("Saved unchecked: " + fileName);
    }

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("   ALTERNATIVE CONCEPT: UNCHECKED RUNTIME EXCEPTIONS     ");
        System.out.println("=========================================================");

        // Compiler does NOT enforce try-catch here!
        try {
            saveUnchecked("test_file.pdf", 800);
        } catch (UncheckedExamException e) {
            System.out.println("Caught unchecked exception: " + e.getMessage());
            System.out.println("Root Cause: " + e.getCause().getMessage());
        }

        System.out.println("\n[Analysis]:");
        System.out.println("- Method signature does not need 'throws'");
        System.out.println("- Caller is not forced by javac to handle the exception");
        System.out.println("- In enterprise portals, checked exceptions are preferred at API boundaries");
        System.out.println("  to prevent silent unhandled crashes at runtime.");
    }
}

