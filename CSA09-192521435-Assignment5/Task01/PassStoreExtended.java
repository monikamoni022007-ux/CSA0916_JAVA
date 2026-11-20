import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PassStoreExtended {
    private final Path file;

    public PassStoreExtended(Path file) {
        this.file = file;
    }

    public void add(String passId, String regNo, String name, int route, double amount)
            throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(String.join(",", passId, regNo, name,
                    String.valueOf(route), String.valueOf(amount)));
            w.newLine();
        }
    }

    public String[] find(String passId) throws IOException {
        if (!Files.exists(file)) return null;
        try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] fields = parseAndValidateFiveFields(line);
                if (fields != null && fields[0].equals(passId)) {
                    return fields;
                }
            }
        }
        return null;
    }

    public List<String[]> listByRoute(int targetRoute) throws IOException {
        List<String[]> matches = new ArrayList<>();
        if (!Files.exists(file)) return matches;
        try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] fields = parseAndValidateFiveFields(line);
                if (fields != null && Integer.parseInt(fields[3].trim()) == targetRoute) {
                    matches.add(fields);
                }
            }
        }
        return matches;
    }

    private String[] parseAndValidateFiveFields(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split(",", -1);
        if (parts.length != 5) return null;
        try {
            Integer.parseInt(parts[3].trim());
            Double.parseDouble(parts[4].trim());
            return parts;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        Path f = Files.createTempFile("passes_ext", ".csv");
        PassStoreExtended store = new PassStoreExtended(f);
        store.add("P1001", "192311001", "Arun", 12, 4500.0);
        Files.writeString(f, "P1002,DAMAGED LINE\n", StandardOpenOption.APPEND);
        store.add("P1003", "192311003", "Divya", 7, 3800.0);
        store.add("P1004", "192311004", "Karthik", 12, 4500.0);

        System.out.println("TC-B1-02 Find P1003 (existing): " + Arrays.toString(store.find("P1003")));
        System.out.println("TC-B1-03 Find P9999 (missing) : " + Arrays.toString(store.find("P9999")));
        System.out.println("TC-B1-04 Find P1002 (damaged) : " + Arrays.toString(store.find("P1002")));
        System.out.println("TC-B1-05 List Route 12 count  : " + store.listByRoute(12).size());
        Files.deleteIfExists(f);
    }
}
