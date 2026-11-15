import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Triage {
    private final PriorityQueue<Patient> queue = new PriorityQueue<>(
            Comparator.comparingInt((Patient patient) -> patient.severity)
                    .thenComparingLong(patient -> patient.arrival));
    private long counter = 0;

    void admit(String name, int severity) {
        queue.add(new Patient(name, severity, counter++));
    }

    Patient next() {
        return queue.poll();
    }

    int size() {
        return queue.size();
    }

    List<Patient> waitingInTreatmentOrder() {
        PriorityQueue<Patient> copy = new PriorityQueue<>(queue);
        List<Patient> ordered = new ArrayList<>(copy.size());
        Patient patient;
        while ((patient = copy.poll()) != null) {
            ordered.add(patient);
        }
        return ordered;
    }

    public static void main(String[] args) {
        Triage er = new Triage();
        er.admit("Ravi", 3);
        er.admit("Meena", 1);
        er.admit("John", 3);
        er.admit("Asha", 5);
        er.admit("Kumar", 1);
        er.admit("Lata", 2);
        StringBuilder order = new StringBuilder();
        Patient patient;
        while ((patient = er.next()) != null) {
            order.append(patient).append(' ');
        }
        System.out.println(order.toString().trim());
    }
}
