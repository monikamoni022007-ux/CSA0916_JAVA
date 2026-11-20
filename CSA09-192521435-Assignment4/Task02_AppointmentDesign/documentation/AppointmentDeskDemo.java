import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AppointmentDeskDemo extends JFrame {
    private final JTextField patientIdField = new JTextField();
    private final JComboBox<String> departmentCombo = new JComboBox<>(new String[]{"Cardiology", "Neurology", "Orthopedics"});
    private final JComboBox<String> doctorCombo = new JComboBox<>();
    private final JComboBox<String> slotCombo = new JComboBox<>(new String[]{"09:00", "10:30", "12:00", "15:00"});
    private final JLabel statusLabel = new JLabel("Waiting for appointment request.");
    private final Map<String, List<String>> doctorsByDepartment = new HashMap<>();

    public AppointmentDeskDemo() {
        super("Hospital Outpatient Appointment Desk");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int answer = JOptionPane.showConfirmDialog(AppointmentDeskDemo.this, "Close appointment desk?", "Exit", JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });
        setSize(500, 320);
        setLocationRelativeTo(null);
        loadDoctorMap();
        buildInterface();
        refreshDoctors();
    }

    private void loadDoctorMap() {
        doctorsByDepartment.put("Cardiology", Arrays.asList("Dr. Sheela", "Dr. Rohan"));
        doctorsByDepartment.put("Neurology", Arrays.asList("Dr. Priya", "Dr. Irfan"));
        doctorsByDepartment.put("Orthopedics", Arrays.asList("Dr. Nair", "Dr. Siddarth"));
    }

    private void buildInterface() {
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.add(new JLabel("Patient ID:"));
        formPanel.add(patientIdField);
        formPanel.add(new JLabel("Department:"));
        formPanel.add(departmentCombo);
        formPanel.add(new JLabel("Doctor:"));
        formPanel.add(doctorCombo);
        formPanel.add(new JLabel("Time Slot:"));
        formPanel.add(slotCombo);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusLabel);

        JButton bookButton = new JButton("Book Appointment");
        bookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleBooking();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(bookButton);
        setLayout(new BorderLayout(10, 10));
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        departmentCombo.addActionListener(e -> refreshDoctors());
    }

    private void refreshDoctors() {
        String selectedDepartment = (String) departmentCombo.getSelectedItem();
        doctorCombo.removeAllItems();
        List<String> doctors = doctorsByDepartment.getOrDefault(selectedDepartment, List.of());
        for (String doctor : doctors) {
            doctorCombo.addItem(doctor);
        }
    }

    private void handleBooking() {
        String patientId = patientIdField.getText().trim();
        String department = (String) departmentCombo.getSelectedItem();
        String doctor = (String) doctorCombo.getSelectedItem();
        String slot = (String) slotCombo.getSelectedItem();

        if (patientId.isEmpty()) {
            statusLabel.setText("Patient ID is required.");
            JOptionPane.showMessageDialog(this, "Please enter a patient ID.", "Validation", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (department == null || department.isEmpty()) {
            statusLabel.setText("Department is required.");
            JOptionPane.showMessageDialog(this, "Please select a department.", "Validation", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (doctor == null || doctor.isEmpty()) {
            statusLabel.setText("Doctor is required.");
            JOptionPane.showMessageDialog(this, "Please select a doctor.", "Validation", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (slot == null || slot.isEmpty()) {
            statusLabel.setText("Time slot is required.");
            JOptionPane.showMessageDialog(this, "Please select a time slot.", "Validation", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String message = "Appointment booked for " + patientId + " with " + doctor + " on " + slot + " in " + department + ".";
        statusLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "Appointment Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new AppointmentDeskDemo().setVisible(true));
    }
}
