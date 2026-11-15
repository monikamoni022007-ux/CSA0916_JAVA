package campusparking.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LookupBenchmark {
    private LookupBenchmark() {}

    public static void main(String[] args) {
        int size = args.length > 0 ? Integer.parseInt(args[0]) : 10_000;
        int repetitions = args.length > 1 ? Integer.parseInt(args[1]) : 10_000;
        if (size <= 0 || repetitions <= 0) {
            throw new IllegalArgumentException("Size and repetitions must be positive");
        }

        String[] array = new String[size];
        List<String> list = new ArrayList<>(size);
        Map<String, Integer> map = new HashMap<>(size * 2);
        for (int i = 0; i < size; i++) {
            String registration = "VEHICLE-" + i;
            array[i] = registration;
            list.add(registration);
            map.put(registration, i);
        }
        String target = array[size - 1];

        for (int i = 0; i < 100; i++) {
            findInArray(array, target);
            list.indexOf(target);
            map.get(target);
        }

        long start = System.nanoTime();
        int arrayChecksum = 0;
        for (int i = 0; i < repetitions; i++) {
            arrayChecksum += findInArray(array, target);
        }
        long arrayNanos = System.nanoTime() - start;

        start = System.nanoTime();
        int listChecksum = 0;
        for (int i = 0; i < repetitions; i++) {
            listChecksum += list.indexOf(target);
        }
        long listNanos = System.nanoTime() - start;

        start = System.nanoTime();
        long mapChecksum = 0;
        for (int i = 0; i < repetitions; i++) {
            mapChecksum += map.get(target);
        }
        long mapNanos = System.nanoTime() - start;

        if (arrayChecksum != listChecksum || arrayChecksum != mapChecksum) {
            throw new IllegalStateException("Lookups returned inconsistent results");
        }
        System.out.println("Entries: " + size + "; lookups: " + repetitions + "; target: last entry");
        System.out.println("Array linear scan: " + arrayNanos / 1_000_000.0 + " ms");
        System.out.println("ArrayList.indexOf: " + listNanos / 1_000_000.0 + " ms");
        System.out.println("HashMap.get: " + mapNanos / 1_000_000.0 + " ms");
        System.out.println("Checksums equal: true");
    }

    private static int findInArray(String[] values, String target) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(target)) {
                return i;
            }
        }
        return -1;
    }
}
