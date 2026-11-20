// File: Q2/OnlineExamPortal.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 2: Three-Layer Online Examination Portal Implementation
 * 
 * Demonstrates:
 * 1. User-Defined Exception Hierarchy (ExamException base and subclasses)
 * 2. Checked Exception design enforced by compiler
 * 3. Exception propagation across 3 layers: Storage -> Service -> Controller
 * 4. Exception chaining with getCause() preserving original low-level causes
 */

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

// -------------------------------------------------------------
// Exception Hierarchy
// -------------------------------------------------------------

// Base checked domain exception
class PortalExamException extends Exception {
    public PortalExamException(String message) {
        super(message);
    }
    public PortalExamException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Controller layer exception: Window is closed
class ExamWindowClosedException extends PortalExamException {
    public ExamWindowClosedException(String message) {
        super(message);
    }
}

// Service layer exception: Student is not registered
class StudentNotRegisteredException extends PortalExamException {
    public StudentNotRegisteredException(String message) {
        super(message);
    }
}

// Storage layer exception: File exceeds storage quota, wraps lower-level IOException
class PortalStorageException extends PortalExamException {
    public PortalStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

// -------------------------------------------------------------
// Layer 3: Storage Layer
// -------------------------------------------------------------
class StorageLayer {
    private static final long MAX_FILE_SIZE_KB = 500;

    /**
     * Attempts to store the file on disk. Throws PortalStorageException chained to IOException.
     */
    public void save(String fileName, long sizeKb) throws PortalStorageException {
        try {
            if (sizeKb > MAX_FILE_SIZE_KB) {
                // Low-level filesystem limitation
                throw new IOException("Disk quota exceeded: File size " + sizeKb 
                        + " KB exceeds limit of " + MAX_FILE_SIZE_KB + " KB.");
            }
            System.out.println("  [Storage Layer] Success: '" + fileName + "' (" + sizeKb + " KB) written to disk.");
        } catch (IOException ioEx) {
            // Chaining: wrap technical IOException inside business PortalStorageException
            throw new PortalStorageException("Could not store answer script '" + fileName + "'", ioEx);
        }
    }
}

// -------------------------------------------------------------
// Layer 2: Service Layer
// -------------------------------------------------------------
class ExamServiceLayer {
    private final StorageLayer storage = new StorageLayer();
    private final Set<String> registeredStudents = new HashSet<>();

    public ExamServiceLayer() {
        // Enrolled students database
        registeredStudents.add("STU_101");
        registeredStudents.add("STU_102");
        registeredStudents.add("REG2026_01");
    }

    /**
     * Validates student registration, then calls storage layer.
     * Propagates exceptions upward using throws.
     */
    public void submitAnswerScript(String studentId, String fileName, long sizeKb) 
            throws PortalExamException {
        
        if (!registeredStudents.contains(studentId)) {
            throw new StudentNotRegisteredException(
                    "Registration Verification Failed: Student ID '" + studentId + "' is not registered for course CSA09.");
        }

        // Delegate to Storage Layer
        storage.save(fileName, sizeKb);
    }
}

// -------------------------------------------------------------
// Layer 1: Controller Layer
// -------------------------------------------------------------
class ExamControllerLayer {
    private final ExamServiceLayer service = new ExamServiceLayer();
    private boolean examWindowOpen = true;

    public void setExamWindowOpen(boolean open) {
        this.examWindowOpen = open;
    }

    /**
     * Entry point for upload requests from the student UI.
     * Handles all exceptions and formats user-friendly messages.
     */
    public void handleSubmission(String studentId, String fileName, long sizeKb) {
        System.out.println("\n--------------------------------------------------");
        System.out.println("Processing submission: Student=" + studentId + ", File=" + fileName + " (" + sizeKb + " KB)");

        try {
            // Check submission window
            if (!examWindowOpen) {
                throw new ExamWindowClosedException(
                        "Submission Window Closed: The examination deadline passed at 17:00 IST.");
            }

            // Propagate request to Service Layer
            service.submitAnswerScript(studentId, fileName, sizeKb);
            System.out.println("  [UI Banner] SUCCESS: Your answer script was accepted and saved.");

        } catch (PortalExamException e) {
            // Handle and display user-facing error message
            System.out.println("  [UI Banner] ERROR: " + e.getMessage());

            // Diagnostic inspection using getCause()
            if (e.getCause() != null) {
                System.out.println("  [Diagnostic Log] Root Cause: " 
                        + e.getCause().getClass().getName() + " -> " + e.getCause().getMessage());
            }
        }
    }
}

public class OnlineExamPortal {
    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("   QUESTION 2: ONLINE EXAM PORTAL (THREE-LAYER SYSTEM)   ");
        System.out.println("=========================================================");

        ExamControllerLayer controller = new ExamControllerLayer();

        // Scenario 1: Valid Submission
        System.out.println("\n[Scenario 1: Valid Submission - Open Window, Registered, Size <= 500KB]");
        controller.handleSubmission("REG2026_01", "answer01.pdf", 120);

        // Scenario 2: File Size Exceeds Quota (Chained Exception from Storage Layer)
        System.out.println("\n[Scenario 2: Storage Failure - File Size Exceeds Quota]");
        controller.handleSubmission("REG2026_01", "answer02.pdf", 800);

        // Scenario 3: Unregistered Student (Service Layer Exception)
        System.out.println("\n[Scenario 3: Service Failure - Student Not Registered]");
        controller.handleSubmission("UNREGISTERED_999", "answer03.pdf", 250);

        // Scenario 4: Exam Window Closed (Controller Layer Exception)
        System.out.println("\n[Scenario 4: Controller Failure - Exam Window Closed]");
        controller.setExamWindowOpen(false);
        controller.handleSubmission("REG2026_01", "answer04.pdf", 150);
    }
}

