package net.froihofer.dsfinance.bank.client;


import net.froihofer.dsfinance.bank.api.EmployeeBankService;
import net.froihofer.dsfinance.bank.dto.CustomerDTO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomerManagementPanel extends JPanel {

    private EmployeeBankService service;
    private JTextField firstNameField, lastNameField, addressField;
    private JTextField searchFirstNameField, searchLastNameField;
    private JTable customerTable;
    private DefaultTableModel tableModel;

    public CustomerManagementPanel(EmployeeBankService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create two sections: Create and Search
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        topPanel.add(createCreateCustomerPanel());
        topPanel.add(createSearchCustomerPanel());

        add(topPanel, BorderLayout.NORTH);
        add(createCustomerTablePanel(), BorderLayout.CENTER);
    }

    private JPanel createCreateCustomerPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
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

        JButton createButton = new JButton("Create Customer");
        createButton.addActionListener(e -> createCustomer());
        panel.add(new JLabel());
        panel.add(createButton);

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

        // Create table
        String[] columns = {"ID", "Customer Number", "First Name", "Last Name", "Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        customerTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(customerTable);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void createCustomer() {
        // This MUST run in a background thread!
        SwingWorker<Long, Void> worker = new SwingWorker<>() {
            @Override
            protected Long doInBackground() throws Exception {
                // Validate input
                String firstName = firstNameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                String address = addressField.getText().trim();

                if (firstName.isEmpty() || lastName.isEmpty()) {
                    throw new IllegalArgumentException("First and Last name are required");
                }

                // Create DTO
                CustomerDTO customer = new CustomerDTO();
                customer.setFirstName(firstName);
                customer.setLastName(lastName);
                customer.setAddress(address);
                // Customer number is auto-generated, so don't set it

                // Call EJB service
                return service.createCustomer(customer);
            }

            @Override
            protected void done() {
                try {
                    Long customerId = get();
                    JOptionPane.showMessageDialog(CustomerManagementPanel.this,
                            "Customer created successfully! ID: " + customerId,
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Clear fields
                    firstNameField.setText("");
                    lastNameField.setText("");
                    addressField.setText("");

                    // Refresh search
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
                String lastName = searchLastNameField.getText().trim();

                // Convert empty strings to null for the search
                firstName = firstName.isEmpty() ? null : firstName;
                lastName = lastName.isEmpty() ? null : lastName;

                return service.findCustomersByName(firstName, lastName);
            }

            @Override
            protected void done() {
                try {
                    List<CustomerDTO> customers = get();

                    // Clear existing rows
                    tableModel.setRowCount(0);

                    // Add customers to table
                    for (CustomerDTO customer : customers) {
                        tableModel.addRow(new Object[]{
                                customer.getCustomerId(),
                                customer.getCustomerNumber(),
                                customer.getFirstName(),
                                customer.getLastName(),
                                customer.getAddress()
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
