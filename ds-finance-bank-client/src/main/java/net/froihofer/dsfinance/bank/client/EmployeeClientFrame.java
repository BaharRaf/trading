package net.froihofer.dsfinance.bank.client;

import net.froihofer.dsfinance.bank.api.EmployeeBankService;
import javax.swing.*;
import java.awt.*;

public class EmployeeClientFrame extends JFrame {

    private BankServiceConnector connector;
    private EmployeeBankService employeeService;

    public EmployeeClientFrame() {
        setTitle("DS Finance Bank - Employee Client");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Show login dialog
        if (!performLogin()) {
            System.exit(0);
        }

        // Create tabbed interface
        JTabbedPane tabbedPane = new JTabbedPane();

        // Add tabs
        tabbedPane.addTab("Customer Management",
                new CustomerManagementPanel(employeeService));
        tabbedPane.addTab("Stock Trading",
                new JLabel("Coming soon..."));
        tabbedPane.addTab("Portfolio View",
                new JLabel("Coming soon..."));

        add(tabbedPane);
        setVisible(true);
    }

    private boolean performLogin() {
        LoginDialog loginDialog = new LoginDialog(this);
        loginDialog.setVisible(true);

        if (!loginDialog.isAuthenticated()) {
            return false;
        }

        try {
            connector = new BankServiceConnector();
            connector.connect(loginDialog.getUsername(), loginDialog.getPassword());
            employeeService = connector.getEmployeeService();

            JOptionPane.showMessageDialog(this,
                    "Successfully connected to bank server!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            return true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to connect: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EmployeeClientFrame());
    }
}
