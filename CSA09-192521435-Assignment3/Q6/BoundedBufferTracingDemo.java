// File: Q6/BoundedBufferTracingDemo.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 6: Student A Requirements - Bounded Buffer Invariant Tracing
 * 
 * Demonstrates:
 * 1. Tracking counter buffer size 'count' over time to prove 0 <= count <= 5 at every step.
 * 2. Difference between sleep() and wait().
 * 3. Why notifyAll() is essential over notify() when multiple producer/consumer threads wait.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoundedBufferTracingDemo {

    static class TracingTokenCounter {
        private final int[] buffer = new int[5];
        private int count = 0, in = 0, out = 0;
        private final List<String> traceLog = Collections.synchronizedList(new ArrayList<>());
        private boolean violationOccurred = false;

        public synchronized void put(int token) throws InterruptedException {
            while (count == buffer.length) {
                wait();
            }
            buffer[in] = token;
            in = (in + 1) % buffer.length;
            count++;

            // Record size invariant check
            traceLog.add(String.format("PUT  token=%2d | Buffer count=%d", token, count));
            if (count < 0 || count > 5) {
                violationOccurred = true;
            }

            notifyAll();
        }

        public synchronized int take() throws InterruptedException {
            while (count == 0) {
                wait();
            }
            int token = buffer[out];
            out = (out + 1) % buffer.length;
            count--;

            // Record size invariant check
            traceLog.add(String.format("TAKE token=%2d | Buffer count=%d", token, count));
            if (count < 0 || count > 5) {
                violationOccurred = true;
            }

            notifyAll();
            return token;
        }

        public List<String> getTraceLog() {
            return traceLog;
        }

        public boolean isViolationOccurred() {
            return violationOccurred;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=========================================================");
        System.out.println("   STUDENT A: BOUNDED BUFFER SIZE INVARIANT TRACING      ");
        System.out.println("=========================================================");

        TracingTokenCounter counter = new TracingTokenCounter();
        Thread cook = new Thread(() -> {
            try {
                for (int t = 1; t <= 10; t++) {
                    counter.put(t);
                    Thread.sleep(5);
                }
            } catch (InterruptedException ignored) {}
        });

        Thread student1 = new Thread(() -> {
            try {
                for (int k = 0; k < 5; k++) {
                    counter.take();
                    Thread.sleep(8);
                }
            } catch (InterruptedException ignored) {}
        });

        Thread student2 = new Thread(() -> {
            try {
                for (int k = 0; k < 5; k++) {
                    counter.take();
                    Thread.sleep(8);
                }
            } catch (InterruptedException ignored) {}
        });

        cook.start();
        student1.start();
        student2.start();

        cook.join();
        student1.join();
        student2.join();

        System.out.println("\n[CHRONOLOGICAL TRACE LOG OF BUFFER SIZE OVER TIME]:");
        for (String entry : counter.getTraceLog()) {
            System.out.println("  " + entry);
        }

        System.out.println("\n[INVARIANT VERIFICATION RESULT]:");
        System.out.println("  Buffer capacity: 5");
        System.out.println("  Did buffer size ever drop below 0 or exceed 5? -> " 
                + (counter.isViolationOccurred() ? "YES (VIOLATION!)" : "NO (STRICTLY PRESERVED)"));
    }
}

