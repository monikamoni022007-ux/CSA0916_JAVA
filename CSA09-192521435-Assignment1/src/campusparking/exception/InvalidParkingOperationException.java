package campusparking.exception;

public class InvalidParkingOperationException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public InvalidParkingOperationException(String message) {
        super(message);
    }
}
