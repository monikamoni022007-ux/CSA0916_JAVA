import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.IntConsumer;

public class PlaylistBenchmark {
    private static final int WARMUP_RUNS = 2;
    private static final int MEASURED_RUNS = 5;
    private static final int END_OPERATIONS = 100;
    private static final int MIDDLE_OPERATIONS = 50;
    private static final int ACCESS_OPERATIONS = 50;
    private static final int ITERATION_PASSES = 5;
    private static volatile long resultSink;

    private enum Structure {
        ARRAY_LIST("ArrayList"),
        LINKED_LIST("LinkedList"),
        ARRAY_DEQUE("ArrayDeque");

        private final String label;

        Structure(String label) {
            this.label = label;
        }
    }

    private enum Operation {
        ADD_END("add_end"),
        INSERT_FRONT("insert_front"),
        REMOVE_FRONT("remove_front"),
        INSERT_MIDDLE("insert_middle"),
        REMOVE_MIDDLE("remove_middle"),
        ACCESS_KTH("access_kth"),
        ITERATE("iterate");

        private final String label;

        Operation(String label) {
            this.label = label;
        }
    }

    private interface Sequence {
        void addLast(int value);

        void addFirst(int value);

        int removeFirst();

        void insertAt(int index, int value);

        int removeAt(int index);

        int get(int index);

        int size();

        void forEachValue(IntConsumer consumer);
    }

    private static final class ArrayListSequence implements Sequence {
        private final List<Integer> values;

        private ArrayListSequence(int initialSize) {
            values = new ArrayList<>(initialSize);
            fill(values, initialSize);
        }

        @Override
        public void addLast(int value) {
            values.add(value);
        }

        @Override
        public void addFirst(int value) {
            values.add(0, value);
        }

        @Override
        public int removeFirst() {
            return values.remove(0);
        }

        @Override
        public void insertAt(int index, int value) {
            values.add(index, value);
        }

        @Override
        public int removeAt(int index) {
            return values.remove(index);
        }

        @Override
        public int get(int index) {
            return values.get(index);
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        public void forEachValue(IntConsumer consumer) {
            for (int value : values) {
                consumer.accept(value);
            }
        }
    }

    private static final class LinkedListSequence implements Sequence {
        private final LinkedList<Integer> values = new LinkedList<>();

        private LinkedListSequence(int initialSize) {
            fill(values, initialSize);
        }

        @Override
        public void addLast(int value) {
            values.addLast(value);
        }

        @Override
        public void addFirst(int value) {
            values.addFirst(value);
        }

        @Override
        public int removeFirst() {
            return values.removeFirst();
        }

        @Override
        public void insertAt(int index, int value) {
            values.add(index, value);
        }

        @Override
        public int removeAt(int index) {
            return values.remove(index);
        }

        @Override
        public int get(int index) {
            return values.get(index);
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        public void forEachValue(IntConsumer consumer) {
            for (int value : values) {
                consumer.accept(value);
            }
        }
    }

    private static final class ArrayDequeSequence implements Sequence {
        private final ArrayDeque<Integer> values;

        private ArrayDequeSequence(int initialSize) {
            values = new ArrayDeque<>(Math.max(1, initialSize));
            fill(values, initialSize);
        }

        @Override
        public void addLast(int value) {
            values.addLast(value);
        }

        @Override
        public void addFirst(int value) {
            values.addFirst(value);
        }

        @Override
        public int removeFirst() {
            return values.removeFirst();
        }

        @Override
        public void insertAt(int index, int value) {
            checkInsertIndex(index, values.size());
            // ArrayDeque has no indexed insertion, so move the suffix temporarily.
            Deque<Integer> suffix = new ArrayDeque<>();
            int suffixSize = values.size() - index;
            for (int moved = 0; moved < suffixSize; moved++) {
                suffix.addFirst(values.removeLast());
            }
            values.addLast(value);
            while (!suffix.isEmpty()) {
                values.addLast(suffix.removeFirst());
            }
        }

