package campusparking.app;

import java.time.LocalDate;
import java.time.LocalDateTime;

import campusparking.model.Car;
import campusparking.model.DeliveryVan;
import campusparking.model.MonthlyPass;
import campusparking.model.ParkingUser;
import campusparking.model.UserRole;
import campusparking.service.ParkingService;

public class Main {
    public static void main(String[] args) {
        ParkingService service = new ParkingService(new int[] {2, 3, 4, 5, 6});

        ParkingUser staff = new ParkingUser(
            "U100",
            "Sam",
            UserRole.STAFF,
            new MonthlyPass(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31))
        );
        ParkingUser deliveryUser = new ParkingUser("U300", "Morgan", UserRole.STUDENT);

        service.enterVehicle(new Car("TN01AA1111"), staff, LocalDateTime.of(2025, 1, 20, 8, 30));
        service.enterVehicle(new DeliveryVan("TN03CC3333"), deliveryUser, LocalDateTime.of(2025, 1, 20, 8, 45));

        System.out.println("Initial entry complete. Active vehicles: 2");
        System.out.println("Car fee: " + service.exitVehicle("TN01AA1111", LocalDateTime.of(2025, 1, 20, 10, 40)));
        System.out.println("Delivery van fee: " + service.exitVehicle("TN03CC3333", LocalDateTime.of(2025, 1, 20, 17, 30)));
    }
}
