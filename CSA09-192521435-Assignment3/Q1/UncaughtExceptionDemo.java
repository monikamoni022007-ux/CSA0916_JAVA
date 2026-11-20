// File: Q1/UncaughtExceptionDemo.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 1: Student B Requirement - Uncaught Exception Demonstration
 * 
 * This program intentionally allows an exception to go uncaught, demonstrating:
 * 1. How the JVM terminates the thread with a non-zero exit code.
 * 2. How the JVM formats and prints the stack trace to System.err.
 * 3. Line-by-line anatomical explanation of the resulting stack trace.
 */

public class UncaughtExceptionDemo {

    public static void parseUserAmount(String userInput) {
        // Line 16: Triggers uncaught java.lang.NumberFormatException when userInput is non-numeric
        int amount = Integer.parseInt(userInput);
        System.out.println("Parsed amount: Rs. " + amount);
    }

    public static void main(String[] args) {
        System.out.println("=== Uncaught Exception Demonstration ===");
        System.out.println("Attempting to parse invalid input 'TWO_THOUSAND' without a try-catch block...\n");
        // Line 23: Call that leads to uncaught exception
        parseUserAmount("TWO_THOUSAND");
        System.out.println("This statement will NEVER be reached!");
    }
}

