import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("overrides")
class StudentBefore {
    private final String regNo;
    private final String name;

    StudentBefore(String regNo, String name) {
        this.regNo = regNo;
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof StudentBefore)) {
            return false;
        }
        return regNo.equals(((StudentBefore) other).regNo);
    }

    @Override
    public String toString() {
        return regNo + " " + name;
    }
}

public class AttendanceBefore {
    public static void main(String[] args) {
        Set<StudentBefore> present = new HashSet<>();
        present.add(new StudentBefore("192311001", "Arun"));
        present.add(new StudentBefore("192311002", "Divya"));
        present.add(new StudentBefore("192311001", "Arun"));
        System.out.println("Unique students: " + present.size());
    }
}
