# Question 1: Handling Failure in a Bank ATM Simulation

**Subject:** Programming in Java (CSA09)  
**Course Outcome:** CO3 (Exception Handling)  
**Bloom's Taxonomy Level:** K4 – Analyse  
**Marks:** 6 Marks  

---

## 1. Objective and Problem Context
An Automated Teller Machine (ATM) simulation reads an account number and a withdrawal amount from the user, verifies the hardware card-reader status, checks the account balance, and dispenses cash.

In real-world operations, transactions can fail at various points:
- The user may type alphabetical characters instead of numbers.
- The user may input a negative or zero withdrawal amount.
- The user may request an amount exceeding their available account balance.
- The hardware card-reader configuration file may be missing from the disk.

The objective is to implement a robust, clean, beginner-friendly Java application that catches and handles all failures gracefully using `try`, specific `catch`, multi-catch (`|`), and `finally` blocks, ensuring that resources are closed and the ATM card is safely ejected in all scenarios.

---

## 2. Shared Group Work

### 2.1 Failure Point Identification & Classification
| Failure Point in Workflow | Exception / Error Type | Classification | Where Caught / Handled |
| :--- | :--- | :--- | :--- |
| Card-reader configuration file missing on disk | `FileNotFoundException` | **Checked Exception** | Specific `catch (FileNotFoundException e)` block |
| Non-numeric input for account number or amount | `NumberFormatException` | **Unchecked Exception** (`RuntimeException`) | Dedicated `catch (NumberFormatException e)` block |
| Negative or zero withdrawal amount entered | `IllegalArgumentException` | **Unchecked Exception** (`RuntimeException`) | Java 7+ multi-catch `(IllegalArgumentException \| ArithmeticException e)` |
| Withdrawal amount exceeds current account balance | `ArithmeticException` | **Unchecked Exception** (`RuntimeException`) | Java 7+ multi-catch `(IllegalArgumentException \| ArithmeticException e)` |
| Card-reader stream I/O failure during read | `IOException` | **Checked Exception** | Fallback `catch (IOException e)` block |
| Unexpected system anomaly | `Exception` | Checked / General | Top-level fallback `catch (Exception e)` block |
| JVM Heap exhaustion during transaction logging | `OutOfMemoryError` | **Error (Unchecked)** | Not caught in business logic; handled by JVM supervisor |

---

## 3. Student A – Exception Design

### 3.1 Relevant Portion of the `java.lang.Throwable` Hierarchy
```
                        java.lang.Object
                               |
                      java.lang.Throwable
              +----------------+----------------+
              |                                 |
       java.lang.Error                 java.lang.Exception
   (Unchecked JVM Failures)                     |
   +- VirtualMachineError         +-------------+-------------+
      +- OutOfMemoryError         |                           |
      +- StackOverflowError  IOException (Checked)    RuntimeException (Unchecked)
                             +- FileNotFoundException +- IllegalArgumentException
                                (Missing Card Reader) |  +- NumberFormatException
                                                      +- ArithmeticException
                                                      +- NullPointerException
```

### 3.2 Catch Block Ordering Justification
In Java, `catch` blocks are evaluated sequentially from top to bottom. The runtime chooses the first catch block whose parameter type matches or is a superclass of the thrown exception object.
- **Narrower subclasses must always appear before broader superclasses:**
  - `FileNotFoundException` is a subclass of `IOException`. Therefore, `catch (FileNotFoundException e)` must be placed **before** `catch (IOException e)`. If placed in reverse, the compiler rejects the program with an `unreachable code` error (JLS §14.21).
  - Similarly, specific exceptions (`NumberFormatException`, `IllegalArgumentException`) must appear before general `catch (Exception e)`.
- **Multi-catch block requirements:**
  - Inside a multi-catch clause `catch (IllegalArgumentException | ArithmeticException e)`, the alternatives must be completely disjoint (neither can be a subclass of the other). Both directly extend `RuntimeException` and are disjoint peer classes.

### 3.3 Guarantees Provided by the `finally` Block
The `finally` block executes unconditionally whether the `try` block completes normally, throws a caught exception, or throws an uncaught exception (unless `System.exit()` is invoked).
In our ATM simulation, the `finally` block guarantees two critical invariants:
1. **Physical / Logical Security:** Safe ejection of the customer's ATM debit card (`ATM Card Ejected Safely`) so it is never trapped inside the machine after an aborted withdrawal.
2. **Resource Safety:** Deterministic closure of the `BufferedReader` card-reader stream to avoid operating system file-descriptor leaks.
3. **Audit Trail:** Writing a transaction completion audit log entry.