        @Override
        public int removeAt(int index) {
            checkElementIndex(index, values.size());
            Deque<Integer> suffix = new ArrayDeque<>();
            int suffixSize = values.size() - index - 1;
            for (int moved = 0; moved < suffixSize; moved++) {
                suffix.addFirst(values.removeLast());
            }
            int removed = values.removeLast();
            while (!suffix.isEmpty()) {
                values.addLast(suffix.removeFirst());
            }
            return removed;
        }

        @Override
        public int get(int index) {
            checkElementIndex(index, values.size());
            int distanceFromStart = index;
            int distanceFromEnd = values.size() - 1 - index;
            if (distanceFromStart <= distanceFromEnd) {
                Iterator<Integer> iterator = values.iterator();
                for (int step = 0; step < distanceFromStart; step++) {
                    iterator.next();
                }
                return iterator.next();
            }
            Iterator<Integer> iterator = values.descendingIterator();
            for (int step = 0; step < distanceFromEnd; step++) {
                iterator.next();
            }
            return iterator.next();
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        public void forEachValue(IntConsumer consumer) {
            for (int value : values) {
                consumer.accept(value);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Path timingCsv = Path.of("experimental_data", "q2_list_benchmark.csv");
        int[] sizes = {1_000, 10_000, 50_000};

        verifyOperations();
        showUpNextExample();
        writeMeasurements(sizes, timingCsv);
        System.out.println("Warm-up runs: " + WARMUP_RUNS);
        System.out.println("Measured runs per operation, size, and structure: " + MEASURED_RUNS);
        System.out.println("Timing data: " + timingCsv);
    }

    private static void writeMeasurements(int[] sizes, Path timingCsv) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(timingCsv, StandardCharsets.UTF_8)) {
            writer.write("size,structure,operation,operations_per_run,run,elapsed_ns");
            writer.newLine();

            System.out.printf("%-8s %-16s %-16s %12s%n",
                    "Size", "Structure", "Operation", "Average ms");
            for (int size : sizes) {
                for (Operation operation : Operation.values()) {
                    int operationCount = operationCount(operation);
                    for (Structure structure : Structure.values()) {
                        long totalNanos = 0;
                        for (int warmup = 0; warmup < WARMUP_RUNS; warmup++) {
                            runOperation(structure, operation, size, operationCount);
                        }
                        for (int run = 1; run <= MEASURED_RUNS; run++) {
                            long elapsedNanos = runOperation(
                                    structure, operation, size, operationCount);
                            totalNanos += elapsedNanos;
                            writer.write(String.join(",",
                                    Integer.toString(size),
                                    structure.label,
                                    operation.label,
                                    Integer.toString(operationCount),
                                    Integer.toString(run),
                                    Long.toString(elapsedNanos)));
                            writer.newLine();
                        }
                        double averageMillis =
                                totalNanos / (double) MEASURED_RUNS / 1_000_000.0;
                        System.out.printf("%-8d %-16s %-16s %12.3f%n",
                                size, structure.label, operation.label, averageMillis);
                    }
                }
            }
        }
    }

