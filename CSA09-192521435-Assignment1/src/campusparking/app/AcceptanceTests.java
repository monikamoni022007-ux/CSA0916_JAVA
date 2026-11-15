package campusparking.app;

import java.time.LocalDate;
import java.time.LocalDateTime;

import campusparking.exception.InvalidParkingOperationException;
import campusparking.model.Car;
import campusparking.model.DeliveryVan;
import campusparking.model.EBike;
import campusparking.model.ElectricCar;
import campusparking.model.MonthlyPass;
import campusparking.model.ParkingTicket;
import campusparking.model.ParkingUser;
import campusparking.model.TwoWheeler;
import campusparking.model.UserRole;
import campusparking.model.Vehicle;
import campusparking.model.VehicleFactory;
import campusparking.model.VisitorPass;
import campusparking.service.ParkingService;

public final class AcceptanceTests {
    private static int passed;

    private AcceptanceTests() {}

    public static void main(String[] args) {
        runAll();
    }

    public static void runAll() {
        passed = 0;
        System.out.println("Campus Smart-Parking acceptance tests");
        testVehicleFeesAndBoundaries();
        testFiveUnequalFloorsAndTicketIdentity();
        testParkingOperationsAndFailures();
        testPassesAndEvCharging();
        testDayFourPricing();
        testManualDailyCollection();
        System.out.println("Result: " + passed + " checks passed");
    }

    public static void runTask12() {
        passed = 0;
        System.out.println("Task 12 Day-4 design challenge tests");
        testDayFourPricing();
        testPassesAndEvCharging();
        testFiveUnequalFloorsAndTicketIdentity();
        System.out.println("Result: " + passed + " checks passed");
    }

    private static void testVehicleFeesAndBoundaries() {
        Vehicle bike = new TwoWheeler("tn01a1");
        Vehicle car = new Car("TN02B2");
        check("2-wheeler 0 minutes", bike.calculateFee(0), 0.0);
        check("2-wheeler 1 minute", bike.calculateFee(1), 10.0);
        check("2-wheeler 60 minutes", bike.calculateFee(60), 10.0);
        check("2-wheeler 61 minutes", bike.calculateFee(61), 20.0);
        check("2-wheeler 130 minutes", bike.calculateFee(130), 30.0);
        check("car 130 minutes", car.calculateFee(130), 90.0);
        expectFailure("negative duration rejected", () -> car.calculateFee(-1));
        expectFailure("blank registration rejected", () -> new Car("  "));
        expectFailure("unknown vehicle type rejected",
            () -> VehicleFactory.create("AIRCRAFT", "TYPE-1", null));
        expectFailure("e-bike factory requires an explicit flat fee",
            () -> VehicleFactory.create("EBIKE", "EB-3", null));
    }

    private static void testFiveUnequalFloorsAndTicketIdentity() {
        ParkingService service = new ParkingService(new int[] {1, 2, 1, 3, 2});
        ParkingUser visitor = visitorWithoutPass("V-FLOOR");
        LocalDateTime entry = LocalDateTime.of(2026, 11, 13, 8, 0);
        ParkingTicket first = service.enterVehicle(new Car("FLOOR-1"), visitor, entry);
        ParkingTicket second = service.enterVehicle(new Car("FLOOR-2"), visitor, entry);
        check("first floor allocation", first.getFloor() == 0 && first.getSlot() == 0);
        check("next floor allocation with unequal capacities", second.getFloor() == 1 && second.getSlot() == 0);
        check("ticket numbers are unique", first.getTicketId() != second.getTicketId());
    }

    private static void testParkingOperationsAndFailures() {
        ParkingService oneSlot = new ParkingService(new int[] {1});
        ParkingUser visitor = visitorWithoutPass("V-OPS");
        LocalDateTime entry = LocalDateTime.of(2026, 11, 13, 10, 0);
        oneSlot.enterVehicle(new Car("LAST-SLOT"), visitor, entry);
        expectFailure("duplicate active registration rejected",
            () -> oneSlot.enterVehicle(new Car("last-slot"), visitor, entry));
        expectFailure("full facility rejects entry",
            () -> oneSlot.enterVehicle(new Car("NO-SPACE"), visitor, entry));
        expectFailure("unknown vehicle exit rejected",
            () -> oneSlot.exitVehicle("NOT-PARKED", entry.plusHours(1)));
        expectFailure("exit before entry rejected without releasing slot",
            () -> oneSlot.exitVehicle("LAST-SLOT", entry.minusMinutes(1)));
        check("vehicle remains parked after invalid exit",
            oneSlot.exitVehicle("LAST-SLOT", entry), 0.0);
        ParkingTicket replacement = oneSlot.enterVehicle(new Car("REPLACEMENT"), visitor, entry);
        check("released slot can be reused", replacement.getFloor() == 0 && replacement.getSlot() == 0);

        campusparking.model.ParkingFloor floor = new campusparking.model.ParkingFloor(0, 1);
        check("invalid slot release rejected", !floor.release(-1));
        expectFailure("occupied slot cannot be allocated twice", () -> {
            floor.allocate("SLOT-1", 0);
            floor.allocate("SLOT-2", 0);
        });
    }

