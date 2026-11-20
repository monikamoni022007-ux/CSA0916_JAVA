public class Student {
    private final String regNo;
    private final String name;
    private final String department;
    private final int semester;
    private final double cgpa;

    public Student(String regNo, String name, String department, int semester, double cgpa) {
        this.regNo = regNo;
        this.name = name;
        this.department = department;
        this.semester = semester;
        this.cgpa = cgpa;
    }

    public String getRegNo() { return regNo; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public int getSemester() { return semester; }
    public double getCgpa() { return cgpa; }

    @Override
    public String toString() {
        return String.format("Student{regNo='%s', name='%s', dept='%s', sem=%d, cgpa=%.2f}",
                regNo, name, department, semester, cgpa);
    }
}
