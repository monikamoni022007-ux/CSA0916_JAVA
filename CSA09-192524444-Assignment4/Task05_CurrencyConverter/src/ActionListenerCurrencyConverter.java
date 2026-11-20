import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ActionListenerCurrencyConverter extends JFrame implements ActionListener {
    private static final double USD_TO_INR = 83.0;
    private static final double EUR_TO_INR = 90.0;

    private final JTextField amountField = new JTextField();
    private final JComboBox<String> fromCombo = new JComboBox<>(new String[]{"INR", "USD", "EUR"});
    private final JComboBox<String> toCombo = new JComboBox<>(new String[]{"INR", "USD", "EUR"});
    private final JButton convertButton = new JButton("Convert");
    private final JLabel resultLabel = new JLabel("Result: ");

    public ActionListenerCurrencyConverter() {
        setTitle("Currency Converter - ActionListener");
        setSize(400, 220);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        buildUi();
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.add(new JLabel("Amount:"));
        panel.add(amountField);
        panel.add(new JLabel("From:"));
        panel.add(fromCombo);
        panel.add(new JLabel("To:"));
        panel.add(toCombo);
        panel.add(new JLabel("Result:"));
        panel.add(resultLabel);
        add(panel, BorderLayout.CENTER);
        add(convertButton, BorderLayout.SOUTH);
        amountField.addActionListener(this);
        convertButton.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String inputText = amountField.getText().trim();
        if (inputText.isEmpty()) {
            resultLabel.setText("Result: Invalid amount");
            JOptionPane.showMessageDialog(this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double amount = Double.parseDouble(inputText);
            if (amount < 0) {
                resultLabel.setText("Result: Invalid amount");
                JOptionPane.showMessageDialog(this, "Amount cannot be negative", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double converted = convert(amount, (String) fromCombo.getSelectedItem(), (String) toCombo.getSelectedItem());
            resultLabel.setText(String.format("Result: %.2f", converted));
            JOptionPane.showMessageDialog(this, "Converted amount: " + String.format("%.2f", converted));
        } catch (NumberFormatException ex) {
            resultLabel.setText("Result: Invalid amount");
            JOptionPane.showMessageDialog(this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double convert(double amount, String fromCurrency, String toCurrency) {
        double fromRate = switch (fromCurrency) {
            case "USD" -> USD_TO_INR;
            case "EUR" -> EUR_TO_INR;
            default -> 1.0;
        };
        double toRate = switch (toCurrency) {
            case "USD" -> USD_TO_INR;
            case "EUR" -> EUR_TO_INR;
            default -> 1.0;
        };
        double inrValue = amount * fromRate;
        return inrValue / toRate;
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new ActionListenerCurrencyConverter().setVisible(true));
    }
}
