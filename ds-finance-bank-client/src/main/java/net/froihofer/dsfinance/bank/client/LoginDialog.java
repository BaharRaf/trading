package net.froihofer.dsfinance.bank.client;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private String username;
    private String password;
    private boolean authenticated = false;

    public LoginDialog(JFrame parent) {
        super(parent, "Login", true); // Modal dialog
        setSize(350, 200);
        setLocationRelativeTo(parent);

        // Create panel with form layout
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Username
        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        // Password
        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());
        panel.add(new JLabel()); // Empty cell
        panel.add(loginButton);

        add(panel);
    }

    private void handleLogin() {
        username = usernameField.getText();
        password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        authenticated = true;
        dispose(); // Close dialog
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean isAuthenticated() { return authenticated; }
}
