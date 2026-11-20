import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class RegistrationFrame extends JFrame {
    private final JTextField registrationNumberField = new JTextField();
    private final JTextField studentNameField = new JTextField();
    private final JComboBox<String> eventCombo = new JComboBox<>();
    private final JComboBox<String> filterCombo = new JComboBox<>();
    private final JTable registrationTable = new JTable();
    private final JLabel statusLabel = new JLabel("Ready - local storage");
    private final JButton previousButton = new JButton("Previous");
    private final JButton nextButton = new JButton("Next");
    private final JButton exportButton = new JButton("Export CSV");
    private final int pageSize = 5;
    private int currentOffset = 0;
    private final Map<String, Integer> eventIdCache = new HashMap<>();

    private final RegistrationService registrationService = new RegistrationService();

    public RegistrationFrame() {
        super("Campus Event Registration");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(RegistrationFrame.this, "Close the registration application?", "Exit", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });
        setLocationRelativeTo(null);
        buildInterface();
        loadEvents();
        loadPage();
    }

    private void buildInterface() {
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        formPanel.add(new JLabel("Registration Number:"));
        formPanel.add(registrationNumberField);
        formPanel.add(new JLabel("Student Name:"));
        formPanel.add(studentNameField);
        formPanel.add(new JLabel("Event:"));
        formPanel.add(eventCombo);
        formPanel.add(new JLabel("Filter:"));
        formPanel.add(filterCombo);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> registerStudent());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.add(registerButton);
        actionPanel.add(exportButton);
        exportButton.addActionListener(e -> exportCsv());

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Registrations"));
        tablePanel.add(new JScrollPane(registrationTable), BorderLayout.CENTER);

        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        previousButton.addActionListener(e -> {
            if (currentOffset >= pageSize) {
                currentOffset -= pageSize;
                loadPage();
            }
        });
        nextButton.addActionListener(e -> {
            currentOffset += pageSize;
            loadPage();
        });
        navigationPanel.add(previousButton);
        navigationPanel.add(nextButton);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(actionPanel, BorderLayout.NORTH);
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        setLayout(new BorderLayout(10, 10));
        add(formPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(navigationPanel, BorderLayout.SOUTH);
        add(statusPanel, BorderLayout.EAST);

        filterCombo.addActionListener(e -> {
            currentOffset = 0;
            loadPage();
        });
    }

    private void loadEvents() {
        SwingWorker<List<Event>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Event> doInBackground() throws Exception {
                return registrationService.getEvents();
            }

            @Override
            protected void done() {
                try {
                    List<Event> events = get();
                    eventCombo.removeAllItems();
                    filterCombo.removeAllItems();
                    eventIdCache.clear();
                    eventCombo.addItem("Select event");
                    filterCombo.addItem("All Events");
                    for (Event event : events) {
                        eventCombo.addItem(event.getEventName());
                        filterCombo.addItem(event.getEventName());
                        eventIdCache.put(event.getEventName(), event.getEventId());
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(RegistrationFrame.this,
                            "Unable to load event list: " + workerErrorMessage(ex), "Storage Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void registerStudent() {
        String registrationNumber = registrationNumberField.getText();
        String studentName = studentNameField.getText();
        String selectedEvent = (String) eventCombo.getSelectedItem();

        if (registrationNumber.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Registration number is required.", "Validation", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (studentName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Student name is required.", "Validation", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (selectedEvent == null || selectedEvent.equals("Select event")) {
            JOptionPane.showMessageDialog(this, "Please select an event.", "Validation", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Registration registration = new Registration();
                registration.setRegistrationNumber(registrationNumber.trim());
                registration.setStudentName(studentName.trim());
                registration.setEventId(resolveEventId(selectedEvent));
                registrationService.registerStudent(registration);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Registration successful.");
                    JOptionPane.showMessageDialog(RegistrationFrame.this, "Registration stored successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    registrationNumberField.setText("");
                    studentNameField.setText("");
                    currentOffset = 0;
                    loadPage();
                } catch (Exception ex) {
                    statusLabel.setText("Registration failed.");
                    JOptionPane.showMessageDialog(RegistrationFrame.this, workerErrorMessage(ex),
                            "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private int resolveEventId(String eventName) {
        Integer eventId = eventIdCache.get(eventName);
        return eventId == null ? 0 : eventId;
    }

    private void loadPage() {
        String selectedFilter = (String) filterCombo.getSelectedItem();
        previousButton.setEnabled(currentOffset > 0);
        nextButton.setEnabled(true);

        SwingWorker<List<Registration>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Registration> doInBackground() throws Exception {
                return registrationService.filterRegistrations(selectedFilter, currentOffset, pageSize);
            }

            @Override
            protected void done() {
                try {
                    List<Registration> rows = get();
                    DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Student", "Reg No", "Event", "Date"}, 0);
                    for (Registration registration : rows) {
                        model.addRow(new Object[]{registration.getRegistrationId(), registration.getStudentName(), registration.getRegistrationNumber(), registration.getEventName(), registration.getRegistrationDate()});
                    }
                    registrationTable.setModel(model);
                    previousButton.setEnabled(currentOffset > 0);
                    nextButton.setEnabled(rows.size() == pageSize);
                    statusLabel.setText("Loaded " + rows.size() + " records.");
                } catch (Exception ex) {
                    statusLabel.setText("Unable to load records.");
                    JOptionPane.showMessageDialog(RegistrationFrame.this, workerErrorMessage(ex),
                            "Storage Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select CSV export path");
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        final File exportFile = chooser.getSelectedFile();
        final File finalExportFile = exportFile.getName().toLowerCase().endsWith(".csv")
                ? exportFile
                : new File(exportFile.getAbsolutePath() + ".csv");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                String selectedFilter = (String) filterCombo.getSelectedItem();
                List<Registration> rows = registrationService.filterRegistrations(selectedFilter, 0, Integer.MAX_VALUE);
                registrationService.exportCsv(finalExportFile, rows);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(RegistrationFrame.this, "CSV exported to " + finalExportFile.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(RegistrationFrame.this, workerErrorMessage(ex),
                            "Export Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private String workerErrorMessage(Exception exception) {
        Throwable cause = exception;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? cause.toString() : cause.getMessage();
    }
}
