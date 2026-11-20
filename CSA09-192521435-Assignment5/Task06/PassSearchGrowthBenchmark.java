import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class PassSearchGrowthBenchmark {
    public static void populateRecords(Path file, int count) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (int i = 1; i <= count; i++) {
                w.write("P" + i + ",1923" + String.format("%05d", i) + ",Student" + i
                        + "," + ((i % 40) + 1) + ",4500.0");
                w.newLine();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int[] recordCounts = {1_000, 10_000, 100_000};
        int trials = 10;
        System.out.printf("%-15s | %-22s | %-22s%n",
                "Record Count", "Find Last Record (ms)", "Find Missing ID (ms)");
        System.out.println("-------------------------------------------------------------------");
        for (int n : recordCounts) {
            Path tempFile = Files.createTempFile("passes_" + n + "_", ".csv");
            try {
                populateRecords(tempFile, n);
                PassStore store = new PassStore(tempFile);
                String lastId = "P" + n;
                store.find(lastId); // Warm-up

                long totalLastNs = 0;
                long totalMissingNs = 0;
                for (int t = 0; t < trials; t++) {
                    long s1 = System.nanoTime();
                    store.find(lastId);
                    totalLastNs += (System.nanoTime() - s1);

                    long s2 = System.nanoTime();
                    store.find("P_NON_EXISTENT");
                    totalMissingNs += (System.nanoTime() - s2);
                }
                System.out.printf("%-15d | %-22.3f | %-22.3f%n",
                        n, (totalLastNs / (double) trials) / 1_000_000.0,
                        (totalMissingNs / (double) trials) / 1_000_000.0);
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }
    }
}
