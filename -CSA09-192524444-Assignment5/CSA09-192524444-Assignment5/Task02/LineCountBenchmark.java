import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class LineCountBenchmark {
    public static void generateLogFile(Path file, long targetBytes) throws IOException {
        byte[] sampleLine = "2026-11-12T09:00:00Z,192311001,PRESENT,CAMPUS_MAIN_GATE_01\n"
                .getBytes(StandardCharsets.UTF_8);
        long written = 0;
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file.toFile()), 65536)) {
            while (written < targetBytes) {
                int toWrite = (int) Math.min(sampleLine.length, targetBytes - written);
                bos.write(sampleLine, 0, toWrite);
                written += toWrite;
            }
        }
    }

    public static long countUnbuffered(Path file) throws IOException {
        long lines = 0;
        try (FileInputStream fis = new FileInputStream(file.toFile())) {
            int b;
            while ((b = fis.read()) != -1) {
                if (b == '\n') lines++;
            }
        }
        return lines;
    }

    public static void main(String[] args) throws IOException {
        long[] sizesInMB = {1L, 10L, 50L};
        System.out.printf("%-10s | %-18s | %-18s | %-15s%n",
                "File Size", "Unbuffered (ms)", "Buffered (ms)", "Speedup Ratio");
        System.out.println("-------------------------------------------------------------------------");
        for (long mb : sizesInMB) {
            long bytes = mb * 1024L * 1024L;
            Path tempFile = Files.createTempFile("attendance_" + mb + "MB_", ".log");
            try {
                generateLogFile(tempFile, bytes);

                long bufStart = System.nanoTime();
                long bufLines = BufferedLineCounter.countLinesWithByteArray(tempFile);
                double bufMs = (System.nanoTime() - bufStart) / 1_000_000.0;

                long unbufStart = System.nanoTime();
                long unbufLines = countUnbuffered(tempFile);
                double unbufMs = (System.nanoTime() - unbufStart) / 1_000_000.0;

                if (bufLines != unbufLines) {
                    throw new IllegalStateException("Line count mismatch!");
                }
                System.out.printf("%-10s | %-18.2f | %-18.2f | %-14.1fx%n",
                        mb + " MB", unbufMs, bufMs, unbufMs / Math.max(0.001, bufMs));
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }
    }
}