    private static long runOperation(
            Structure structure, Operation operation, int size, int operationCount) {
        Sequence sequence = createSequence(structure, size);
        long checksum = 0;
        long start = System.nanoTime();
        switch (operation) {
            case ADD_END -> {
                for (int i = 0; i < operationCount; i++) {
                    sequence.addLast(size + i);
                }
            }
            case INSERT_FRONT -> {
                for (int i = 0; i < operationCount; i++) {
                    sequence.addFirst(size + i);
                }
            }
            case REMOVE_FRONT -> {
                for (int i = 0; i < operationCount; i++) {
                    checksum += sequence.removeFirst();
                }
            }
            case INSERT_MIDDLE -> {
                for (int i = 0; i < operationCount; i++) {
                    sequence.insertAt(sequence.size() / 2, size + i);
                }
            }
            case REMOVE_MIDDLE -> {
                for (int i = 0; i < operationCount; i++) {
                    checksum += sequence.removeAt(sequence.size() / 2);
                }
            }
            case ACCESS_KTH -> {
                for (int i = 0; i < operationCount; i++) {
                    int index = (i * 7_919) % size;
                    checksum += sequence.get(index);
                }
            }
            case ITERATE -> {
                long[] passChecksum = {0};
                for (int pass = 0; pass < ITERATION_PASSES; pass++) {
                    sequence.forEachValue(value -> passChecksum[0] += value);
                }
                checksum = passChecksum[0];
            }
        }
        long elapsedNanos = System.nanoTime() - start;
        resultSink = checksum + sequence.size() + fingerprint(sequence);
        return elapsedNanos;
    }

    private static long fingerprint(Sequence sequence) {
        // Consume the final contents outside the timed section so mutations stay observable.
        long[] sum = {0};
        sequence.forEachValue(value -> sum[0] += value);
        return sum[0];
    }

    private static int operationCount(Operation operation) {
        return switch (operation) {
            case ADD_END, INSERT_FRONT, REMOVE_FRONT -> END_OPERATIONS;
            case INSERT_MIDDLE, REMOVE_MIDDLE -> MIDDLE_OPERATIONS;
            case ACCESS_KTH -> ACCESS_OPERATIONS;
            case ITERATE -> ITERATION_PASSES;
        };
    }

    private static Sequence createSequence(Structure structure, int size) {
        return switch (structure) {
            case ARRAY_LIST -> new ArrayListSequence(size);
            case LINKED_LIST -> new LinkedListSequence(size);
            case ARRAY_DEQUE -> new ArrayDequeSequence(size);
        };
    }

    private static void verifyOperations() {
        List<Integer> expected = List.of(0, 1, 2, 3, 4);
        for (Structure structure : Structure.values()) {
            Sequence sequence = createSequence(structure, 4);
            sequence.addLast(4);
            sequence.addFirst(-1);
            sequence.insertAt(3, 99);
            if (sequence.removeFirst() != -1 || sequence.removeAt(2) != 99) {
                throw new IllegalStateException(structure.label + " edit result differs.");
            }
            List<Integer> actual = new ArrayList<>();
            sequence.forEachValue(actual::add);
            if (!expected.equals(actual)) {
                throw new IllegalStateException(
                        structure.label + " produced " + actual + " instead of " + expected);
            }
        }
    }

    private static void showUpNextExample() {
        Deque<String> upNext = new ArrayDeque<>();
        upNext.addLast("Track A");
        upNext.addLast("Track B");
        upNext.addFirst("Play Next");
        String firstToPlay = upNext.removeFirst();
        String remainingQueue = String.join(" -> ", upNext);
        if (!"Play Next".equals(firstToPlay)
                || !"Track A -> Track B".equals(remainingQueue)) {
            throw new IllegalStateException("Up Next queue order is incorrect.");
        }
        System.out.println("Up Next first: " + firstToPlay);
        System.out.println("Up Next remaining: " + remainingQueue);
        System.out.println("ArrayList, LinkedList, and ArrayDeque operation checks passed.");
    }

    private static void fill(List<Integer> values, int initialSize) {
        for (int i = 0; i < initialSize; i++) {
            values.add(i);
        }
    }

    private static void fill(LinkedList<Integer> values, int initialSize) {
        for (int i = 0; i < initialSize; i++) {
            values.addLast(i);
        }
    }

    private static void fill(Deque<Integer> values, int initialSize) {
        for (int i = 0; i < initialSize; i++) {
            values.addLast(i);
        }
    }

    private static void checkInsertIndex(int index, int size) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }

    private static void checkElementIndex(int index, int size) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }
}
