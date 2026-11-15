import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TriageExperiments {
    private static volatile long resultSink;
    private static final int WARMUP_RUNS = 2;
    private static final int MEASURED_RUNS = 5;

    private static final class ExpectedPatient {
        private final String name;
        private final int severity;

        private ExpectedPatient(String name, int severity) {
            this.name = name;
            this.severity = severity;
        }
    }

    public static void main(String[] args) throws IOException {
        Path resultsPath = Path.of("experimental_data", "q6_triage_results.csv");
        Path timingPath = Path.of("experimental_data", "q6_triage_benchmark.csv");

        runEightPatientScenario(resultsPath);
        runPerformanceBenchmark(timingPath);
    }

    private static void runEightPatientScenario(Path resultsPath) throws IOException {
        Triage triage = new Triage();
        triage.admit("Ava", 3);
        triage.admit("Ben", 1);
        triage.admit("Cara", 3);
        triage.admit("Dev", 5);
        triage.admit("Eli", 2);
        triage.admit("Fay", 1);
        triage.admit("Gus", 3);
        triage.admit("Hana", 2);

        List<ExpectedPatient> expected = List.of(
                new ExpectedPatient("Ben", 1),
                new ExpectedPatient("Fay", 1),
                new ExpectedPatient("Eli", 2),
                new ExpectedPatient("Hana", 2),
                new ExpectedPatient("Ava", 3),
                new ExpectedPatient("Cara", 3),
                new ExpectedPatient("Gus", 3),
                new ExpectedPatient("Dev", 5));

        List<Patient> treatmentOrder = triage.waitingInTreatmentOrder();
        verifyTreatmentOrder(treatmentOrder, expected);

        try (BufferedWriter writer =
                     Files.newBufferedWriter(resultsPath, StandardCharsets.UTF_8)) {
            writer.write("position,patient,severity,expected_patient,matches_expected");
            writer.newLine();
            for (int index = 0; index < treatmentOrder.size(); index++) {
                Patient patient = treatmentOrder.get(index);
                ExpectedPatient expectedPatient = expected.get(index);
                writer.write(String.join(",",
                        Integer.toString(index + 1),
                        patient.name,
                        Integer.toString(patient.severity),
                        expectedPatient.name,
                        Boolean.toString(patient.name.equals(expectedPatient.name))));
                writer.newLine();
            }
        }

        System.out.println("Eight-patient treatment order (queue remains unchanged):");
        System.out.println(treatmentOrder);
        System.out.println("Treatment-order checks passed for all 8 patients.");
        System.out.println("Eight-patient results: " + resultsPath);
    }

    private static void verifyTreatmentOrder(
            List<Patient> actual, List<ExpectedPatient> expected) {
        if (actual.size() != expected.size()) {
            throw new IllegalStateException("Treatment order has the wrong number of patients.");
        }
        for (int index = 0; index < expected.size(); index++) {
            Patient actualPatient = actual.get(index);
            ExpectedPatient expectedPatient = expected.get(index);
            if (!actualPatient.name.equals(expectedPatient.name)
                    || actualPatient.severity != expectedPatient.severity) {
                throw new IllegalStateException(
                        "Unexpected patient at position " + (index + 1));
            }
        }
    }

    private static void runPerformanceBenchmark(Path timingPath) throws IOException {
        int[] queueSizes = {100, 10_000, 50_000};
        try (BufferedWriter writer =
                     Files.newBufferedWriter(timingPath, StandardCharsets.UTF_8)) {
            writer.write("queue_size,operation,operations_per_run,run,elapsed_ns");
            writer.newLine();
            System.out.printf("%-10s %-16s %14s%n", "Queue size", "Operation", "Average ms");

            for (int queueSize : queueSizes) {
                measureAndWrite(writer, queueSize, "admit_all",
                        () -> timeAdmissions(queueSize));
                measureAndWrite(writer, queueSize, "serve_all",
                        () -> timeServing(queueSize));
            }
        }
        System.out.println("Warm-up runs: " + WARMUP_RUNS);
        System.out.println("Measured runs per queue size and operation: " + MEASURED_RUNS);
        System.out.println("Performance data: " + timingPath);
    }

    private static void measureAndWrite(
            BufferedWriter writer,
            int queueSize,
            String operation,
            TimedOperation timedOperation) throws IOException {
        for (int warmup = 0; warmup < WARMUP_RUNS; warmup++) {
            timedOperation.run();
        }

        long totalNanos = 0;
        for (int run = 1; run <= MEASURED_RUNS; run++) {
            long elapsedNanos = timedOperation.run();
            totalNanos += elapsedNanos;
            writer.write(String.join(",",
                    Integer.toString(queueSize),
                    operation,
                    Integer.toString(queueSize),
                    Integer.toString(run),
                    Long.toString(elapsedNanos)));
            writer.newLine();
        }
        double averageMillis = totalNanos / (double) MEASURED_RUNS / 1_000_000.0;
        System.out.printf("%-10d %-16s %14.3f%n", queueSize, operation, averageMillis);
    }

    private static long timeAdmissions(int queueSize) {
        Triage triage = new Triage();
        long start = System.nanoTime();
        for (int index = 0; index < queueSize; index++) {
            triage.admit("Patient-" + index, (index % 5) + 1);
        }
        long elapsedNanos = System.nanoTime() - start;
        resultSink = triage.size();
        return elapsedNanos;
    }

    private static long timeServing(int queueSize) {
        Triage triage = new Triage();
        for (int index = 0; index < queueSize; index++) {
            triage.admit("Patient-" + index, (index % 5) + 1);
        }

        long checksum = 0;
        int servedCount = 0;
        long start = System.nanoTime();
        Patient patient;
        while ((patient = triage.next()) != null) {
            checksum += patient.name.hashCode() + patient.severity;
            servedCount++;
        }
        long elapsedNanos = System.nanoTime() - start;
        if (servedCount != queueSize) {
            throw new IllegalStateException("Not all admitted patients were served.");
        }
        resultSink = checksum;
        return elapsedNanos;
    }

    @FunctionalInterface
    private interface TimedOperation {
        long run();
    }
}
