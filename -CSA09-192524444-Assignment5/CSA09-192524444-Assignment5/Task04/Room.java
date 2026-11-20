import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private int roomNumber;
    private List<Resident> residents = new ArrayList<>();

    public Room(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void addResident(Resident resident) {
        residents.add(resident);
    }

    public int getRoomNumber() { return roomNumber; }
    public List<Resident> getResidents() { return residents; }
}
