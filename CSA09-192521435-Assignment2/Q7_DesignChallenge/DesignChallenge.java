import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DesignChallenge {
    private static final int TICKET_COUNT = 10_000;
    private static final int FIELDS_PER_TICKET = 50;
    private static final int WARMUP_RUNS = 2;
    private static final int MEASURED_RUNS = 10;
    private static volatile long resultSink;

    private enum Approach {
        STRING_PLUS("String +"),
        PRE_SIZED_STRING_BUILDER("Pre-sized StringBuilder");

        private final String label;

        Approach(String label) {
            this.label = label;
        }
    }

    private static final class Measurement {
        private final long elapsedNanos;
        private final long checksum;

        private Measurement(long elapsedNanos, long checksum) {
            this.elapsedNanos = elapsedNanos;
            this.checksum = checksum;
        }
    }

    private static final class MemorySample {
        private final long usedBefore;
        private final long usedAfter;
        private final int outputCount;
        private final int firstOutputLength;

        private MemorySample(long usedBefore, long usedAfter, List<String> outputs) {
            this.usedBefore = usedBefore;
            this.usedAfter = usedAfter;
            this.outputCount = outputs.size();
            this.firstOutputLength = outputs.isEmpty() ? 0 : outputs.get(0).length();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        List<String> fields = createFields(FIELDS_PER_TICKET);
        int ticketCapacity = totalCharacters(fields);
        long appendCount = (long) TICKET_COUNT * FIELDS_PER_TICKET;
        long plusStringEstimate = appendCount;
        long builderStringEstimate = TICKET_COUNT;
        long plusCharacterCopyEstimate =
                estimateRepeatedConcatenationCopies(fields) * TICKET_COUNT;
        long builderCharacterCopyEstimate = (long) ticketCapacity * 2 * TICKET_COUNT;

        Path timingCsv = Path.of("experimental_data", "q7_design_timing.csv");
        Path memoryCsv = Path.of("experimental_data", "q7_design_memory.csv");
        List<Long> plusSamples = new ArrayList<>(MEASURED_RUNS);
        List<Long> builderSamples = new ArrayList<>(MEASURED_RUNS);

        System.out.println("Workload: " + TICKET_COUNT + " tickets x "
                + FIELDS_PER_TICKET + " fields");
        System.out.println("Warm-up runs per approach: " + WARMUP_RUNS);
        System.out.println("Measured runs per approach: " + MEASURED_RUNS);
        verifySameOutput(fields, ticketCapacity);
        writeTimingMeasurements(timingCsv, fields, ticketCapacity, plusSamples, builderSamples,
                appendCount, plusStringEstimate, builderStringEstimate,
                plusCharacterCopyEstimate, builderCharacterCopyEstimate);
        printTimingSummary(plusSamples, builderSamples);
        writeMemoryMeasurements(memoryCsv, fields, ticketCapacity,
                plusStringEstimate, builderStringEstimate);
        System.out.println("Timing data: " + timingCsv);
        System.out.println("Retained-heap samples: " + memoryCsv);
    }

    private static void writeTimingMeasurements(
            Path timingCsv,
            List<String> fields,
            int ticketCapacity,
            List<Long> plusSamples,
            List<Long> builderSamples,
            long appendCount,
            long plusStringEstimate,
            long builderStringEstimate,
            long plusCharacterCopyEstimate,
            long builderCharacterCopyEstimate) throws IOException {
        try (BufferedWriter writer =
                     Files.newBufferedWriter(timingCsv, StandardCharsets.UTF_8)) {
            writer.write("approach,tickets,fields_per_ticket,total_field_appends,run,"
                    + "elapsed_ns,estimated_string_objects,"
                    + "estimated_characters_copied,output_checksum");
            writer.newLine();

            for (int warmup = 0; warmup < WARMUP_RUNS; warmup++) {
                runBatch(Approach.STRING_PLUS, fields, ticketCapacity);
                runBatch(Approach.PRE_SIZED_STRING_BUILDER, fields, ticketCapacity);
            }

            for (int run = 1; run <= MEASURED_RUNS; run++) {
                Approach first = run % 2 == 1
                        ? Approach.STRING_PLUS : Approach.PRE_SIZED_STRING_BUILDER;
                Approach second = first == Approach.STRING_PLUS
                        ? Approach.PRE_SIZED_STRING_BUILDER : Approach.STRING_PLUS;
                Measurement firstMeasurement = runBatch(first, fields, ticketCapacity);
                Measurement secondMeasurement = runBatch(second, fields, ticketCapacity);
                if (firstMeasurement.checksum != secondMeasurement.checksum) {
                    throw new IllegalStateException(
                            "Original and redesigned outputs differ in run " + run);
                }
                writeTimingRow(writer, first, run, firstMeasurement, appendCount,
                        plusStringEstimate, builderStringEstimate,
                        plusCharacterCopyEstimate, builderCharacterCopyEstimate);
                writeTimingRow(writer, second, run, secondMeasurement, appendCount,
                        plusStringEstimate, builderStringEstimate,
                        plusCharacterCopyEstimate, builderCharacterCopyEstimate);
                addSample(first, firstMeasurement.elapsedNanos, plusSamples, builderSamples);
                addSample(second, secondMeasurement.elapsedNanos, plusSamples, builderSamples);
            }
        }
    }

    private static Measurement runBatch(
            Approach approach, List<String> fields, int ticketCapacity) {
        long checksum = 0;
        long start = System.nanoTime();
        for (int ticket = 0; ticket < TICKET_COUNT; ticket++) {
            String text = build(approach, fields, ticketCapacity);
            checksum = checksum * 31 + text.hashCode();
        }
        long elapsedNanos = System.nanoTime() - start;
        resultSink = checksum;
        return new Measurement(elapsedNanos, checksum);
    }

    private static String build(
            Approach approach, List<String> fields, int ticketCapacity) {
        if (approach == Approach.STRING_PLUS) {
            String ticket = "";
            for (String field : fields) {
                ticket += field;
            }
            return ticket;
        }

        StringBuilder ticket = new StringBuilder(ticketCapacity);
        for (String field : fields) {
            ticket.append(field);
        }
        return ticket.toString();
    }

    private static void verifySameOutput(List<String> fields, int ticketCapacity) {
        String original = build(Approach.STRING_PLUS, fields, ticketCapacity);
        String redesigned = build(Approach.PRE_SIZED_STRING_BUILDER, fields, ticketCapacity);
        if (!original.equals(redesigned)) {
            throw new IllegalStateException("Ticket outputs differ.");
        }
        System.out.println("Output equivalence check passed; characters per ticket: "
                + original.length());
    }

    private static void writeTimingRow(
            BufferedWriter writer,
            Approach approach,
            int run,
            Measurement measurement,
            long appendCount,
            long plusStringEstimate,
            long builderStringEstimate,
            long plusCharacterCopyEstimate,
            long builderCharacterCopyEstimate) throws IOException {
        boolean usesPlus = approach == Approach.STRING_PLUS;
        writer.write(String.join(",",
                approach.label,
                Integer.toString(TICKET_COUNT),
                Integer.toString(FIELDS_PER_TICKET),
                Long.toString(appendCount),
                Integer.toString(run),
                Long.toString(measurement.elapsedNanos),
                Long.toString(usesPlus ? plusStringEstimate : builderStringEstimate),
                Long.toString(usesPlus
                        ? plusCharacterCopyEstimate : builderCharacterCopyEstimate),
                Long.toString(measurement.checksum)));
        writer.newLine();
    }

    private static void addSample(
            Approach approach,
            long elapsedNanos,
            List<Long> plusSamples,
            List<Long> builderSamples) {
        if (approach == Approach.STRING_PLUS) {
            plusSamples.add(elapsedNanos);
        } else {
            builderSamples.add(elapsedNanos);
        }
    }

    private static void printTimingSummary(
            List<Long> plusSamples, List<Long> builderSamples) {
        double plusAverage = averageMillis(plusSamples);
        double builderAverage = averageMillis(builderSamples);
        double improvementPercent = (plusAverage - builderAverage) / plusAverage * 100.0;
        System.out.printf("String + average: %.3f ms%n", plusAverage);
        System.out.printf("Pre-sized StringBuilder average: %.3f ms%n", builderAverage);
        System.out.printf("Elapsed-time reduction: %.1f%%%n", improvementPercent);
        System.out.println("Objective (at least 30% reduction): "
                + (improvementPercent >= 30.0 ? "met" : "not met"));
        System.out.printf("Estimated strings per batch: %,d vs. %,d%n",
                (long) TICKET_COUNT * FIELDS_PER_TICKET, (long) TICKET_COUNT);
        System.out.println("Both approaches produced matching checksums in every run.");
    }

    private static void writeMemoryMeasurements(
            Path memoryCsv,
            List<String> fields,
            int ticketCapacity,
            long plusStringEstimate,
            long builderStringEstimate) throws IOException, InterruptedException {
        try (BufferedWriter writer =
                     Files.newBufferedWriter(memoryCsv, StandardCharsets.UTF_8)) {
            writer.write("approach,tickets,fields_per_ticket,estimated_string_objects,"
                    + "used_before_bytes,used_after_bytes,retained_heap_delta_bytes,"
                    + "outputs_retained,first_output_chars");
            writer.newLine();
            for (Approach approach : Approach.values()) {
                MemorySample sample = measureRetainedHeap(approach, fields, ticketCapacity);
                boolean usesPlus = approach == Approach.STRING_PLUS;
                writer.write(String.join(",",
                        approach.label,
                        Integer.toString(TICKET_COUNT),
                        Integer.toString(FIELDS_PER_TICKET),
                        Long.toString(usesPlus ? plusStringEstimate : builderStringEstimate),
                        Long.toString(sample.usedBefore),
                        Long.toString(sample.usedAfter),
                        Long.toString(sample.usedAfter - sample.usedBefore),
                        Integer.toString(sample.outputCount),
                        Integer.toString(sample.firstOutputLength)));
                writer.newLine();
                System.out.printf("%s approximate retained heap delta: %,d bytes%n",
                        approach.label, sample.usedAfter - sample.usedBefore);
            }
            System.out.println("Heap values are single post-GC samples; interpret cautiously.");
        }
    }

    private static MemorySample measureRetainedHeap(
            Approach approach, List<String> fields, int ticketCapacity)
            throws InterruptedException {
        collectGarbage();
        long usedBefore = usedHeapBytes();
        List<String> outputs = new ArrayList<>(TICKET_COUNT);
        for (int ticket = 0; ticket < TICKET_COUNT; ticket++) {
            outputs.add(build(approach, fields, ticketCapacity));
        }
        collectGarbage();
        long usedAfter = usedHeapBytes();
        return new MemorySample(usedBefore, usedAfter, outputs);
    }

    private static void collectGarbage() throws InterruptedException {
        System.gc();
        Thread.sleep(100);
    }

    private static long usedHeapBytes() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static List<String> createFields(int fieldCount) {
        List<String> fields = new ArrayList<>(fieldCount);
        for (int index = 0; index < fieldCount; index++) {
            fields.add("field-" + index + "=value-" + index + ";");
        }
        return fields;
    }

    private static int totalCharacters(List<String> fields) {
        int total = 0;
        for (String field : fields) {
            total += field.length();
        }
        return total;
    }

    private static long estimateRepeatedConcatenationCopies(List<String> fields) {
        long cumulativeCharacters = 0;
        long copiedCharacters = 0;
        for (String field : fields) {
            cumulativeCharacters += field.length();
            copiedCharacters += cumulativeCharacters;
        }
        return copiedCharacters;
    }

    private static double averageMillis(List<Long> samples) {
        long totalNanos = 0;
        for (long sample : samples) {
            totalNanos += sample;
        }
        return totalNanos / (double) samples.size() / 1_000_000.0;
    }
}
