package net.froihofer.dsfinance.bank.client.gui;

import net.froihofer.dsfinance.bank.api.EmployeeBankService;
import net.froihofer.dsfinance.bank.dto.CustomerDTO;
import net.froihofer.dsfinance.bank.dto.PortfolioDTO;
import net.froihofer.dsfinance.bank.dto.PortfolioPositionDTO;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;
import net.froihofer.util.AuthCallbackHandler;
import net.froihofer.util.WildflyJndiLookupHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

/**
 * Swing GUI for Bank Employees
 * Allows creating customers, managing trades, viewing portfolios
 */
public class EmployeeClientGUI extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(EmployeeClientGUI.class);

    private EmployeeBankService employeeService;
    private JTabbedPane tabbedPane;

    // Customer Management Tab
    private JTextField txtFirstName, txtLastName, txtAddress;
    private JTextField txtLoginUsername;
    private JPasswordField txtInitialPassword;
    private JTextField txtSearchFirstName, txtSearchLastName, txtSearchById;
    private JTable customerTable;
    private DefaultTableModel customerTableModel;

    // Trading Tab
    private JTextField txtTradeCustomerId, txtTradeSymbol, txtTradeQuantity;
    private JTextArea txtTradeResult;

    // Portfolio Tab
    private JTextField txtPortfolioCustomerId;
    private JTable portfolioTable;
    private DefaultTableModel portfolioTableModel;
    private JLabel lblTotalPortfolioValue;

    // Stock Search Tab
    private JTextField txtStockSearch;
    private JTable stockTable;
    private DefaultTableModel stockTableModel;
    private JTextField txtStockSearchSymbol;


    // Bank Volume Tab
    private JLabel lblInvestableVolume;

    public EmployeeClientGUI(EmployeeBankService service) {
        this.employeeService = service;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("DS Finance Bank - Employee Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        // Add all tabs
        tabbedPane.addTab("Customer Management", createCustomerManagementPanel());
        tabbedPane.addTab("Stock Trading", createTradingPanel());
        tabbedPane.addTab("Portfolio View", createPortfolioPanel());
        tabbedPane.addTab("Stock Search", createStockSearchPanel());
        tabbedPane.addTab("Bank Volume", createBankVolumePanel());

        add(tabbedPane);
    }

    // ==================== CUSTOMER MANAGEMENT TAB ====================
    private JPanel createCustomerManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: Create Customer Form
        JPanel createPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        createPanel.setBorder(BorderFactory.createTitledBorder("Create New Customer"));

        createPanel.add(new JLabel("First Name:"));
        txtFirstName = new JTextField();
        createPanel.add(txtFirstName);

        createPanel.add(new JLabel("Last Name:"));
        txtLastName = new JTextField();
        createPanel.add(txtLastName);

        createPanel.add(new JLabel("Address:"));
        txtAddress = new JTextField();
        createPanel.add(txtAddress);

        createPanel.add(new JLabel("Login Username (WildFly):"));
        txtLoginUsername = new JTextField();
        createPanel.add(txtLoginUsername);

        createPanel.add(new JLabel("Initial Password (WildFly):"));
        txtInitialPassword = new JPasswordField();
        createPanel.add(txtInitialPassword);

        createPanel.add(new JLabel(""));
        JButton btnCreate = new JButton("Create Customer");
        btnCreate.addActionListener(e -> createCustomer());
        createPanel.add(btnCreate);

        panel.add(createPanel, BorderLayout.NORTH);

        // Middle: Search Form
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Customers"));

        searchPanel.add(new JLabel("First Name:"));
        txtSearchFirstName = new JTextField(10);
        searchPanel.add(txtSearchFirstName);

        searchPanel.add(new JLabel("Last Name:"));
        txtSearchLastName = new JTextField(10);
        searchPanel.add(txtSearchLastName);

        JButton btnSearchByName = new JButton("Search by Name");
        btnSearchByName.addActionListener(e -> searchCustomersByName());
        searchPanel.add(btnSearchByName);

        searchPanel.add(Box.createHorizontalStrut(20));

        searchPanel.add(new JLabel("Customer ID:"));
        txtSearchById = new JTextField(10);
        searchPanel.add(txtSearchById);

        JButton btnSearchById = new JButton("Search by ID");
        btnSearchById.addActionListener(e -> searchCustomerById());
        searchPanel.add(btnSearchById);

        // NEW: Search by Customer Number
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(new JLabel("Customer #:"));
        JTextField txtSearchByNumber = new JTextField(15);
        searchPanel.add(txtSearchByNumber);

        JButton btnSearchByNumber = new JButton("Search by #");
        btnSearchByNumber.addActionListener(e -> searchCustomerByNumber(txtSearchByNumber.getText()));
        searchPanel.add(btnSearchByNumber);

        panel.add(searchPanel, BorderLayout.CENTER);

        // Bottom: Results Table
        String[] columns = {"ID", "Customer Number", "First Name", "Last Name", "Address", "Username"};
        customerTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customerTable = new JTable(customerTableModel);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Customer List"));
        panel.add(scrollPane, BorderLayout.SOUTH);

        return panel;
    }

    private void createCustomer() {
        SwingWorker<Long, Void> worker = new SwingWorker<>() {
            @Override
            protected Long doInBackground() throws Exception {
                String firstName = txtFirstName.getText().trim();
                String lastName = txtLastName.getText().trim();
                String address = txtAddress.getText().trim();
                String loginUser = txtLoginUsername.getText().trim();
                String loginPass = new String(txtInitialPassword.getPassword());

                // Validation
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

                // CRITICAL: Generate customerNumber (required by EJB)
                String customerNumber = "CUST-" + System.currentTimeMillis() + "-" + loginUser;
                customer.setCustomerNumber(customerNumber);

                // Set username and password for WildFly authentication
                customer.setUsername(loginUser);
                customer.setInitialPassword(loginPass);

                return employeeService.createCustomer(customer);
            }

            @Override
            protected void done() {
                try {
                    Long id = get();
                    String username = txtLoginUsername.getText().trim();
                    
                    JOptionPane.showMessageDialog(EmployeeClientGUI.this,
                            "Customer created successfully!\nID: " + id + "\n\n" +
                            "Customer can now log in to Customer GUI with:\n" +
                            "Username: " + username + "\n" +
                            "Password: (the one you entered)",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Clear fields
                    txtFirstName.setText("");
                    txtLastName.setText("");
                    txtAddress.setText("");
                    txtLoginUsername.setText("");
                    txtInitialPassword.setText("");

                    // Refresh customer list
                    searchCustomersByName();

                } catch (Exception e) {
                    log.error("Failed to create customer", e);
                    String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(EmployeeClientGUI.this,
                            "Error creating customer: " + errorMsg,
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void searchCustomersByName() {
        SwingWorker<List<CustomerDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CustomerDTO> doInBackground() throws Exception {
                String firstName = txtSearchFirstName.getText().trim();
                String lastName = txtSearchLastName.getText().trim();

                if (firstName.isEmpty()) firstName = null;
                if (lastName.isEmpty()) lastName = null;

                return employeeService.findCustomersByName(firstName, lastName);
            }

            @Override
            protected void done() {
                try {
                    List<CustomerDTO> customers = get();
                    displayCustomers(customers);
                } catch (Exception e) {
                    log.error("Failed to search customers", e);
                    JOptionPane.showMessageDialog(EmployeeClientGUI.this,
                            "Error: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void searchCustomerById() {
        SwingWorker<CustomerDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected CustomerDTO doInBackground() throws Exception {
                long id = Long.parseLong(txtSearchById.getText().trim());
                return employeeService.findCustomerById(id);
            }

            @Override
            protected void done() {
                try {
                    CustomerDTO customer = get();
                    if (customer != null) {
                        customerTableModel.setRowCount(0);
                        customerTableModel.addRow(new Object[]{
                                customer.getCustomerId(),
                                customer.getCustomerNumber(),
                                customer.getFirstName(),
                                customer.getLastName(),
                                customer.getAddress(),
                                customer.getUsername()
                        });
                    } else {
                        JOptionPane.showMessageDialog(EmployeeClientGUI.this,
                                "Customer not found",
                                "Not Found", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    log.error("Failed to find customer", e);
                    JOptionPane.showMessageDialog(EmployeeClientGUI.this,
                            "Error: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * Search customer by customer number (e.g., "CUST-12345")
     */
    private void searchCustomerByNumber(String customerNumber) {
        SwingWorker<CustomerDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected CustomerDTO doInBackground() throws Exception {
                String number = customerNumber.trim();
                if (number.isEmpty()) {
                    throw new IllegalArgumentException("Customer number must not be empty");
                }
                return employeeService.findCustomerByCustomerNumber(number);
            }

            @Override
            protected void done() {
                try {
                    CustomerDTO customer = get();
                    if (customer != null) {
                        customerTableModel.setRowCount(0);
                        customerTableModel.addRow(new Object[]{
                                customer.getCustomerId(),
                                customer.getCustomerNumber(),
                                customer.getFirstName(),
                                customer.getLastName(),
                                customer.getAddress(),
                                customer.getUsername()
                        });
                    } else {
                        JOptionPane.showMessageDialog(EmployeeClientGUI.this,
                                "Customer not found with number: " + customerNumber,
                                "Not Found", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    log.error("Failed to find customer by number", e);
                    JOptionPane.showMessageDialog(EmployeeClientGUI.this,
                            "Error: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void displayCustomers(List<CustomerDTO> customers) {
        customerTableModel.setRowCount(0);
        for (CustomerDTO customer : customers) {
            customerTableModel.addRow(new Object[]{
                    customer.getCustomerId(),
                    customer.getCustomerNumber(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getAddress(),
                    customer.getUsername()
            });
        }
    }

    // ==================== TRADING TAB ====================
    private JPanel createTradingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Trading for Customer"));

        formPanel.add(new JLabel("Customer ID:"));
        txtTradeCustomerId = new JTextField();
        formPanel.add(txtTradeCustomerId);

        formPanel.add(new JLabel("Stock Symbol:"));
        txtTradeSymbol = new JTextField();
        formPanel.add(txtTradeSymbol);

        formPanel.add(new JLabel("Quantity:"));
        txtTradeQuantity = new JTextField();
        formPanel.add(txtTradeQuantity);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnBuy = new JButton("Buy Stock");
        btnBuy.addActionListener(e -> buyStock());
        buttonPanel.add(btnBuy);

        JButton btnSell = new JButton("Sell Stock");
        btnSell.addActionListener(e -> sellStock());
        buttonPanel.add(btnSell);

        formPanel.add(buttonPanel);

        panel.add(formPanel, BorderLayout.NORTH);

        txtTradeResult = new JTextArea(15, 50);
        txtTradeResult.setEditable(false);
        txtTradeResult.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(txtTradeResult);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Transaction Results"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void buyStock() {
        SwingWorker<BigDecimal, Void> worker = new SwingWorker<>() {
            @Override
            protected BigDecimal doInBackground() throws Exception {
                long customerId = Long.parseLong(txtTradeCustomerId.getText().trim());
                String symbol = txtTradeSymbol.getText().trim().toUpperCase();
                int quantity = Integer.parseInt(txtTradeQuantity.getText().trim());

                return employeeService.buyStockForCustomer(customerId, symbol, quantity);
            }

            @Override
            protected void done() {
                try {
                    BigDecimal price = get();
                    String result = String.format("✓ BUY SUCCESS\n" +
                                    "Customer: %s\n" +
                                    "Symbol: %s\n" +
                                    "Quantity: %s\n" +
                                    "Price per share: $%.2f\n" +
                                    "Total: $%.2f\n" +
                                    "Time: %tc\n\n",
                            txtTradeCustomerId.getText(),
                            txtTradeSymbol.getText().toUpperCase(),
                            txtTradeQuantity.getText(),
                            price,
                            price.multiply(new BigDecimal(txtTradeQuantity.getText())),
                            System.currentTimeMillis());

                    txtTradeResult.append(result);
                    txtTradeResult.setCaretPosition(txtTradeResult.getDocument().getLength());

                } catch (Exception e) {
                    log.error("Buy failed", e);
                    txtTradeResult.append("✗ BUY FAILED: " + e.getCause().getMessage() + "\n\n");
                }
            }
        };
        worker.execute();
    }

    private void sellStock() {
        SwingWorker<BigDecimal, Void> worker = new SwingWorker<>() {
            @Override
            protected BigDecimal doInBackground() throws Exception {
                long customerId = Long.parseLong(txtTradeCustomerId.getText().trim());
                String symbol = txtTradeSymbol.getText().trim().toUpperCase();
                int quantity = Integer.parseInt(txtTradeQuantity.getText().trim());

                return employeeService.sellStockForCustomer(customerId, symbol, quantity);
            }

            @Override
            protected void done() {
                try {
                    BigDecimal price = get();
                    String result = String.format("✓ SELL SUCCESS\n" +
                                    "Customer: %s\n" +
                                    "Symbol: %s\n" +
                                    "Quantity: %s\n" +
                                    "Price per share: $%.2f\n" +
                                    "Total: $%.2f\n" +
                                    "Time: %tc\n\n",
                            txtTradeCustomerId.getText(),
                            txtTradeSymbol.getText().toUpperCase(),
                            txtTradeQuantity.getText(),
                            price,
                            price.multiply(new BigDecimal(txtTradeQuantity.getText())),
                            System.currentTimeMillis());

                    txtTradeResult.append(result);
                    txtTradeResult.setCaretPosition(txtTradeResult.getDocument().getLength());

                } catch (Exception e) {
                    log.error("Sell failed", e);
                    txtTradeResult.append("✗ SELL FAILED: " + e.getCause().getMessage() + "\n\n");
                }
            }
        };
        worker.execute();
    }

    // ==================== PORTFOLIO TAB ====================
    private JPanel createPortfolioPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Customer ID:"));
        txtPortfolioCustomerId = new JTextField(15);
        topPanel.add(txtPortfolioCustomerId);

        JButton btnLoadPortfolio = new JButton("Load Portfolio");
        btnLoadPortfolio.addActionListener(e -> loadPortfolio());
        topPanel.add(btnLoadPortfolio);

        lblTotalPortfolioValue = new JLabel("Total Value: $0.00");
        lblTotalPortfolioValue.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        lblTotalPortfolioValue.setForeground(new Color(0, 128, 0));
        topPanel.add(Box.createHorizontalStrut(30));
        topPanel.add(lblTotalPortfolioValue);

        panel.add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Symbol", "Quantity", "Avg Purchase Price", "Current Price", "Total Value", "Profit/Loss"};
        portfolioTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        portfolioTable = new JTable(portfolioTableModel);

        JScrollPane scrollPane = new JScrollPane(portfolioTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Holdings"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadPortfolio() {
        SwingWorker<PortfolioDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected PortfolioDTO doInBackground() throws Exception {
                long customerId = Long.parseLong(txtPortfolioCustomerId.getText().trim());
                return employeeService.getCustomerPortfolio(customerId);
            }

            @Override
            protected void done() {
                try {
                    PortfolioDTO portfolio = get();
                    displayPortfolio(portfolio);
                } catch (Exception e) {
                    log.error("Failed to load portfolio", e);
                    JOptionPane.showMessageDialog(EmployeeClientGUI.this,
                            "Error: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void displayPortfolio(PortfolioDTO portfolio) {
        portfolioTableModel.setRowCount(0);

        if (portfolio != null && portfolio.getPositions() != null) {
            for (PortfolioPositionDTO position : portfolio.getPositions()) {
                BigDecimal profitLoss = position.getCurrentPrice()
                        .subtract(position.getAveragePurchasePrice())
                        .multiply(new BigDecimal(position.getQuantity()));

                portfolioTableModel.addRow(new Object[]{
                        position.getSymbol(),
                        position.getQuantity(),
                        String.format("$%.2f", position.getAveragePurchasePrice()),
                        String.format("$%.2f", position.getCurrentPrice()),
                        String.format("$%.2f", position.getTotalValue()),
                        String.format("$%.2f", profitLoss)
                });
            }

            lblTotalPortfolioValue.setText(String.format("Total Value: $%.2f", portfolio.getTotalValue()));
        } else {
            lblTotalPortfolioValue.setText("Total Value: $0.00");
        }
    }


    // ==================== STOCK SEARCH TAB ====================
    private JPanel createStockSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Search by Company Name (existing)
        searchPanel.add(new JLabel("Company Name:"));
        txtStockSearch = new JTextField(20);
        searchPanel.add(txtStockSearch);

        JButton btnSearchByName = new JButton("Search Name");
        btnSearchByName.addActionListener(e -> searchStocksByCompanyName());
        searchPanel.add(btnSearchByName);

        searchPanel.add(Box.createHorizontalStrut(20));

        // Search by Symbol (new)
        searchPanel.add(new JLabel("Symbol:"));
        JTextField txtStockSymbol = new JTextField(10);
        searchPanel.add(txtStockSymbol);

        JButton btnSearchSymbol = new JButton("Search Symbol");
        btnSearchSymbol.addActionListener(e -> searchStockBySymbol(txtStockSymbol.getText()));
        searchPanel.add(btnSearchSymbol);


        panel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"Symbol", "Company Name", "Last Price", "Change"};
        stockTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        stockTable = new JTable(stockTableModel);

        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Search Results"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void searchStocksByCompanyName() {
        SwingWorker<List<StockQuoteDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<StockQuoteDTO> doInBackground() throws Exception {
                String query = txtStockSearch.getText().trim();
                return employeeService.findStockQuotesByCompanyName(query);
            }

            @Override
            protected void done() {
                try {
                    List<StockQuoteDTO> quotes = get();
                    displayStocks(quotes);
                } catch (Exception e) {
                    log.error("Failed to search stocks by company name", e);
                    JOptionPane.showMessageDialog(EmployeeClientGUI.this,
                            "Error: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void searchStockBySymbol(String symbolInput) {
        SwingWorker<StockQuoteDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected StockQuoteDTO doInBackground() throws Exception {
                String sym = symbolInput.trim().toUpperCase();
                if (sym.isEmpty()) throw new IllegalArgumentException("Symbol must not be empty");
                return employeeService.findStockQuoteBySymbol(sym);
            }

            @Override
            protected void done() {
                try {
                    StockQuoteDTO q = get();
                    // show single result in the same table
                    stockTableModel.setRowCount(0);
                    stockTableModel.addRow(new Object[]{
                            q.getSymbol(),
                            q.getCompanyName(),
                            String.format("$%.2f", q.getLastTradePrice()),
                            (q.getChange() != null) ? String.format("$%.2f", q.getChange()) : "N/A"
                    });
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(EmployeeClientGUI.this,
                            "Error: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }


    private void displayStocks(List<StockQuoteDTO> quotes) {
        stockTableModel.setRowCount(0);
        for (StockQuoteDTO quote : quotes) {
            String changeStr = (quote.getChange() != null) ?
                    String.format("$%.2f", quote.getChange()) : "N/A";

            stockTableModel.addRow(new Object[]{
                    quote.getSymbol(),
                    quote.getCompanyName(),
                    String.format("$%.2f", quote.getLastTradePrice()),
                    changeStr
            });
        }
    }


    // ==================== BANK VOLUME TAB ====================
    private JPanel createBankVolumePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Bank Investable Volume");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(titleLabel);

        centerPanel.add(Box.createVerticalStrut(30));

        lblInvestableVolume = new JLabel("$0.00");
        lblInvestableVolume.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        lblInvestableVolume.setForeground(new Color(0, 100, 0));
        lblInvestableVolume.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblInvestableVolume);

        centerPanel.add(Box.createVerticalStrut(30));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRefresh.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        btnRefresh.addActionListener(e -> loadBankVolume());
        centerPanel.add(btnRefresh);

        panel.add(centerPanel, BorderLayout.CENTER);

        // Load initially
        loadBankVolume();

        return panel;
    }

    private void loadBankVolume() {
        SwingWorker<BigDecimal, Void> worker = new SwingWorker<>() {
            @Override
            protected BigDecimal doInBackground() throws Exception {
                return employeeService.getInvestableVolume();
            }

            @Override
            protected void done() {
                try {
                    BigDecimal volume = get();
                    lblInvestableVolume.setText(String.format("$%,.2f", volume));
                } catch (Exception e) {
                    log.error("Failed to load bank volume", e);
                    lblInvestableVolume.setText("Error loading");
                }
            }
        };
        worker.execute();
    }

    // ==================== MAIN ====================
    public static void main(String[] args) {
        try {
            // Set Look and Feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Get credentials
            String username = JOptionPane.showInputDialog("Enter username (employee):");
            String password = JOptionPane.showInputDialog("Enter password:");

            if (username == null || password == null) {
                System.exit(0);
            }

            // Setup JNDI
            AuthCallbackHandler.setUsername(username);
            AuthCallbackHandler.setPassword(password);

            Properties props = new Properties();
            props.put(Context.SECURITY_PRINCIPAL, username);
            props.put(Context.SECURITY_CREDENTIALS, password);

            WildflyJndiLookupHelper jndiHelper = new WildflyJndiLookupHelper(
                    new InitialContext(props),
                    "ds-finance-bank-ear",
                    "ds-finance-bank-ejb",
                    ""
            );

            EmployeeBankService service = jndiHelper.lookupUsingJBossEjbClient(
                    "EmployeeBankServiceBean",
                    EmployeeBankService.class,
                    true
            );

            // Launch GUI
            SwingUtilities.invokeLater(() -> {
                EmployeeClientGUI gui = new EmployeeClientGUI(service);
                gui.setVisible(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to connect: " + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}