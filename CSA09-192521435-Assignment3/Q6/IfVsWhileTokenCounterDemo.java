// File: Q6/IfVsWhileTokenCounterDemo.java
/*
 * Course: Programming in Java (CSA09) - Assignment 3 (CO3)
 * Question 6: PDF Requirement - Change while to if Experiment
 * 
 * Demonstrates:
 * 1. Why wait() MUST be enclosed in a while loop rather than an if statement.
 * 2. When multiple consumers wake up from notifyAll(), only one can consume the token.
 *    If 'if' is used, the second consumer proceeds without re-checking the condition,
 *    consuming from an empty buffer and corrupting the count/sum!
 */

public class IfVsWhileTokenCounterDemo {

    // Faulty Counter using IF instead of WHILE
    static class BrokenIfCounter {
        private final int[] buffer = new int[5];
        private int count = 0, in = 0, out = 0;

        public synchronized void put(int token) throws InterruptedException {
            // FLAW: Uses 'if' instead of 'while'
            if (count == buffer.length) {
                wait();
            }
            buffer[in] = token;
            in = (in + 1) % buffer.length;
            count++;
            notifyAll();
        }

        public synchronized int take() throws InterruptedException {
            // FLAW: Uses 'if' instead of 'while'
            if (count == 0) {
                wait();
            }
            int token = buffer[out];
            out = (out + 1) % buffer.length;
            count--;
            notifyAll();
            return token;
        }

        public synchronized int getCount() {
            return count;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=========================================================");
        System.out.println("   QUESTION 6: while vs. if EXPERIMENTAL DEMONSTRATION   ");
        System.out.println("=========================================================");

        int iterations = 10;
        int errorCount = 0;

        System.out.println("Running 10 test rounds with 'if' instead of 'while'...");

        for (int round = 1; round <= iterations; round++) {
            BrokenIfCounter counter = new BrokenIfCounter();
            int[] collected = new int[2];

            Thread cook = new Thread(() -> {
                try {
                    for (int t = 1; t <= 20; t++) {
                        counter.put(t);
                        Thread.sleep(1); // slight delay to increase interleaving
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });

            Thread[] students = new Thread[2];
            for (int s = 0; s < 2; s++) {
                final int id = s;
                students[s] = new Thread(() -> {
                    try {
                        for (int k = 0; k < 10; k++) {
                            collected[id] += counter.take();
                            Thread.sleep(1);
                        }
                    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                });
            }

            cook.start();
            for (Thread t : students) t.start();

            cook.join();
            for (Thread t : students) t.join();

            int sum = collected[0] + collected[1];
            boolean hasAnomaly = (sum != 210) || (counter.getCount() != 0);
            if (hasAnomaly) {
                errorCount++;
            }

            System.out.printf("  Round %2d: Sum collected = %3d (Expected: 210) | Final count = %d %s%n",
                    round, sum, counter.getCount(), hasAnomaly ? "<- ANOMALY / CORRUPTION!" : "<- ok");
        }

        System.out.println("\n[OBSERVATION & EXPLANATION]:");
        System.out.println("1. When 'while' is replaced with 'if', waiting threads resume without re-verifying the predicate.");
        System.out.println("2. If multiple consumers are awakened by notifyAll(), one consumes the token and empties the buffer.");
        System.out.println("3. The second consumer resumes immediately without re-checking, causing buffer underflow or reading stale data.");
        System.out.println("4. Anomaly detected in " + errorCount + " out of " + iterations + " test runs.");
    }
}

