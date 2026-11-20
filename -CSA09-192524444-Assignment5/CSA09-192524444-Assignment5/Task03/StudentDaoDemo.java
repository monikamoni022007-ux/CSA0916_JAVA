import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class StudentDaoDemo {
    public static void main(String[] args) {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/csa09_assign5_test");
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String pass = System.getenv().getOrDefault("DB_PASS", "root");

        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            StudentDao dao = new StudentDao(con);
            dao.delete("192311001");
            Student s = new Student("192311001", "Arun Kumar", "CSE", 5, 8.92);
            System.out.println("Inserted : " + dao.insert(s));
            System.out.println("FindById : " + dao.findById("192311001"));
            Student updated = new Student("192311001", "Arun Kumar", "CSE", 6, 9.10);
            System.out.println("Updated  : " + dao.update(updated));
            System.out.println("FindAll  : " + dao.findAll());
            System.out.println("Deleted  : " + dao.delete("192311001"));
        } catch (SQLException e) {
            System.err.println("DB Error (ensure MySQL is running and Task03/database.sql is loaded): " + e.getMessage());
        }
    }
}