    private static void testPassesAndEvCharging() {
        LocalDate date = LocalDate.of(2026, 11, 13);
        ParkingService service = new ParkingService(new int[] {2});
        ParkingUser staff = new ParkingUser(
            "STAFF-1", "Staff User", UserRole.STAFF,
            new MonthlyPass(date.minusDays(1), date.plusDays(28))
        );
        service.enterVehicle(new Car("MONTHLY-PASS"), staff, date.atTime(8, 0));
        check("valid monthly pass waives parking fee",
            service.exitVehicle("MONTHLY-PASS", date.atTime(10, 0)), 0.0);

        ParkingUser visitor = new ParkingUser(
            "VISITOR-1", "Visitor User", UserRole.VISITOR, new VisitorPass(date)
        );
        service.enterVehicle(new Car("VISITOR-PASS"), visitor, date.atTime(8, 0));
        check("visitor pass does not waive normal parking fee",
            service.exitVehicle("VISITOR-PASS", date.atTime(9, 0)), 30.0);

        ParkingUser expiredVisitor = new ParkingUser(
            "VISITOR-2", "Expired Visitor", UserRole.VISITOR, new VisitorPass(date.minusDays(1))
        );
        expectFailure("expired one-day pass cannot be used",
            () -> service.enterVehicle(new Car("EXPIRED-PASS"), expiredVisitor, date.atTime(8, 0)));

        ParkingUser student = new ParkingUser(
            "STUDENT-1", "Student User", UserRole.STUDENT,
            new MonthlyPass(date.minusDays(1), date.plusDays(28))
        );
        service.enterVehicle(new ElectricCar("EV-PASS"), student, date.atTime(8, 0));
        check("monthly pass leaves separately metered EV charging payable",
            service.exitVehicle("EV-PASS", date.atTime(10, 0), 10), 120.0);
        expectFailure("negative EV energy rejected",
            () -> new ElectricCar("EV-NEG").chargingFee(-1));
        expectFailure("negative charging usage rejected",
            () -> service.exitVehicle("EV-PASS", date.atTime(10, 0), -1));
    }

    private static void testDayFourPricing() {
        LocalDateTime day = LocalDateTime.of(2026, 11, 13, 0, 0);
        EBike configuredEBike = (EBike) VehicleFactory.create("EBIKE", "EB-1", 17.50);
        check("e-bike uses configured flat fee for 1 minute",
            configuredEBike.calculateFee(1), 17.50);
        check("e-bike uses configured flat fee for long stay",
            configuredEBike.calculateFee(600), 17.50);
        check("e-bike factory accepts hyphenated type names",
            VehicleFactory.create("E-BIKE", "EB-2", 17.50) instanceof EBike);
        check("delivery van entirely before 9 AM",
            new DeliveryVan("VAN-1").calculateFee(day.withHour(8), day.withHour(9)), 30.0);
        check("delivery van entry at 9 AM",
            new DeliveryVan("VAN-2").calculateFee(day.withHour(9), day.withHour(10)), 60.0);
        check("delivery van exit at 5 PM",
            new DeliveryVan("VAN-3").calculateFee(day.withHour(16), day.withHour(17)), 60.0);
        check("delivery van entirely after 5 PM",
            new DeliveryVan("VAN-4").calculateFee(day.withHour(17), day.withHour(18)), 30.0);
        check("delivery van partial peak overlap",
            new DeliveryVan("VAN-5").calculateFee(day.withHour(8).plusMinutes(30),
                day.withHour(9).plusMinutes(30)), 45.0);
        check("delivery van spans both peak boundaries",
            new DeliveryVan("VAN-6").calculateFee(day.withHour(8).plusMinutes(30),
                day.withHour(17).plusMinutes(30)), 510.0);
        check("delivery van across midnight",
            new DeliveryVan("VAN-7").calculateFee(day.withHour(16),
                day.plusDays(1).withHour(10)), 600.0);
    }

    private static void testManualDailyCollection() {
        ParkingService service = new ParkingService(new int[] {1, 2, 1, 1, 1});
        LocalDate date = LocalDate.of(2026, 11, 13);
        ParkingUser regular = visitorWithoutPass("DAILY-COLLECTION");
        service.enterVehicle(new Car("DAY-CAR"), regular, date.atTime(7, 0));
        service.enterVehicle(new TwoWheeler("DAY-BIKE"), regular, date.atTime(7, 0));
        service.enterVehicle(new DeliveryVan("DAY-VAN"), regular, date.atTime(8, 30));
        service.enterVehicle(new ElectricCar("DAY-EV"), regular, date.atTime(7, 0));

        double collection = 0.0;
        collection += service.exitVehicle("DAY-CAR", date.atTime(9, 10)); // 3 hours x Rs 30
        collection += service.exitVehicle("DAY-BIKE", date.atTime(8, 0)); // 1 hour x Rs 10
        collection += service.exitVehicle("DAY-VAN", date.atTime(9, 30)); // 30 min x Rs 30 + 30 min x Rs 60
        collection += service.exitVehicle("DAY-EV", date.atTime(9, 10), 10); // Rs 90 parking + Rs 120 charging
        check("hand-calculated daily collection (90+10+45+210)", collection, 355.0);
    }

    private static ParkingUser visitorWithoutPass(String id) {
        return new ParkingUser(id, "Visitor", UserRole.VISITOR);
    }

    private static void check(String label, boolean condition) {
        if (!condition) {
            throw new AssertionError("FAIL: " + label);
        }
        passed++;
        System.out.println("PASS: " + label);
    }

    private static void check(String label, double actual, double expected) {
        if (Math.abs(actual - expected) > 0.000001) {
            throw new AssertionError("FAIL: " + label + "; expected " + expected + ", got " + actual);
        }
        passed++;
        System.out.println("PASS: " + label + " = " + actual);
    }

    private static void expectFailure(String label, Runnable operation) {
        try {
            operation.run();
        } catch (IllegalArgumentException | IllegalStateException expected) {
            passed++;
            System.out.println("PASS: " + label);
            return;
        }
        throw new AssertionError("FAIL: " + label + " (no exception thrown)");
    }
}
