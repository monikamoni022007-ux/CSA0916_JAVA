import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class HashSetExperiments {
    @SuppressWarnings("overrides")
    private static final class BrokenStudent {
        private final String regNo;
        private final String name;

        private BrokenStudent(String regNo, String name) {
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof BrokenStudent)) {
                return false;
            }
            return regNo.equals(((BrokenStudent) other).regNo);
        }
    }

    private static final class MutableStudent {
        private String regNo;

        private MutableStudent(String regNo) {
            this.regNo = regNo;
        }

        private void setRegNo(String regNo) {
            this.regNo = regNo;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof MutableStudent)) {
                return false;
            }
            return regNo.equals(((MutableStudent) other).regNo);
        }

        @Override
        public int hashCode() {
            return regNo.hashCode();
        }
    }

    public static void main(String[] args) throws IOException {
        Path csvPath = Path.of("experimental_data", "q3_hashset_tests.csv");
        try (BufferedWriter writer =
                     Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8)) {
            writer.write("test_name,implementation,predicted,observed,hash_first,hash_second");
            writer.newLine();

            demonstrateSuppliedProgramBeforeTodo(writer);
            demonstrateSameReference(writer);
            demonstrateDuplicateInstances(writer);
            demonstrateCorrectedStudent(writer);
            demonstrateMutableHashField(writer);
        }
        System.out.println("Experimental results: " + csvPath);
    }

    @SuppressWarnings("auxiliaryclass")
    private static void demonstrateSuppliedProgramBeforeTodo(BufferedWriter writer)
            throws IOException {
        System.out.println("Prediction before supplied program: set size=3");
        Set<StudentBefore> present = new HashSet<>();
        present.add(new StudentBefore("192311001", "Arun"));
        present.add(new StudentBefore("192311002", "Divya"));
        present.add(new StudentBefore("192311001", "Arun"));
        int actualSize = present.size();
        require(actualSize == 3, "The uncorrected experiment should expose the duplicate.");
        System.out.println("Supplied program before TODO: observed=" + actualSize);
        writeRow(writer, "attendance_before_todo", "equals_only", "size=3",
                "size=" + actualSize, "", "");
    }

    private static void demonstrateSameReference(BufferedWriter writer) throws IOException {
        System.out.println("Prediction before test 1: set size=1 (same reference)");
        BrokenStudent student = new BrokenStudent("192311001", "Arun");
        Set<BrokenStudent> scans = new HashSet<>();
        scans.add(student);
        scans.add(student);
        int actualSize = scans.size();
        require(actualSize == 1, "The same reference should be stored only once.");
        System.out.println("Test 1 (same object scanned twice): observed=" + actualSize);
        writeRow(writer, "same_reference", "equals_only", "size=1",
                "size=" + actualSize, Integer.toString(student.hashCode()),
                Integer.toString(student.hashCode()));
    }

    private static void demonstrateDuplicateInstances(BufferedWriter writer)
            throws IOException {
        BrokenStudent first = new BrokenStudent("192311001", "Arun");
        BrokenStudent second = new BrokenStudent("192311001", "Arun");
        while (first.hashCode() == second.hashCode()) {
            second = new BrokenStudent("192311001", "Arun");
        }

        System.out.println("Prediction before test 2: set size=2 (equal IDs, different hashes)");
        Set<BrokenStudent> scans = new HashSet<>();
        scans.add(first);
        scans.add(second);
        int actualSize = scans.size();
        require(actualSize == 2, "Different inherited hash codes should expose the defect.");
        System.out.println("Test 2 (equal IDs, separate objects): observed="
                + actualSize + ", hashes=" + first.hashCode() + "/" + second.hashCode());
        writeRow(writer, "duplicate_instances", "equals_only", "size=2",
                "size=" + actualSize, Integer.toString(first.hashCode()),
                Integer.toString(second.hashCode()));
    }

    @SuppressWarnings("auxiliaryclass")
    private static void demonstrateCorrectedStudent(BufferedWriter writer) throws IOException {
        System.out.println("Prediction before test 3: set size=2 (registration number identity)");
        Set<Student> present = new HashSet<>();
        present.add(new Student("192311001", "Arun"));
        present.add(new Student("192311002", "Divya"));
        present.add(new Student("192311001", "Arun"));
        int actualSize = present.size();
        require(actualSize == 2, "The corrected Student must deduplicate equal registration IDs.");
        System.out.println("Test 3 (corrected Student): observed=" + actualSize);
        writeRow(writer, "corrected_duplicates", "equals_and_hashCode", "size=2",
                "size=" + actualSize, "", "");
    }

    private static void demonstrateMutableHashField(BufferedWriter writer) throws IOException {
        System.out.println("Prediction before test 4: contains after key mutation=false");
        MutableStudent student = new MutableStudent("192311001");
        Set<MutableStudent> present = new HashSet<>();
        present.add(student);
        boolean foundBeforeChange = present.contains(student);
        int oldHash = student.hashCode();
        student.setRegNo("192311099");
        int newHash = student.hashCode();
        boolean foundAfterChange = present.contains(student);
        require(foundBeforeChange, "The inserted student should initially be found.");
        require(oldHash != newHash, "The chosen registration IDs must have different hashes.");
        require(!foundAfterChange, "Changing the hash field should make lookup fail.");
        System.out.println("Test 4 (mutated hash field): contains before=true, after="
                + foundAfterChange + ", hashes=" + oldHash + "->" + newHash);
        writeRow(writer, "mutable_hash_field", "mutable_hashCode", "contains_after=false",
                "contains_after=" + foundAfterChange, Integer.toString(oldHash),
                Integer.toString(newHash));
    }

    private static void writeRow(
            BufferedWriter writer,
            String testName,
            String implementation,
            String predicted,
            String observed,
            String hashFirst,
            String hashSecond) throws IOException {
        writer.write(String.join(",",
                testName,
                implementation,
                predicted,
                observed,
                hashFirst,
                hashSecond));
        writer.newLine();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
