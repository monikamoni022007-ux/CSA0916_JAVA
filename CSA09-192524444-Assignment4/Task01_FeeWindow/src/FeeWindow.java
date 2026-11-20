import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

public class FeeWindow extends JFrame {
    private final JTextField regNumberField = new JTextField();
    private final JTextField amountField = new JTextField();
    private final JLabel statusLabel = new JLabel("Ready");
    private final FeeService feeService = new FeeService();
    private String lastReceipt = "";

    public FeeWindow() {
        super("Fee Payment Window");
        setSize(420, 260);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(FeeWindow.this,
                        "Do you want to close the payment window?",
                        "Exit",
                        JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        form.add(new JLabel("Registration No:"));
        form.add(regNumberField);
        form.add(new JLabel("Amount:"));
        form.add(amountField);
        form.add(new JLabel("Status:"));
        form.add(statusLabel);

        JPanel buttons = new JPanel(new GridLayout(1, 3, 10, 10));
        JButton payButton = new JButton("Pay");
        JButton clearButton = new JButton("Clear");
        JButton receiptButton = new JButton("Receipt");

        payButton.addActionListener(e -> handlePay());
        clearButton.addActionListener(e -> clearFields());
        receiptButton.addActionListener(e -> showReceipt());
        buttons.add(payButton);
        buttons.add(clearButton);
        buttons.add(receiptButton);

        setLayout(new BorderLayout(10, 10));
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void handlePay() {
        String regNumber = regNumberField.getText();
        String amountText = amountField.getText();

        if (!feeService.validateRegistrationNumber(regNumber)) {
            statusLabel.setText("Invalid registration number.");
            JOptionPane.showMessageDialog(this,
                    "Registration number must look like ABC123.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!feeService.validateAmount(amountText)) {
            statusLabel.setText("Invalid payment amount.");
            JOptionPane.showMessageDialog(this,
                    "Amount must be greater than zero.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Fee fee = new Fee(regNumber.trim(), feeService.parseAmount(amountText));
        statusLabel.setText("Processing payment...");
        setEnabled(false);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return feeService.processPayment(fee);
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    lastReceipt = result;
                    statusLabel.setText(result);
                    JOptionPane.showMessageDialog(FeeWindow.this, result, "Payment Result", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    statusLabel.setText("Payment failed.");
                    JOptionPane.showMessageDialog(FeeWindow.this, ex.getMessage(), "Payment Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void clearFields() {
        regNumberField.setText("");
        amountField.setText("");
        statusLabel.setText("Ready");
    }

    private void showReceipt() {
        if (lastReceipt == null || lastReceipt.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No transaction exists yet.", "Receipt", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "Receipt:\n" + lastReceipt, "Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new FeeWindow().setVisible(true));
    }
}
