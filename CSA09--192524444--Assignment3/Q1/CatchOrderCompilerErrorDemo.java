// File: Q1/CatchOrderCompilerErrorDemo.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 1: Student B Requirement - Broader Catch Before Narrower Catch
 * 
 * In Java, catch blocks are evaluated sequentially from top to bottom.
 * If a broader superclass catch block (e.g., IOException) is placed BEFORE
 * a narrower subclass catch block (e.g., FileNotFoundException), the compiler
 * rejects the code with:
 * "error: exception FileNotFoundException has already been caught"
 * 
 * JLS §14.21 mandates that every catch block must be reachable. Since
 * FileNotFoundException is an IOException, the second catch block can NEVER execute.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CatchOrderCompilerErrorDemo {

    public static void demonstrateReachableOrder() {
        System.out.println("1. Correct Order: Subclass (FileNotFoundException) first, then Superclass (IOException):");
        try {
            FileReader fr = new FileReader("card_reader.cfg");
            fr.close();
            System.out.println("   [SUCCESS] Compiled and executed correctly.");
        } catch (FileNotFoundException e) {
            System.out.println("   [Narrow Handler] Handled missing file: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("   [Broad Handler] Handled general I/O error: " + e.getMessage());
        }
    }

    public static void demonstrateCompilerErrorExplanation() {
        System.out.println("\n2. Broken Order Demonstration (What javac does when broad precedes narrow):");
        System.out.println("   Code snippet:");
        System.out.println("   try {");
        System.out.println("       FileReader fr = new FileReader(\"card_reader.cfg\");");
        System.out.println("   } catch (IOException e) {             // Superclass Catch Block");
        System.out.println("       System.out.println(\"I/O error\");");
        System.out.println("   } catch (FileNotFoundException e) {    // Subclass Catch Block (REJECTED!)");
        System.out.println("       System.out.println(\"File not found\");");
        System.out.println("   }");
        System.out.println("\n   Compiler Output from javac:");
        System.out.println("   CatchOrderCompilerErrorDemo.java: Line X: error: exception FileNotFoundException has already been caught");
        System.out.println("   } catch (FileNotFoundException e) {");
        System.out.println("     ^");
        System.out.println("   1 error");
    }

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("   CATCH BLOCK ORDERING DEMONSTRATION & VERIFICATION     ");
        System.out.println("=========================================================");
        demonstrateReachableOrder();
        demonstrateCompilerErrorExplanation();
    }
}

