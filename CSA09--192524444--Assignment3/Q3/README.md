# Question 3: Resource Safety, Debugging and Robust Code

**Subject:** Programming in Java (CSA09)  
**Course Outcome:** CO3 (Exception Handling & Resource Management)  
**Bloom's Taxonomy Level:** K4 – Analyse  
**Marks:** 7 Marks  

---

## 1. Objective and Problem Context
A retail chain generates a consolidated daily sales report by reading several regional store files and writing a summary report. In legacy systems, the process occasionally crashed with `java.io.FileNotFoundException: Too many open files` (OS file-descriptor exhaustion), and reports were left incomplete when a single store file contained malformed numeric data.

The objective is to:
- Investigate why legacy manual closing leaks resources during mid-file parsing exceptions.
- Implement and compare the generator using `try-catch-finally` with manual `close()` against Java 7+ `try-with-resources`.
- Demonstrate breakpoint debugging, count open file handles under partial failures, and add audit logging.
- Explain `AutoCloseable` reverse closing order (LIFO) and demonstrate suppressed exceptions via `Throwable.getSuppressed()`.

*(Note: In accordance with the PDF rubric, responsibilities are intentionally reversed for Question 3: Student A performs Debugging and Experimental Analysis, while Student B performs Design and Correctness).*

---

## 2. Resource Management Architecture
```
[Input Store Files: store1..store4] ===> [Inner try-with-resources: BufferedReader]
  - Valid lines parsed and added to total   - Auto-closed BEFORE catch block runs
  - Corrupt lines throw NumberFormatException- Logs faulty file name; attaches close() errors
                                                    |
                                                    v
                                    [Outer try-with-resources: BufferedWriter]
                                      -> Writes daily_summary.txt & auto-flushes/closes
```

---

## 3. Student A – Debugging and Experimental Analysis

### 3.1 Step-Through Debugging Analysis
Using IDE / JDB breakpoints on [`ManualSalesReportGenerator.java`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q3/ManualSalesReportGenerator.java):
1. **Breakpoint 1:** Placed on `Double.parseDouble(line.trim())` inside the inner while loop.
2. **Breakpoint 2:** Placed on `reader.close()` at the end of the `try` block.
3. **Execution with `data/store2_corrupt.txt`:**
   - Line 1 (`1000.00`): Parses successfully into `1000.0`.
   - Line 2 (`CORRUPT_DATA`): `Double.parseDouble` throws `java.lang.NumberFormatException`.
   - **Observation:** The JVM immediately unwinds the call stack, skipping `reader.close()` entirely, and jumps directly to `catch (Exception e)`. 
   - **Result:** The OS file descriptor allocated to `store2_corrupt.txt` remains open in the kernel file-descriptor table until garbage collection occurs.
4. **Try-With-Resources Execution ([`SafeReportGenerator.java`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q3/SafeReportGenerator.java)):**
   - The Java compiler injects synthetic cleanup code ensuring `reader.close()` is invoked **before** control transfers to the `catch` block.

### 3.2 Open File Handle Tracking Table
| Store File Input | Failure Mode Encountered | Open Handles (Naive Manual) | Open Handles (try-with-resources) |
| :--- | :--- | :--- | :--- |
| `data/store1.txt` | Normal execution (`1200.50`, `800.00`, `499.50`) | 0 (Closed normally) | 0 (Auto-closed) |
| `data/store2_corrupt.txt` | Fails on Line 2 (`CORRUPT_DATA` throws `NumberFormatException`) | **1 LEAKED** (`reader.close()` skipped) | **0** (Auto-closed before catch) |
| `data/store3.txt` | Normal execution (`3000.00`, `1500.00`) | 1 cumulative open handle | 0 (Auto-closed) |
| `data/store4_missing.txt` | File does not exist (`FileNotFoundException`) | 1 cumulative open handle | 0 (Never opened) |
| **Batch Total** | **2 Files Failed Part-Way** | **2 LEAKED** (Hits OS `ulimit`) | **0 LEAKED** (Deterministic Cleanup) |

### 3.3 Diagnostic Audit Logging
In [`SafeReportGenerator.java`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q3/SafeReportGenerator.java), every failure is logged via `java.util.logging.Logger`:
```text
WARNING: [TRY-WITH-RESOURCES LOG] Error in file 'data/store2_corrupt.txt': NumberFormatException - For input string: "CORRUPT_DATA" (Stream was safely auto-closed!)
WARNING: [TRY-WITH-RESOURCES LOG] Error in file 'data/store4_missing.txt': NoSuchFileException - data\store4_missing.txt (Stream was safely auto-closed!)
```

---

