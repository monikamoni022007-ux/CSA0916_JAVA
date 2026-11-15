import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class TextBuildingBenchmark {
    private static final int WARMUP_RUNS = 2;
    private static final int MEASURED_RUNS = 5;
    private static volatile long resultSink;

    private enum Approach {
        STRING_PLUS("String +"),
        STRING_BUILDER("StringBuilder"),
        STRING_BUFFER("StringBuffer");

        private final String label;

        Approach(String label) {
            this.label = label;
        }
    }

    private static final class Workload {
        private final String name;
        private final int ticketCount;
        private final int fieldsPerTicket;

        private Workload(String name, int ticketCount, int fieldsPerTicket) {
            this.name = name;
            this.ticketCount = ticketCount;
            this.fieldsPerTicket = fieldsPerTicket;
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
        Path timingCsv = Path.of("experimental_data", "q1_string_timing.csv");
        Path memoryCsv = Path.of("experimental_data", "q1_string_memory.csv");
        List<Workload> workloads = List.of(
                new Workload("many_short_tickets", 20_000, 6),
                new Workload("many_fields_per_ticket", 10_000, 50),
                new Workload("long_ticket", 200, 200));

        verifyApproachesAndAlternatives();
        writeMeasurements(workloads, timingCsv, memoryCsv);
        System.out.println("Warm-up runs: " + WARMUP_RUNS);
        System.out.println("Measured runs per approach and workload: " + MEASURED_RUNS);
        System.out.println("All three measured approaches produced identical text.");
        System.out.println("Alternative StringJoiner and String.format examples ran successfully.");
        System.out.println("Timing data: " + timingCsv);
        System.out.println("Retained-heap samples: " + memoryCsv);
    }

    private static void writeMeasurements(
            List<Workload> workloads, Path timingCsv, Path memoryCsv)
            throws IOException, InterruptedException {
        try (BufferedWriter timingWriter = Files.newBufferedWriter(
                     timingCsv, StandardCharsets.UTF_8);
             BufferedWriter memoryWriter = Files.newBufferedWriter(
                     memoryCsv, StandardCharsets.UTF_8)) {
            timingWriter.write("workload,approach,ticket_count,fields_per_ticket,"
                    + "field_appends,run,elapsed_ns,estimated_string_objects,"
                    + "estimated_characters_copied");
            timingWriter.newLine();
            memoryWriter.write("workload,approach,ticket_count,fields_per_ticket,"
                    + "used_before_bytes,used_after_bytes,retained_heap_delta_bytes,"
                    + "outputs_retained,first_output_chars");
            memoryWriter.newLine();

            for (Workload workload : workloads) {
                List<String> fields = createFields(workload.fieldsPerTicket);
                for (Approach approach : Approach.values()) {
                    for (int warmup = 0; warmup < WARMUP_RUNS; warmup++) {
                        runBatch(approach, fields, workload.ticketCount);
                    }

                    long totalNanos = 0;
                    for (int run = 1; run <= MEASURED_RUNS; run++) {
                        long elapsedNanos = runBatch(approach, fields, workload.ticketCount);
                        totalNanos += elapsedNanos;
                        writeTimingRow(timingWriter, workload, approach, fields, run,
                                elapsedNanos);
                    }

                    double averageMillis = totalNanos / (double) MEASURED_RUNS / 1_000_000.0;
                    System.out.printf("%-24s %-14s average: %10.3f ms%n",
                            workload.name, approach.label, averageMillis);

                    MemorySample sample = measureRetainedHeap(
                            approach, fields, workload.ticketCount);
                    long retainedDelta = sample.usedAfter - sample.usedBefore;
                    memoryWriter.write(String.join(",",
                            workload.name,
                            approach.label,
                            Integer.toString(workload.ticketCount),
                            Integer.toString(workload.fieldsPerTicket),
                            Long.toString(sample.usedBefore),
                            Long.toString(sample.usedAfter),
                            Long.toString(retainedDelta),
                            Integer.toString(sample.outputCount),
                            Integer.toString(sample.firstOutputLength)));
                    memoryWriter.newLine();
                    System.out.printf("  Approx. retained heap delta: %,d bytes "
                                    + "(one post-GC sample; interpret cautiously)%n",
                            retainedDelta);
                }
            }
        }
    }

    private static long runBatch(Approach approach, List<String> fields, int ticketCount) {
        long checksum = 0;
        long start = System.nanoTime();
        for (int ticket = 0; ticket < ticketCount; ticket++) {
            String text = build(approach, fields);
            checksum = checksum * 31 + text.hashCode();
        }
        long elapsedNanos = System.nanoTime() - start;
        resultSink = checksum;
        return elapsedNanos;
    }

    private static MemorySample measureRetainedHeap(
            Approach approach, List<String> fields, int ticketCount)
            throws InterruptedException {
        collectGarbage();
        long usedBefore = usedHeapBytes();

        List<String> outputs = new ArrayList<>(ticketCount);
        for (int ticket = 0; ticket < ticketCount; ticket++) {
            outputs.add(build(approach, fields));
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

    private static String build(Approach approach, List<String> fields) {
        return switch (approach) {
            case STRING_PLUS -> buildWithStringPlus(fields);
            case STRING_BUILDER -> buildWithStringBuilder(fields);
            case STRING_BUFFER -> buildWithStringBuffer(fields);
        };
    }

    private static String buildWithStringPlus(List<String> fields) {
        String ticket = "";
        for (String field : fields) {
            ticket += field;
        }
        return ticket;
    }

    private static String buildWithStringBuilder(List<String> fields) {
        StringBuilder ticket = new StringBuilder();
        for (String field : fields) {
            ticket.append(field);
        }
        return ticket.toString();
    }

    private static String buildWithStringBuffer(List<String> fields) {
        StringBuffer ticket = new StringBuffer();
        for (String field : fields) {
            ticket.append(field);
        }
        return ticket.toString();
    }

    private static String buildWithStringJoiner(List<String> fields) {
        StringJoiner ticket = new StringJoiner("");
        for (String field : fields) {
            ticket.add(field);
        }
        return ticket.toString();
    }

    private static String buildFixedTicketWithFormat() {
        return String.format(
                "PNR: %s%nPassenger: %s%nCoach: %s%nBerth: %s%n",
                "PNR123456", "Asha Kumar", "S2", "36");
    }

    private static void verifyApproachesAndAlternatives() {
        List<String> sampleFields = List.of("PNR=PNR123456;", "Passenger=Asha Kumar;");
        String expected = buildWithStringBuilder(sampleFields);
        for (Approach approach : Approach.values()) {
            if (!expected.equals(build(approach, sampleFields))) {
                throw new IllegalStateException(approach.label + " output differs.");
            }
        }
        String joined = buildWithStringJoiner(sampleFields);
        String formatted = buildFixedTicketWithFormat();
        if (!expected.equals(joined)) {
            throw new IllegalStateException("StringJoiner output differs from StringBuilder.");
        }
        if (!formatted.contains("Passenger: Asha Kumar")) {
            throw new IllegalStateException("String.format ticket example failed.");
        }
        System.out.println("StringJoiner alternative: " + joined);
        System.out.println("String.format fixed-ticket alternative:");
        System.out.print(formatted);
    }

    private static List<String> createFields(int fieldCount) {
        List<String> fields = new ArrayList<>(fieldCount);
        for (int field = 0; field < fieldCount; field++) {
            fields.add("field-" + field + "=value-" + field + ";");
        }
        return fields;
    }

    private static void writeTimingRow(
            BufferedWriter writer,
            Workload workload,
            Approach approach,
            List<String> fields,
            int run,
            long elapsedNanos) throws IOException {
        long appends = (long) workload.ticketCount * workload.fieldsPerTicket;
        long stringObjects = approach == Approach.STRING_PLUS
                ? appends : workload.ticketCount;
        long copiedCharactersPerTicket = estimateCopiedCharacters(approach, fields);
        long copiedCharacters = copiedCharactersPerTicket * workload.ticketCount;
        writer.write(String.join(",",
                workload.name,
                approach.label,
                Integer.toString(workload.ticketCount),
                Integer.toString(workload.fieldsPerTicket),
                Long.toString(appends),
                Integer.toString(run),
                Long.toString(elapsedNanos),
                Long.toString(stringObjects),
                Long.toString(copiedCharacters)));
        writer.newLine();
    }

    private static long estimateCopiedCharacters(Approach approach, List<String> fields) {
        long totalFieldCharacters = 0;
        long cumulativeCharacters = 0;
        long concatenationCopies = 0;
        for (String field : fields) {
            totalFieldCharacters += field.length();
            cumulativeCharacters += field.length();
            concatenationCopies += cumulativeCharacters;
        }
        if (approach == Approach.STRING_PLUS) {
            return concatenationCopies;
        }
        // Appending writes each field once; toString copies the completed text once.
        return totalFieldCharacters * 2;
    }
}