---

## 4. Student B – Testing, Tracing & Catch Order Experiment

### 4.1 Five Test Scenarios Matrix
| Test ID | Input Parameters | Catch / Finally Path | Expected Result | Actual Result |
| :--- | :--- | :--- | :--- | :--- |
| **TC-01** | File: `card_reader.cfg`<br>Acc: `9876543210`<br>Amt: `2500`<br>Bal: `10000.0` | Normal `try` completion $\rightarrow$ `finally` | Cash Dispensed: Rs. 2500<br>Remaining Balance: Rs. 7500.0<br>Card Ejected | **PASS** (Cash dispensed, balance updated, card ejected) |
| **TC-02** | File: `card_reader.cfg`<br>Acc: `9876543210`<br>Amt: `TWO_THOUSAND`<br>Bal: `10000.0` | `catch (NumberFormatException)` $\rightarrow$ `finally` | Error message: Invalid input!<br>Card Ejected | **PASS** (`NumberFormatException` caught, card ejected) |
| **TC-03** | File: `card_reader.cfg`<br>Acc: `9876543210`<br>Amt: `-500`<br>Bal: `10000.0` | Multi-catch `(IllegalArgumentException)` $\rightarrow$ `finally` | Error: Amount must be > 0<br>Card Ejected | **PASS** (Multi-catch handled, card ejected) |
| **TC-04** | File: `card_reader.cfg`<br>Acc: `9876543210`<br>Amt: `25000`<br>Bal: `10000.0` | Multi-catch `(ArithmeticException)` $\rightarrow$ `finally` | Error: Insufficient balance<br>Card Ejected | **PASS** (Multi-catch handled, card ejected) |
| **TC-05** | File: `missing_reader.cfg`<br>Acc: `9876543210`<br>Amt: `1000`<br>Bal: `10000.0` | `catch (FileNotFoundException)` $\rightarrow$ `finally` | Error: Card-reader file missing<br>Card Ejected | **PASS** (`FileNotFoundException` caught, card ejected) |

### 4.2 Uncaught Exception Stack Trace Analysis
Generated by executing [`UncaughtExceptionDemo.java`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q1/UncaughtExceptionDemo.java):
```text
Exception in thread "main" java.lang.NumberFormatException: For input string: "TWO_THOUSAND"
	at java.base/java.lang.NumberFormatException.forInputString(NumberFormatException.java:67)
	at java.base/java.lang.Integer.parseInt(Integer.java:662)
	at java.base/java.lang.Integer.parseInt(Integer.java:778)
	at UncaughtExceptionDemo.parseUserAmount(UncaughtExceptionDemo.java:16)
	at UncaughtExceptionDemo.main(UncaughtExceptionDemo.java:24)
```

**Line-by-Line Anatomy:**
- **Line 1 (Header):** `Exception in thread "main" java.lang.NumberFormatException: For input string: "TWO_THOUSAND"`  
  Specifies the executing thread (`main`), the exception class (`java.lang.NumberFormatException`), and the error message containing the illegal token.
- **Line 2 (Origin):** `at java.base/java.lang.NumberFormatException.forInputString(NumberFormatException.java:67)`  
  The exact internal JDK factory method that instantiated and threw the exception.
- **Lines 3–4 (JDK Internal Calls):** `at java.base/java.lang.Integer.parseInt(Integer.java:662)`  
  The standard library parser frames walking down the call stack.
- **Line 5 (Application Method):** `at UncaughtExceptionDemo.parseUserAmount(UncaughtExceptionDemo.java:16)`  
  The exact line in our application code where `Integer.parseInt` was invoked without a surrounding `try-catch`.
- **Line 6 (Entrypoint):** `at UncaughtExceptionDemo.main(UncaughtExceptionDemo.java:24)`  
  The entry point of the application where execution started.

### 4.3 Broader Catch Before Narrower Catch (Compiler Error Demonstration)
When `catch (IOException e)` is placed before `catch (FileNotFoundException e)`:
```text
CatchOrderTest.java:8: error: exception FileNotFoundException has already been caught
        } catch (FileNotFoundException e) {
          ^
1 error
```
**Explanation:**  
Because `FileNotFoundException` extends `IOException`, every `FileNotFoundException` would already be matched by the first `catch (IOException e)` block. The second block could never be reached under any circumstances. The Java compiler performs static reachability analysis and rejects this unreachable code at compile-time.

