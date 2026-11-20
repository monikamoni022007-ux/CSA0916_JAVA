import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDao implements Dao<Student, String> {
    private final Connection connection;

    public StudentDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean insert(Student student) throws SQLException {
        String sql = "INSERT INTO student (reg_no, name, department, semester, cgpa) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, student.getRegNo());
            ps.setString(2, student.getName());
            ps.setString(3, student.getDepartment());
            ps.setInt(4, student.getSemester());
            ps.setDouble(5, student.getCgpa());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public Optional<Student> findById(String regNo) throws SQLException {
        String sql = "SELECT reg_no, name, department, semester, cgpa FROM student WHERE reg_no = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, regNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToStudent(rs));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<Student> findAll() throws SQLException {
        String sql = "SELECT reg_no, name, department, semester, cgpa FROM student ORDER BY reg_no";
        List<Student> students = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                students.add(mapRowToStudent(rs));
            }
        }
        return students;
    }

    @Override
    public boolean update(Student student) throws SQLException {
        String sql = "UPDATE student SET name = ?, department = ?, semester = ?, cgpa = ? WHERE reg_no = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, student.getName());
            ps.setString(2, student.getDepartment());
            ps.setInt(3, student.getSemester());
            ps.setDouble(4, student.getCgpa());
            ps.setString(5, student.getRegNo());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(String regNo) throws SQLException {
        String sql = "DELETE FROM student WHERE reg_no = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, regNo);
            return ps.executeUpdate() == 1;
        }
    }

    private Student mapRowToStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getString("reg_no"),
                rs.getString("name"),
                rs.getString("department"),
                rs.getInt("semester"),
                rs.getDouble("cgpa")
        );
    }
}
