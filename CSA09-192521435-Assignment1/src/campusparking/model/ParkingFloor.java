package campusparking.model;

import campusparking.exception.InvalidParkingOperationException;

public class ParkingFloor {
    private final int floorNumber;
    private final String[] slots;

    public ParkingFloor(int floorNumber, int slotCount) {
        if (floorNumber < 0) {
            throw new IllegalArgumentException("Floor number cannot be negative");
        }
        if (slotCount < 0) {
            throw new IllegalArgumentException("Slot count cannot be negative");
        }
        this.floorNumber = floorNumber;
        this.slots = new String[slotCount];
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public int getSlotCount() {
        return slots.length;
    }

    public int findFirstFreeSlotIndex() {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public boolean allocate(String regNo, int slotIndex) {
        if (regNo == null || regNo.trim().isEmpty()) {
            throw new InvalidParkingOperationException("Registration number cannot be empty");
        }
        if (slotIndex < 0 || slotIndex >= slots.length || slots[slotIndex] != null) {
            throw new InvalidParkingOperationException("Unable to allocate slot " + slotIndex + " on floor " + floorNumber);
        }
        slots[slotIndex] = regNo;
        return true;
    }

    public String getOccupant(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.length) {
            throw new InvalidParkingOperationException("Invalid slot " + slotIndex + " on floor " + floorNumber);
        }
        return slots[slotIndex];
    }

    public boolean release(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.length) {
            return false;
        }
        if (slots[slotIndex] == null) {
            return false;
        }
        slots[slotIndex] = null;
        return true;
    }
}
