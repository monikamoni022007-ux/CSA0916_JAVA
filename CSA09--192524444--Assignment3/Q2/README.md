# Question 2: User-Defined Exceptions and Propagation in an Online Examination Portal

**Subject:** Programming in Java (CSA09)  
**Course Outcome:** CO3 (Exception Handling)  
**Bloom's Taxonomy Level:** K4 – Analyse  
**Marks:** 7 Marks  

---

## 1. Objective and Problem Context
An online examination portal must evaluate and reject invalid submissions across a 3-layer architecture:
1. **Controller Layer:** Rejects submissions if the examination window has closed (`ExamWindowClosedException`).
2. **Service Layer:** Rejects submissions if the student is not registered for the course (`StudentNotRegisteredException`).
3. **Storage Layer:** Rejects submissions if the answer file exceeds the 500 KB storage limit (`IOException` chained inside `SubmissionStorageException`).

The core engineering objectives are:
- Design a cohesive user-defined exception hierarchy rooted in `ExamException`.
- Justify checked vs. unchecked semantics.
- Demonstrate explicit propagation across layers using `throw` and `throws`.
- Preserve diagnostic evidence through **exception chaining** (`getCause()`).
- Complete **TODO 1** in `StorageDemo.java` and verify output against expected output.

---

## 2. Complete the Code Solution (TODO 1: `StorageDemo.java`)

### Source Code Solution
In `Storage.save(String fileName, long sizeKb)`:
```java
class Storage {
    void save(String fileName, long sizeKb) throws SubmissionStorageException {
        try {
            if (sizeKb > 500) throw new IOException("Disk quota exceeded");
            System.out.println("Saved " + fileName);
        } catch (IOException e) {
            // TODO 1: Wrap the IOException in a SubmissionStorageException, keeping the
            // IOException as its cause
            throw new SubmissionStorageException("Could not store " + fileName, e);
        }
    }
}
```

### Verification Against Expected Output
**Expected Output from Assignment PDF:**
```text
Saved answer01.pdf
Error: Could not store answer02.pdf
Cause: Disk quota exceeded
```
**Actual Output from Execution:**
```text
Saved answer01.pdf
Error: Could not store answer02.pdf
Cause: Disk quota exceeded
```
*Exact match confirmed.*

---

## 3. Student A – Exception Hierarchy & Propagation Design

### 3.1 Exception Hierarchy Class Diagram
```
                              java.lang.Throwable
                                       |
                              java.lang.Exception (Checked)
                       +---------------+---------------+
                       |                               |
                 ExamException (Checked)       RuntimeException (Unchecked)
       +---------------+---------------+               |
       |               |               |       InvalidRequestException
ExamWindowClosedEx  StudentNotRegEx  SubmissionStorageEx (Wraps IOException Cause)
(Controller Layer)  (Service Layer)   (Storage Layer)
```

### 3.2 Checked vs. Unchecked Justification
- **Checked Exceptions (`ExamException` and its operational subclasses):**  
  Used when callers can reasonably be expected to recover or display meaningful domain feedback to the user (e.g. prompting the student to compress the PDF or informing them the portal closed). The Java compiler mandates that callers either catch them or specify them in method signatures using `throws`.
- **Unchecked Exceptions (`RuntimeException`):**  
  Used for programming contract violations (e.g. null parameters, negative file sizes). They signify programming bugs that must be resolved in code rather than handled as routine operations.

### 3.3 Propagation Path Across Three Layers
```
[Storage Layer: Storage.save("answer02.pdf", 800)]
  -> Size 800 KB > 500 KB -> throws IOException("Disk quota exceeded")
  -> Catches IOException and throws new SubmissionStorageException("Could not store...", ioEx)
       | (Propagates upward via 'throws SubmissionStorageException')
       v
[Service Layer: ExamService.submitAnswerScript(...)]
  -> Validates student registration
  -> Declares 'throws ExamException' (propagates without swallowing)
       | (Propagates upward to Controller)
       v
[Controller Layer: ExamController.handleSubmission(...)]
  -> Catches ExamException e
  -> Displays student UI alert: e.getMessage()
  -> Inspects and logs root cause: e.getCause().getMessage()
```

---

## 4. Student B – Failure Scenarios & Chaining Evidence

