// File: Q1/AlternativeErrorCodeAtm.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 1: Alternative Concept Demonstration - Procedural Pre-Validation & Error Codes
 * 
 * Demonstrates:
 * - Avoiding exceptions entirely using defensive checks (File.exists(), Scanner.hasNextLong(), Scanner.hasNextInt())
 * - Returning integer status codes (-1, -2, -3, -4) instead of throwing exceptions
 * - Analysis of readability, code maintenance overhead, and performance differences
 */

import java.io.File;
import java.util.Scanner;

public class AlternativeErrorCodeAtm {

    public static final int ERR_FILE_MISSING = -1;
    public static final int ERR_INVALID_ACCOUNT = -2;
    public static final int ERR_INVALID_AMOUNT = -3;
    public static final int ERR_NEGATIVE_AMOUNT = -4;
    public static final int ERR_INSUFFICIENT_FUNDS = -5;
    public static final int SUCCESS = 0;

    /**
     * Attempts withdrawal using procedural pre-checks and error codes instead of exceptions.
     * 
     * @param cardConfigFile  Path to config file
     * @param accountToken    Raw input string for account
     * @param amountToken     Raw input string for amount
     * @param balance         Available balance
     * @return Integer error code or SUCCESS (0)
     */
    public static int withdrawWithChecks(String cardConfigFile, String accountToken, 
                                        String amountToken, double balance) {
        
        System.out.println("\n[Alternative Concept] Processing withdrawal with return error codes...");

        // Pre-check 1: Check file existence and readability without opening stream
        File configFile = new File(cardConfigFile);
        if (!configFile.exists() || !configFile.canRead()) {
            System.out.println("  -> Error: Card reader file missing or unreadable.");
            return ERR_FILE_MISSING; // -1
        }

        // Pre-check 2: Validate account number syntax using Scanner lookahead
        Scanner accScanner = new Scanner(accountToken);
        if (!accScanner.hasNextLong()) {
            System.out.println("  -> Error: Account number is not a valid integer.");
            return ERR_INVALID_ACCOUNT; // -2
        }
        long accountNo = accScanner.nextLong();

        // Pre-check 3: Validate amount syntax using Scanner lookahead
        Scanner amtScanner = new Scanner(amountToken);
        if (!amtScanner.hasNextInt()) {
            System.out.println("  -> Error: Amount is not a valid integer.");
            return ERR_INVALID_AMOUNT; // -3
        }
        int amount = amtScanner.nextInt();

        // Pre-check 4: Validate positive range
        if (amount <= 0) {
            System.out.println("  -> Error: Amount must be strictly positive.");
            return ERR_NEGATIVE_AMOUNT; // -4
        }

        // Pre-check 5: Validate sufficient balance
        if (amount > balance) {
            System.out.println("  -> Error: Insufficient balance.");
            return ERR_INSUFFICIENT_FUNDS; // -5
        }

        // Success Path
        double remaining = balance - amount;
        System.out.println("  -> [SUCCESS] Cash Dispensed: Rs. " + amount + " | Remaining: Rs. " + remaining);
        return SUCCESS; // 0
    }

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("   ALTERNATIVE CONCEPT: PRE-VALIDATION & ERROR CODES     ");
        System.out.println("=========================================================");

        // Test normal scenario
        int code1 = withdrawWithChecks("card_reader.cfg", "9876543210", "2500", 10000.0);
        System.out.println("Result Code: " + code1);

        // Test letters input
        int code2 = withdrawWithChecks("card_reader.cfg", "9876543210", "TWO_THOUSAND", 10000.0);
        System.out.println("Result Code: " + code2 + " (ERR_INVALID_AMOUNT)");

        // Test negative input
        int code3 = withdrawWithChecks("card_reader.cfg", "9876543210", "-500", 10000.0);
        System.out.println("Result Code: " + code3 + " (ERR_NEGATIVE_AMOUNT)");

        // Test overdraft
        int code4 = withdrawWithChecks("card_reader.cfg", "9876543210", "25000", 10000.0);
        System.out.println("Result Code: " + code4 + " (ERR_INSUFFICIENT_FUNDS)");

        // Test missing file
        int code5 = withdrawWithChecks("missing_reader.cfg", "9876543210", "1000", 10000.0);
        System.out.println("Result Code: " + code5 + " (ERR_FILE_MISSING)");
    }
}