---

## 5. Joint Design Question & Discussion

### Should the ATM program catch every exception with a single `catch (Exception e)`?
**Answer:**  
No, catching every exception with a single `catch (Exception e)` in core transaction logic is poor software engineering practice.

**What is lost by doing so:**
1. **Diagnostic Specificity:** A missing configuration file (`FileNotFoundException`) requires taking the machine out of service and paging a technician, whereas user typographical error (`NumberFormatException`) simply requires prompting the user to re-enter their PIN/amount. A blanket `catch (Exception e)` conflates these completely different failure modes.
2. **Defect Masking:** Programming bugs such as `NullPointerException` or `ArrayIndexOutOfBoundsException` are inadvertently caught and suppressed, making debugging exceedingly difficult.

**Under what conditions is a single catch acceptable?**
A single `catch (Exception e)` or `catch (Throwable t)` is acceptable only at the **outermost boundary** of the application (e.g., top-level thread runner or main event loop) as a final safety net to log fatal crashes, display a friendly "Temporarily Unavailable" message to the user, ensure the debit card is ejected, and restart the service cleanly.

---

## 6. Alternative Concept and Complexity Analysis

### Procedural Pre-Validation & Error Codes (`AlternativeErrorCodeAtm.java`)
Instead of exceptions, the program checks every input in advance:
```java
if (!configFile.exists()) return -1;
if (!accScanner.hasNextLong()) return -2;
if (!amtScanner.hasNextInt()) return -3;
if (amount <= 0) return -4;
if (amount > balance) return -5;
return SUCCESS; // 0
```

### Comparative Analysis
| Evaluation Dimension | Exception Handling (`try-catch-finally`) | Error Codes & Pre-Checks |
| :--- | :--- | :--- |
| **Readability** | **High:** Normal business flow is clear in `try`; error recovery is separated in `catch`. | **Low:** Guard clauses, flag variables, and error returns clutter every line of business logic. |
| **Number of Checks** | Minimal explicit checks; JVM runtime handles parser errors and missing files automatically. | High; caller must test every input token and inspect return status after every call. |
| **Enforcement** | Strong; checked exceptions force callers to handle errors; cannot be ignored. | Weak; callers can easily ignore return codes (e.g. ignoring return value of `-5`). |
| **Cleanup Guarantee** | Guaranteed by `finally` regardless of where the exception occurs. | Fragile; cleanup code must be duplicated before every `return` statement. |

### Asymptotic Time and Space Complexity
- **Normal Path Time Complexity:**
  - *Exceptions:* $O(1)$ — modern HotSpot JVMs use zero-cost exception tables (`Code` attribute exception tables); there is zero execution overhead when no exception is thrown.
  - *Error Codes:* $O(L)$ where $L$ is input token length scanned twice by Scanner lookahead (`hasNextInt()` followed by `nextInt()`).
- **Failure Path Time Complexity:**
  - *Exceptions:* $O(D)$ where $D$ is the call stack depth, because `fillInStackTrace()` must walk all $D$ active stack frames to build the trace.
  - *Error Codes:* $O(1)$ — returning a constant integer takes a single CPU register write.
- **Failure Path Space Complexity:**
  - *Exceptions:* $O(D)$ auxiliary heap memory to instantiate the `Throwable` object and its `StackTraceElement[D]` array.
  - *Error Codes:* $O(1)$ auxiliary memory (zero heap allocation; uses CPU register).

---

## 7. How to Compile and Run
All commands should be executed from the `Q1` folder:

```bash
# 1. Navigate to Q1
cd Q1

# 2. Compile all programs
javac AtmSimulation.java CatchOrderCompilerErrorDemo.java AlternativeErrorCodeAtm.java UncaughtExceptionDemo.java

# 3. Run the main ATM Simulation (all 5 test scenarios)
java AtmSimulation

# 4. Run catch-ordering demonstration
java CatchOrderCompilerErrorDemo

# 5. Run alternative error code concept
java AlternativeErrorCodeAtm

# 6. Run uncaught exception demonstration (exits with stack trace)
java UncaughtExceptionDemo
```

Execution logs are generated locally when the programs run and are intentionally not committed.

