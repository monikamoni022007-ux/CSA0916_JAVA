import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PharmacyInventoryBenchmark {
    private static final int LOOKUP_COUNT = 100_000;
    private static final int WARMUP_RUNS = 2;
    private static final int MEASURED_RUNS = 5;
    private static volatile long resultSink;

    private enum Approach {
        HASH_MAP("HashMap"),
        TREE_MAP("TreeMap"),
        LINKED_HASH_MAP("LinkedHashMap"),
        SORTED_ARRAY("SortedArrayBinarySearch");

        private final String label;

        Approach(String label) {
            this.label = label;
        }
    }

    private enum Operation {
        BUILD("build"),
        LOOKUP("lookup_100000"),
        CODE_REPORT("code_order_report"),
        INSERTION_REPORT("insertion_order_report");

        private final String label;

        Operation(String label) {
            this.label = label;
        }
    }

    private static final class Medicine {
        private final String code;
        private final String name;
        private final int stock;
        private final double unitPrice;

        private Medicine(String code, String name, int stock, double unitPrice) {
            this.code = code;
            this.name = name;
            this.stock = stock;
            this.unitPrice = unitPrice;
        }

        private String reportLine() {
            return code + "|" + name + "|" + stock + "|" + unitPrice;
        }
    }

    private static final class Inventory {
        private final Approach approach;
        private final Map<String, Medicine> map;
        private final Medicine[] sortedByCode;
        private final String[] sortedCodes;
        private final List<String> insertionOrder;

        private Inventory(
                Approach approach,
                Map<String, Medicine> map,
                Medicine[] sortedByCode,
                String[] sortedCodes,
                List<String> insertionOrder) {
            this.approach = approach;
            this.map = map;
            this.sortedByCode = sortedByCode;
            this.sortedCodes = sortedCodes;
            this.insertionOrder = insertionOrder;
        }

        private Medicine get(String code) {
            if (approach == Approach.SORTED_ARRAY) {
                int index = Arrays.binarySearch(sortedCodes, code);
                return index < 0 ? null : sortedByCode[index];
            }
            return map.get(code);
        }

        private List<Medicine> codeOrder() {
            if (approach == Approach.TREE_MAP) {
                return new ArrayList<>(map.values());
            }
            if (approach == Approach.SORTED_ARRAY) {
                return Arrays.asList(sortedByCode);
            }
            List<Map.Entry<String, Medicine>> entries = new ArrayList<>(map.entrySet());
            entries.sort(Map.Entry.comparingByKey());
            List<Medicine> ordered = new ArrayList<>(entries.size());
            for (Map.Entry<String, Medicine> entry : entries) {
                ordered.add(entry.getValue());
            }
            return ordered;
        }

        private List<Medicine> insertionOrder() {
            if (approach == Approach.LINKED_HASH_MAP) {
                return new ArrayList<>(map.values());
            }
            List<Medicine> ordered = new ArrayList<>(insertionOrder.size());
            for (String code : insertionOrder) {
                Medicine medicine = get(code);
                if (medicine == null) {
                    throw new IllegalStateException("Inventory is missing " + code);
                }
                ordered.add(medicine);
            }
            return ordered;
        }

        private int size() {
            return approach == Approach.SORTED_ARRAY ? sortedByCode.length : map.size();
        }
    }

    public static void main(String[] args) throws IOException {
        Path timingCsv = Path.of("experimental_data", "q4_map_benchmark.csv");
        int[] sizes = {100, 10_000, 50_000};

        try (BufferedWriter writer =
                     Files.newBufferedWriter(timingCsv, StandardCharsets.UTF_8)) {
            writer.write("inventory_size,approach,operation,operations_per_run,run,elapsed_ns");
            writer.newLine();

            System.out.printf("%-8s %-25s %-24s %12s%n",
                    "Size", "Approach", "Operation", "Average ms");
            for (int size : sizes) {
                List<Medicine> medicines = createMedicinesInReverseCodeOrder(size);
                List<String> lookupCodes = createLookupCodes(size, LOOKUP_COUNT);
                List<String> expectedCodeOrder = sortedCodes(medicines);
                List<String> expectedInsertionOrder = codesInOrder(medicines);

                for (Approach approach : Approach.values()) {
                    Inventory inventory = buildInventory(approach, medicines);
                    verifyOrdering(approach, inventory, expectedCodeOrder,
                            expectedInsertionOrder);
                    verifyReports(inventory, expectedCodeOrder, expectedInsertionOrder);
                    System.out.printf("Verified %s report orders for inventory size %d.%n",
                            approach.label, size);

                    for (Operation operation : Operation.values()) {
                        int operationCount = operation == Operation.LOOKUP
                                ? LOOKUP_COUNT : size;
                        measureAndWrite(writer, size, approach, operation,
                                operationCount, medicines, lookupCodes);
                    }
                }
            }
        }
        System.out.println("Warm-up runs: " + WARMUP_RUNS);
        System.out.println("Measured runs per approach, size, and operation: " + MEASURED_RUNS);
        System.out.println("Timing data: " + timingCsv);
    }

    private static void measureAndWrite(
            BufferedWriter writer,
            int size,
            Approach approach,
            Operation operation,
            int operationCount,
            List<Medicine> medicines,
            List<String> lookupCodes) throws IOException {
        for (int warmup = 0; warmup < WARMUP_RUNS; warmup++) {
            runOperation(approach, operation, medicines, lookupCodes);
        }

        long totalNanos = 0;
        for (int run = 1; run <= MEASURED_RUNS; run++) {
            long elapsedNanos = runOperation(approach, operation, medicines, lookupCodes);
            totalNanos += elapsedNanos;
            writer.write(String.join(",",
                    Integer.toString(size),
                    approach.label,
                    operation.label,
                    Integer.toString(operationCount),
                    Integer.toString(run),
                    Long.toString(elapsedNanos)));
            writer.newLine();
        }
        double averageMillis = totalNanos / (double) MEASURED_RUNS / 1_000_000.0;
        System.out.printf("%-8d %-25s %-24s %12.3f%n",
                size, approach.label, operation.label, averageMillis);
    }

    private static long runOperation(
            Approach approach,
            Operation operation,
            List<Medicine> medicines,
            List<String> lookupCodes) {
        if (operation == Operation.BUILD) {
            long start = System.nanoTime();
            Inventory inventory = buildInventory(approach, medicines);
            long elapsedNanos = System.nanoTime() - start;
            Medicine firstMedicine = inventory.get(lookupCodes.get(0));
            resultSink = inventory.size() + (firstMedicine == null ? 0 : firstMedicine.stock);
            return elapsedNanos;
        }

        Inventory inventory = buildInventory(approach, medicines);
        long checksum = 0;
        long start = System.nanoTime();
        switch (operation) {
            case LOOKUP -> {
                for (String code : lookupCodes) {
                    Medicine medicine = inventory.get(code);
                    if (medicine == null) {
                        throw new IllegalStateException("Lookup missed " + code);
                    }
                    checksum += medicine.stock;
                }
            }
            case CODE_REPORT -> checksum = render(inventory.codeOrder()).hashCode();
            case INSERTION_REPORT -> checksum = render(inventory.insertionOrder()).hashCode();
            case BUILD -> throw new IllegalStateException("Build handled above.");
        }
        long elapsedNanos = System.nanoTime() - start;
        resultSink = checksum;
        return elapsedNanos;
    }

    private static Inventory buildInventory(Approach approach, List<Medicine> medicines) {
        List<String> insertionOrder = new ArrayList<>(medicines.size());
        if (approach == Approach.SORTED_ARRAY) {
            Medicine[] sorted = medicines.toArray(new Medicine[0]);
            Arrays.sort(sorted, Comparator.comparing(medicine -> medicine.code));
            String[] sortedCodes = new String[sorted.length];
            for (int index = 0; index < sorted.length; index++) {
                sortedCodes[index] = sorted[index].code;
            }
            for (Medicine medicine : medicines) {
                insertionOrder.add(medicine.code);
            }
            return new Inventory(approach, null, sorted, sortedCodes, insertionOrder);
        }

        Map<String, Medicine> map = switch (approach) {
            case HASH_MAP -> new HashMap<>();
            case TREE_MAP -> new TreeMap<>();
            case LINKED_HASH_MAP -> new LinkedHashMap<>();
            case SORTED_ARRAY -> throw new IllegalStateException("Handled above.");
        };
        for (Medicine medicine : medicines) {
            map.put(medicine.code, medicine);
            insertionOrder.add(medicine.code);
        }
        return new Inventory(approach, map, null, null, insertionOrder);
    }

    private static List<Medicine> createMedicinesInReverseCodeOrder(int size) {
        List<Medicine> medicines = new ArrayList<>(size);
        for (int id = size; id >= 1; id--) {
            String code = String.format("MED%08d", id);
            medicines.add(new Medicine(code, "Medicine " + id, id % 1_000,
                    (id % 2_500) / 100.0));
        }
        return medicines;
    }

    private static List<String> createLookupCodes(int inventorySize, int lookupCount) {
        List<String> codes = new ArrayList<>(lookupCount);
        for (int lookup = 0; lookup < lookupCount; lookup++) {
            int id = (lookup % inventorySize) + 1;
            codes.add(String.format("MED%08d", id));
        }
        return codes;
    }

    private static List<String> sortedCodes(List<Medicine> medicines) {
        List<String> codes = codesInOrder(medicines);
        codes.sort(String::compareTo);
        return codes;
    }

    private static List<String> codesInOrder(List<Medicine> medicines) {
        List<String> codes = new ArrayList<>(medicines.size());
        for (Medicine medicine : medicines) {
            codes.add(medicine.code);
        }
        return codes;
    }

    private static String render(List<Medicine> medicines) {
        StringBuilder report = new StringBuilder(medicines.size() * 40);
        for (Medicine medicine : medicines) {
            report.append(medicine.reportLine()).append(System.lineSeparator());
        }
        return report.toString();
    }

    private static void verifyOrdering(
            Approach approach,
            Inventory inventory,
            List<String> expectedCodeOrder,
            List<String> expectedInsertionOrder) {
        if (approach == Approach.TREE_MAP) {
            List<String> actual = new ArrayList<>(inventory.map.keySet());
            require(actual.equals(expectedCodeOrder), "TreeMap keys are not in code order.");
        }
        if (approach == Approach.LINKED_HASH_MAP) {
            List<String> actual = new ArrayList<>(inventory.map.keySet());
            require(actual.equals(expectedInsertionOrder),
                    "LinkedHashMap keys are not in insertion order.");
        }
        List<String> codeOrder = new ArrayList<>();
        for (Medicine medicine : inventory.codeOrder()) {
            codeOrder.add(medicine.code);
        }
        require(codeOrder.equals(expectedCodeOrder),
                approach.label + " code report order is incorrect.");
        List<String> insertionOrder = new ArrayList<>();
        for (Medicine medicine : inventory.insertionOrder()) {
            insertionOrder.add(medicine.code);
        }
        require(insertionOrder.equals(expectedInsertionOrder),
                approach.label + " insertion report order is incorrect.");
    }

    private static void verifyReports(
            Inventory inventory,
            List<String> expectedCodeOrder,
            List<String> expectedInsertionOrder) {
        String codeReport = render(inventory.codeOrder());
        String insertionReport = render(inventory.insertionOrder());
        require(reportCodes(codeReport).equals(expectedCodeOrder),
                inventory.approach.label + " code report text is not sorted.");
        require(reportCodes(insertionReport).equals(expectedInsertionOrder),
                inventory.approach.label + " insertion report text is not in insertion order.");
    }

    private static List<String> reportCodes(String report) {
        List<String> codes = new ArrayList<>();
        for (String line : report.split("\\R")) {
            if (!line.isEmpty()) {
                codes.add(line.substring(0, line.indexOf('|')));
            }
        }
        return codes;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