## 4. Student B – Design and Correctness

### 4.1 Resource Identification & Closing Scope
1. **Summary Writer (`BufferedWriter`):** Outer resource. Must remain open during the entire batch execution and close once all stores are aggregated.
2. **Store Reader (`BufferedReader`):** Inner resource. Must be opened per store file and closed immediately upon completing or failing that specific file.

### 4.2 Why Manual Closing Leaks Resources
In legacy `try-catch-finally` code:
- Developers frequently place `close()` at the end of the `try` block. Any mid-file exception skips `close()`.
- Placing multiple `close()` calls in a single `finally` block (e.g. `reader.close(); writer.close();`) is hazardous: if `reader.close()` throws an `IOException`, `writer.close()` is never executed.
- Nesting `finally` blocks produces cumbersome boilerplate that is easily misconfigured.

### 4.3 Reverse Closing Order (LIFO)
When multiple resources are declared in a single `try(...)` header:
```java
try (ResourceA a = new ResourceA(); ResourceB b = new ResourceB()) { ... }
```
The JVM guarantees they are closed in **reverse order of declaration** (`b.close()` executes first, then `a.close()`). This ensures dependent wrapper streams flush and close before underlying physical channel resources close.

### 4.4 Suppressed Exceptions (`Throwable.getSuppressed()`)
- **Problem in Legacy Code:** If code inside `try` throws an exception (e.g. `IllegalStateException`) AND `close()` in `finally` also throws an exception (e.g. `IOException`), the `finally` exception completely overwrites and erases the primary exception.
- **Solution in Try-With-Resources:** The JVM preserves the primary exception from `try` and attaches the closing exception as a **suppressed exception** via `Throwable.addSuppressed()`.
- Demonstrated in [`SuppressedExceptionDemo.java`](file:///c:/Users/KAVYA%20SREE/JAVA%203/Q3/SuppressedExceptionDemo.java):
  ```text
  [PRIMARY EXCEPTION CAUGHT]: java.lang.IllegalStateException: Primary Exception: Malformed binary payload
  [SUPPRESSED EXCEPTIONS RETRIEVED]: Count = 1
    -> Suppressed[0]: java.io.IOException | Message: Secondary Exception: Network connection reset during close()
  ```

---

## 5. Joint Engineering Decision & Discussion

### Under what conditions is try-with-resources NOT sufficient on its own?
While `try-with-resources` guarantees deterministic closing of file descriptors, it is NOT sufficient by itself to ensure system robustness because:
1. **Batch Continuity:** If exceptions (`NumberFormatException` / `IOException`) are not caught *inside the per-file loop*, a single malformed file will abort the entire application, leaving remaining store files unread and the summary report truncated.
2. **Atomic Disk Updates:** If the JVM crashes mid-write, `summary.txt` is left partially written. Robust production systems must write to a temporary file (`summary.tmp`) and perform an atomic rename (`Files.move(..., StandardCopyOption.ATOMIC_MOVE)`).
3. **Data Integrity:** Semantic errors (e.g. negative sales values or integer overflows) do not trigger I/O exceptions and require explicit business assertion logic.

---

## 6. Alternative Concept and Complexity Analysis

### Time and Space Complexity for $f$ Store Files ($L$ lines per file)
| Implementation Strategy | Time Complexity | Max Concurrently Open File Handles (Space) | Robustness Under Failures |
| :--- | :--- | :--- | :--- |
| **Try-With-Resources** | $O(f \cdot L)$ | **$O(1)$** (Exactly 2 handles: 1 writer + 1 active reader) | High: 0 leaks; handles errors gracefully. |
| **Manual Safe `finally`** | $O(f \cdot L)$ | **$O(1)$** (Requires nested `finally` blocks) | Medium: Verbose boilerplate; error-prone. |
| **Naive Manual `close()`** | $O(f \cdot L)$ | **$O(f)$** (Accumulates 1 leaked descriptor per failure) | Low: Crashes with `Too many open files` when $f > ulimit$. |

---

## 7. How to Compile and Run
All commands should be executed from the `Q3` folder:

```bash
cd Q3

# 1. Compile all programs
javac ManualSalesReportGenerator.java SafeReportGenerator.java SuppressedExceptionDemo.java ReportEquivalenceVerification.java

# 2. Run the main Safe Report Generator (Demonstrates leak tracking & try-with-resources)
java SafeReportGenerator

# 3. Run Suppressed Exception Demonstration
java SuppressedExceptionDemo

# 4. Run Shared Report Equivalence Verification
java ReportEquivalenceVerification
```

Execution logs and generated report summaries are created locally when the programs run and are intentionally not committed.

