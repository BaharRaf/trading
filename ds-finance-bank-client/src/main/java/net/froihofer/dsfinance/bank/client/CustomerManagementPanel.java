package net.froihofer.dsfinance.bank.client;

import net.froihofer.dsfinance.bank.api.EmployeeBankService;
import net.froihofer.dsfinance.bank.dto.CustomerDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomerManagementPanel extends JPanel {

    private final EmployeeBankService service;

    private JTextField firstNameField, lastNameField, addressField;
    private JTextField searchFirstNameField, searchLastNameField;
    private JTable customerTable;
    private DefaultTableModel tableModel;

    private JTextField txtLoginUsername;
    private JPasswordField txtInitialPassword;

    public CustomerManagementPanel(EmployeeBankService service) {
        this.service = service;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        topPanel.add(createCreateCustomerPanel());
        topPanel.add(createSearchCustomerPanel());

        add(topPanel, BorderLayout.NORTH);
        add(createCustomerTablePanel(), BorderLayout.CENTER);
    }

    private JPanel createCreateCustomerPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Create New Customer"));

        panel.add(new JLabel("First Name:"));
        firstNameField = new JTextField();
        panel.add(firstNameField);

        panel.add(new JLabel("Last Name:"));
        lastNameField = new JTextField();
        panel.add(lastNameField);

        panel.add(new JLabel("Address:"));
        addressField = new JTextField();
        panel.add(addressField);

        panel.add(new JLabel("Login Username (WildFly):"));
        txtLoginUsername = new JTextField();
        panel.add(txtLoginUsername);

        panel.add(new JLabel("Initial Password (WildFly):"));
        txtInitialPassword = new JPasswordField();
        panel.add(txtInitialPassword);

        panel.add(new JLabel(""));
        JButton btnCreate = new JButton("Create Customer");
        btnCreate.addActionListener(e -> createCustomer());
        panel.add(btnCreate);

        return panel;
    }

    private JPanel createSearchCustomerPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Search Customers"));

        panel.add(new JLabel("First Name:"));
        searchFirstNameField = new JTextField();
        panel.add(searchFirstNameField);

        panel.add(new JLabel("Last Name:"));
        searchLastNameField = new JTextField();
        panel.add(searchLastNameField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchCustomers());
        panel.add(new JLabel());
        panel.add(searchButton);

        return panel;
    }

    private JPanel createCustomerTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Customer List"));

        // Include username for transparency / debugging and demo
        String[] columns = {"ID", "Customer Number", "First Name", "Last Name", "Address", "Username"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        customerTable = new JTable(tableModel);
        panel.add(new JScrollPane(customerTable), BorderLayout.CENTER);
        return panel;
    }

    private void createCustomer() {
        SwingWorker<Long, Void> worker = new SwingWorker<>() {
            @Override
            protected Long doInBackground() throws Exception {
                String firstName = firstNameField.getText().trim();
                String lastName  = lastNameField.getText().trim();
                String address   = addressField.getText().trim();

                String loginUser = txtLoginUsername.getText().trim();
                String loginPass = new String(txtInitialPassword.getPassword());

                if (firstName.isEmpty() || lastName.isEmpty()) {
                    throw new IllegalArgumentException("First and Last name are required");
                }
                if (loginUser.isEmpty()) {
                    throw new IllegalArgumentException("Login Username (WildFly) is required");
                }
                if (loginPass.isEmpty()) {
                    throw new IllegalArgumentException("Initial Password (WildFly) is required for customer authentication");
                }

                CustomerDTO customer = new CustomerDTO();
                customer.setFirstName(firstName);
                customer.setLastName(lastName);
                customer.setAddress(address);

                // CRITICAL FIX: Generate customerNumber (required by EJB)
                // Using timestamp + username to ensure uniqueness
                String customerNumber = "CUST-" + System.currentTimeMillis() + "-" + loginUser;
                customer.setCustomerNumber(customerNumber);

                // Critical: DB mapping must match principal name
                customer.setUsername(loginUser);

                // Set password to trigger server-side WildFly user creation
                customer.setInitialPassword(loginPass);

                return service.createCustomer(customer);
            }

            @Override
            protected void done() {
                try {
                    Long customerId = get();
                    JOptionPane.showMessageDialog(CustomerManagementPanel.this,
                            "Customer created successfully!\nID: " + customerId + "\n\n" +
                            "You can now log in to Customer GUI with:\n" +
                            "Username: " + txtLoginUsername.getText().trim() + "\n" +
                            "Password: (the one you entered)",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    firstNameField.setText("");
                    lastNameField.setText("");
                    addressField.setText("");
                    txtLoginUsername.setText("");
                    txtInitialPassword.setText("");

                    searchCustomers();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CustomerManagementPanel.this,
                            "Error creating customer: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void searchCustomers() {
        SwingWorker<List<CustomerDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CustomerDTO> doInBackground() throws Exception {
                String firstName = searchFirstNameField.getText().trim();
                String lastName  = searchLastNameField.getText().trim();

                firstName = firstName.isEmpty() ? null : firstName;
                lastName  = lastName.isEmpty() ? null : lastName;

                return service.findCustomersByName(firstName, lastName);
            }

            @Override
            protected void done() {
                try {
                    List<CustomerDTO> customers = get();
                    tableModel.setRowCount(0);

                    for (CustomerDTO customer : customers) {
                        tableModel.addRow(new Object[]{
                                customer.getCustomerId(),
                                customer.getCustomerNumber(),
                                customer.getFirstName(),
                                customer.getLastName(),
                                customer.getAddress(),
                                customer.getUsername()
                        });
                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CustomerManagementPanel.this,
                            "Error searching customers: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}