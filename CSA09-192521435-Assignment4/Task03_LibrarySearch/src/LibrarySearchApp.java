import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class LibrarySearchApp extends JFrame {
    private final JTextField searchField = new JTextField(25);
    private final JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"Title", "Author", "Subject"});
    private final JTextArea resultsArea = new JTextArea();
    private final JLabel statusLabel = new JLabel("Ready");
    private final List<String> catalog = new ArrayList<>();
    private final List<String> filteredRecords = new ArrayList<>();
    private int currentIndex = 0;

    public LibrarySearchApp() {
        super("Campus Library Search");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        loadCatalog();
        buildInterface();
    }

    private void loadCatalog() {
        catalog.add("Java Programming | K. Sharma | Computer Science");
        catalog.add("Data Structures | L. Rao | Computer Science");
        catalog.add("Operating Systems | P. Nair | Systems");
        catalog.add("World History | A. Singh | Humanities");
        catalog.add("Probability and Statistics | D. Reddy | Mathematics");
        catalog.add("Design Patterns | P. Joseph | Software Engineering");
        catalog.add("Digital Electronics | V. Kumar | Engineering");
        catalog.add("Literature Review | C. Martin | Humanities");
    }

    private void buildInterface() {
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.add(new JLabel("Campus Library Search"));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Category:"));
        searchPanel.add(categoryCombo);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> performSearch());
        searchPanel.add(searchButton);

        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        resultsArea.setEditable(false);
        resultsPanel.add(new JScrollPane(resultsArea), BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton previousButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");
        JButton reserveButton = new JButton("Reserve");

        previousButton.addActionListener(e -> previousPage());
        nextButton.addActionListener(e -> nextPage());
        reserveButton.addActionListener(e -> reserveBook());

        navPanel.add(previousButton);
        navPanel.add(nextButton);
        navPanel.add(reserveButton);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);

        setLayout(new BorderLayout(10, 10));
        add(titlePanel, BorderLayout.NORTH);
        add(searchPanel, BorderLayout.BEFORE_FIRST_LINE);
        add(resultsPanel, BorderLayout.CENTER);
        add(navPanel, BorderLayout.SOUTH);
        add(statusPanel, BorderLayout.EAST);

        resultsArea.setText("Type a keyword and press Search.");
    }

    private void performSearch() {
        String text = searchField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();
        filteredRecords.clear();

        if (text.isEmpty()) {
            statusLabel.setText("Search text is required.");
            JOptionPane.showMessageDialog(this, "Please enter a search term.", "Validation", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (String record : catalog) {
            String[] parts = record.split("\\|");
            String target = switch (category) {
                case "Title" -> parts[0].trim();
                case "Author" -> parts[1].trim();
                default -> parts[2].trim();
            };
            if (target.toLowerCase().contains(text.toLowerCase())) {
                filteredRecords.add(record);
            }
        }
        currentIndex = 0;
        showPage();
    }

    private void showPage() {
        if (filteredRecords.isEmpty()) {
            resultsArea.setText("No matching records found.");
            statusLabel.setText("No results");
            return;
        }

        int end = Math.min(currentIndex + 3, filteredRecords.size());
        List<String> pageRecords = filteredRecords.subList(currentIndex, end);
        resultsArea.setText(String.join("\n", pageRecords));
        statusLabel.setText("Showing " + (currentIndex + 1) + " to " + end + " of " + filteredRecords.size());
    }

    private void previousPage() {
        if (filteredRecords.isEmpty()) {
            return;
        }
        if (currentIndex >= 3) {
            currentIndex -= 3;
            showPage();
        }
    }

    private void nextPage() {
        if (filteredRecords.isEmpty()) {
            return;
        }
        if (currentIndex + 3 < filteredRecords.size()) {
            currentIndex += 3;
            showPage();
        }
    }

    private void reserveBook() {
        if (filteredRecords.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records to reserve.", "Reservation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String record = filteredRecords.get(currentIndex);
        JOptionPane.showMessageDialog(this, "Reserved: " + record, "Reservation", JOptionPane.INFORMATION_MESSAGE);
        statusLabel.setText("Reservation made");
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new LibrarySearchApp().setVisible(true));
    }
}