### 4.1 Multi-Layer Scenario Test Table
| Scenario ID | Test Scenario | Exception & Cause | Output / UI Message | Status |
| :--- | :--- | :--- | :--- | :--- |
| **SC-01** | Valid upload (REG2026_01, 120 KB, Window Open) | None (Normal execution) | `Saved answer01.pdf` | **PASS** |
| **SC-02** | File size exceeds 500 KB limit (REG2026_01, 800 KB) | `SubmissionStorageException` caused by `IOException` | `Error: Could not store answer02.pdf`<br>`Cause: Disk quota exceeded` | **PASS** |
| **SC-03** | Unregistered student (UNREGISTERED_999, 250 KB) | `StudentNotRegisteredException` | `Registration Verification Failed: Student ID 'UNREGISTERED_999' is not registered` | **PASS** |
| **SC-04** | Exam window closed (REG2026_01, 150 KB) | `ExamWindowClosedException` | `Submission Window Closed: The examination deadline passed at 17:00 IST` | **PASS** |

### 4.2 Evidence of What Is Lost If Cause Is Omitted (`UnchainedExceptionDemo.java`)
If the developer omits `e` in the constructor (`new SubmissionStorageException("...", null)`):
1. **`getCause()` returns `null`:** Calling `e.getCause().getMessage()` immediately crashes the error handler with a `java.lang.NullPointerException`.
2. **Permanent Diagnostic Loss:** The underlying system message `"Disk quota exceeded"` and the exact line number where the file system failed are completely lost.
3. **Chained Stack Trace comparison:** With chaining, the JVM prints:
   ```text
   SubmissionStorageException: Could not store answer02.pdf
       at Storage.save(StorageDemo.java:24)
       at StorageDemo.main(StorageDemo.java:33)
   Caused by: java.io.IOException: Disk quota exceeded
       at Storage.save(StorageDemo.java:19)
       ... 1 more
   ```

---

## 5. Joint Conclusion & Discussion

### Why wrap `IOException` into `SubmissionStorageException`?
1. **Separation of Concerns & Encapsulation:** Directly exposing `java.io.IOException` to the controller and presentation layer creates a leaky abstraction. If storage changes from local disk to an Amazon S3 bucket (`AmazonS3Exception`) or a database BLOB (`SQLException`), every method signature and catch block in upstream layers would break.
2. **Domain-Specific Meaning:** A student understands `"Could not store answer script answer02.pdf"`, but has no context for low-level OS file descriptor or disk quota exceptions.
3. **Preserved Root Cause:** Chaining guarantees that while the UI receives a high-level domain message, developers still retain full access to low-level diagnostics via `getCause()`.

---

## 6. Alternative Concept & Complexity Analysis

### Unchecked Custom Exceptions (`RuntimeException`)
If `ExamException` extended `RuntimeException`:
- **Method Signatures:** The `throws ExamException` and `throws SubmissionStorageException` clauses would disappear from `save()` and `submitAnswerScript()`.
- **Calling Code:** Callers would not be forced by the compiler to write `try-catch` blocks.
- **Risk:** Developers may inadvertently forget to catch storage failures, leading to unhandled thread termination in production.

### Asymptotic Time and Space Complexity
- **Stack Trace Capture Time Complexity:** $O(D)$ where $D$ is active call-stack depth. The JVM native method `fillInStackTrace()` traverses all $D$ frames. Chaining two exceptions requires capturing two stack traces: $O(2D) = O(D)$.
- **Stack Trace Space Complexity:** $O(D)$ heap memory per exception object to hold the `StackTraceElement[]` array. When chaining is used, both exception objects remain referenced on the heap until the outer exception is garbage collected, holding approximately $2 \times O(D) = O(D)$ heap space.

---

## 7. How to Compile and Run
```bash
cd Q2

# Compile all programs
javac StorageDemo.java OnlineExamPortal.java UnchainedExceptionDemo.java UncheckedAlternativeDemo.java

# Run Complete the Code program (TODO 1)
java StorageDemo

# Run Three-Layer Online Exam Portal
java OnlineExamPortal

# Run unchained exception demonstration
java UnchainedExceptionDemo

# Run unchecked alternative concept demonstration
java UncheckedAlternativeDemo
```
Execution logs are generated locally when the programs run and are intentionally not committed.

