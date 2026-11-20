import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SerializationDemo {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Hostel hostel = new Hostel("Cauvery Mens Hostel");
        Room r101 = new Room(101);
        r101.addResident(new Resident("Arun Kumar", "192311001", "MyPlaintextPass#1", null));
        r101.addResident(new Resident("Divya S", "192311003", "SecretPass#2", null));
        hostel.addRoom(r101);

        Path serFile = Files.createTempFile("hostel_state_", ".ser");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serFile.toFile()))) {
            oos.writeObject(hostel);
        }
        System.out.println("Serialized Hostel object graph to: " + serFile + " (" + Files.size(serFile) + " bytes)");

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(serFile.toFile()))) {
            Hostel loaded = (Hostel) ois.readObject();
            System.out.println("Deserialized Hostel: " + loaded.getHostelName());
            for (Room rm : loaded.getRooms()) {
                for (Resident res : rm.getResidents()) {
                    System.out.println("  -> " + res);
                }
            }
        } finally {
            Files.deleteIfExists(serFile);
        }
    }
}
