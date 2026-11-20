public class Registration {
    private int registrationId;
    private String registrationNumber;
    private String studentName;
    private int eventId;
    private String eventName;
    private String registrationDate;

    public Registration() {}

    public Registration(int registrationId, String registrationNumber, String studentName, int eventId, String eventName, String registrationDate) {
        this.registrationId = registrationId;
        this.registrationNumber = registrationNumber;
        this.studentName = studentName;
        this.eventId = eventId;
        this.eventName = eventName;
        this.registrationDate = registrationDate;
    }

    public int getRegistrationId() { return registrationId; }
    public void setRegistrationId(int registrationId) { this.registrationId = registrationId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
}
