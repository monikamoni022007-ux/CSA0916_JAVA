// File: Q1/AtmSimulation.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 1: Handling Failure in a Bank ATM Simulation
 * 
 * Objectives:
 * - Handle card reader hardware check (FileNotFoundException - Checked)
 * - Handle user typing letters instead of numbers (NumberFormatException - Unchecked)
 * - Handle negative withdrawal amounts (IllegalArgumentException - Unchecked)
 * - Handle amount exceeding balance (ArithmeticException - Unchecked)
 * - Implement try, specific catch, multi-catch (|), and finally blocks
 * - Guarantee resource cleanup and safe ATM card ejection in the finally block
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AtmSimulation {

    /**
     * Simulates an ATM withdrawal operation with structured exception handling.
     * 
     * @param cardConfigFile  Path to the card reader hardware configuration file
     * @param accountInput    Account number string entered by user
     * @param amountInput     Withdrawal amount string entered by user
     * @param currentBalance  Available account balance
     */
    public static void withdraw(String cardConfigFile, String accountInput, 
                                String amountInput, double currentBalance) {
        
        BufferedReader reader = null;
        System.out.println("\n--------------------------------------------------");
        System.out.println(">>> Starting ATM Transaction Session <<<");

        try {
            // STEP 1: Verify Hardware Card Reader File (Checked Exception: FileNotFoundException)
            File configFile = new File(cardConfigFile);
            reader = new BufferedReader(new FileReader(configFile));
            String hardwareStatus = reader.readLine();
            System.out.println("[Hardware Check] Card Reader Status: " + hardwareStatus);

            // STEP 2: Parse Inputs (Unchecked Exception: NumberFormatException)
            long accountNumber = Long.parseLong(accountInput.trim());
            int amount = Integer.parseInt(amountInput.trim());
            System.out.println("[Input Verified] Account Number: " + accountNumber);
            System.out.println("[Input Verified] Requested Amount: Rs. " + amount);

            // STEP 3: Validate Amount is Strictly Positive (Unchecked: IllegalArgumentException)
            if (amount <= 0) {
                throw new IllegalArgumentException(
                    "Withdrawal amount must be greater than zero. Entered: Rs. " + amount);
            }

            // STEP 4: Check Account Balance (Unchecked: ArithmeticException)
            if (amount > currentBalance) {
                throw new ArithmeticException(
                    "Insufficient balance: Requested Rs. " + amount 
                    + " exceeds available balance Rs. " + currentBalance);
            }

            // STEP 5: Dispense Cash and Update Balance
            double remainingBalance = currentBalance - amount;
            System.out.println("[SUCCESS] Please collect your cash: Rs. " + amount);
            System.out.println("[SUCCESS] Remaining Account Balance: Rs. " + remainingBalance);

        } catch (FileNotFoundException e) {
            // Checked exception: specific hardware error
            System.out.println("[CATCH - FileNotFoundException] Error: Card-reader configuration file is missing!");
            System.out.println("  Details: " + e.getMessage());

        } catch (NumberFormatException e) {
            // Unchecked exception: user typed letters instead of numeric digits
            System.out.println("[CATCH - NumberFormatException] Error: Invalid input! Please enter numbers only.");
            System.out.println("  Details: " + e.getMessage());

        } catch (IllegalArgumentException | ArithmeticException e) {
            // Java 7+ Multi-Catch: handles disjoint business logic validation exceptions
            System.out.println("[CATCH - Multi-Catch (" + e.getClass().getSimpleName() + ")] Transaction Denied!");
            System.out.println("  Reason: " + e.getMessage());

        } catch (IOException e) {
            // Checked exception: general I/O stream read failure
            System.out.println("[CATCH - IOException] Hardware read failure: " + e.getMessage());

        } catch (Exception e) {
            // General top-level fallback catch for unexpected runtime exceptions
            System.out.println("[CATCH - General Exception] Unexpected system error: " + e.getMessage());

        } finally {
            // Mandatory Invariant: Execute cleanup whether transaction succeeded or failed
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    System.out.println("[FINALLY Warning] Could not close card-reader stream: " + ex.getMessage());
                }
            }
            System.out.println("[FINALLY] Security Cleanup: ATM Card Ejected Safely.");
            System.out.println("[FINALLY] Transaction Session Ended and Logged.");
        }
    }

    public static void main(String[] args) {
        // Setup a valid card reader file for testing
        String validFile = "card_reader.cfg";
        try (FileWriter fw = new FileWriter(validFile)) {
            fw.write("CARD_READER_ONLINE_OK\n");
        } catch (IOException e) {
            System.err.println("Could not create test card config: " + e.getMessage());
        }

        System.out.println("=========================================================");
        System.out.println("   QUESTION 1: BANK ATM SIMULATION - EXCEPTION HANDLING  ");
        System.out.println("=========================================================");

        // Test Scenario 1: Normal Valid Withdrawal
        System.out.println("\n[Test Case 1: Valid Withdrawal Request]");
        withdraw(validFile, "9876543210", "2500", 10000.0);

        // Test Scenario 2: User types letters instead of numbers
        System.out.println("\n[Test Case 2: User Enters Letters Instead of Numbers]");
        withdraw(validFile, "9876543210", "TWO_THOUSAND", 10000.0);

        // Test Scenario 3: User enters a negative withdrawal amount
        System.out.println("\n[Test Case 3: User Enters Negative Amount]");
        withdraw(validFile, "9876543210", "-500", 10000.0);

        // Test Scenario 4: User requests more than the available balance
        System.out.println("\n[Test Case 4: Withdrawal Amount Exceeds Account Balance]");
        withdraw(validFile, "9876543210", "25000", 10000.0);

        // Test Scenario 5: Card-reader configuration file is missing
        System.out.println("\n[Test Case 5: Missing Card-Reader Configuration File]");
        withdraw("missing_reader.cfg", "9876543210", "1000", 10000.0);
    }
}

