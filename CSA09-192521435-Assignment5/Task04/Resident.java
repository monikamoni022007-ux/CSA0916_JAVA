import java.io.Serializable;
import java.sql.Connection;

public class Resident implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String regNo;
    private transient String password;
    private transient Connection dbConnection;
    private String phoneNumber;

    public Resident(String name, String regNo, String password, Connection dbConnection) {
        this.name = name;
        this.regNo = regNo;
        this.password = password;
        this.dbConnection = dbConnection;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    @Override
    public String toString() {
        return String.format("Resident{name='%s', regNo='%s', password=%s, dbConnection=%s, phone=%s}",
                name, regNo, password, dbConnection, phoneNumber);
    }
}
