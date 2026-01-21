package net.froihofer.dsfinance.bank.client.gui;

import net.froihofer.dsfinance.bank.api.CustomerBankService;
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
import java.math.RoundingMode;
import java.util.List;
import java.util.Properties;

/**
 * Swing GUI for Bank Customers
 * Allows searching stocks, buying/selling, viewing own portfolio
 */
public class CustomerClientGUI extends JFrame {
    private static final Logger log = LoggerFactory.getLogger(CustomerClientGUI.class);

    private CustomerBankService customerService;
    private String username;
    private JTabbedPane tabbedPane;

    // Portfolio Tab
    private JTable portfolioTable;
    private DefaultTableModel portfolioTableModel;
    private JLabel lblTotalValue;

    // Stock Search Tab
    private JTextField txtStockSearch;
    private JTable stockTable;
    private DefaultTableModel stockTableModel;

    // Trading Tab
    private JTextField txtTradeSymbol, txtTradeQuantity;
    private JTextArea txtTradeResult;

    public CustomerClientGUI(CustomerBankService service, String username) {
        this.customerService = service;
        this.username = username;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("DS Finance Bank - Customer Client (" + username + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("My Portfolio", createPortfolioPanel());
        tabbedPane.addTab("Trade Stocks", createTradingPanel());
        tabbedPane.addTab("Search Stocks", createStockSearchPanel());

        add(tabbedPane);

        // Load portfolio on startup
        SwingUtilities.invokeLater(this::loadMyPortfolio);
    }

    // ==================== PORTFOLIO TAB ====================
    private JPanel createPortfolioPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        lblTotalValue = new JLabel("Total Portfolio Value: $0.00");
        lblTotalValue.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        lblTotalValue.setForeground(new Color(0, 128, 0));
        topPanel.add(lblTotalValue);

        topPanel.add(Box.createHorizontalStrut(20));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadMyPortfolio());
        topPanel.add(btnRefresh);

        panel.add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Symbol", "Quantity", "Avg Price", "Current Price", "Total Value", "Profit/Loss %"};
        portfolioTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        portfolioTable = new JTable(portfolioTableModel);
        portfolioTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(portfolioTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("My Holdings"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadMyPortfolio() {
        SwingWorker<PortfolioDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected PortfolioDTO doInBackground() throws Exception {
                return customerService.getMyPortfolio();
            }

            @Override
            protected void done() {
                try {
                    PortfolioDTO portfolio = get();
                    displayPortfolio(portfolio);
                } catch (Exception e) {
                    log.error("Failed to load portfolio", e);
                    JOptionPane.showMessageDialog(CustomerClientGUI.this,
                            "Error loading portfolio: " + e.getMessage(),
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
                BigDecimal profitLossPercent = position.getCurrentPrice()
                        .subtract(position.getAveragePurchasePrice())
                        .divide(position.getAveragePurchasePrice(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));

                portfolioTableModel.addRow(new Object[]{
                        position.getSymbol(),
                        position.getQuantity(),
                        String.format("$%.2f", position.getAveragePurchasePrice()),
                        String.format("$%.2f", position.getCurrentPrice()),
                        String.format("$%.2f", position.getTotalValue()),
                        String.format("%.2f%%", profitLossPercent)
                });
            }

            lblTotalValue.setText(String.format("Total Portfolio Value: $%,.2f", portfolio.getTotalValue()));
        } else {
            lblTotalValue.setText("Total Portfolio Value: $0.00");
        }
    }

    // ==================== TRADING TAB ====================
    private JPanel createTradingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Trade Stocks"));

        formPanel.add(new JLabel("Stock Symbol:"));
        txtTradeSymbol = new JTextField();
        formPanel.add(txtTradeSymbol);

        formPanel.add(new JLabel("Quantity:"));
        txtTradeQuantity = new JTextField();
        formPanel.add(txtTradeQuantity);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnBuy = new JButton("BUY");
        btnBuy.setBackground(new Color(0, 150, 0));
        btnBuy.setForeground(Color.WHITE);
        btnBuy.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btnBuy.addActionListener(e -> buyStock());
        buttonPanel.add(btnBuy);

        JButton btnSell = new JButton("SELL");
        btnSell.setBackground(new Color(200, 0, 0));
        btnSell.setForeground(Color.WHITE);
        btnSell.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btnSell.addActionListener(e -> sellStock());
        buttonPanel.add(btnSell);

        formPanel.add(buttonPanel);

        panel.add(formPanel, BorderLayout.NORTH);

        txtTradeResult = new JTextArea(20, 50);
        txtTradeResult.setEditable(false);
        txtTradeResult.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(txtTradeResult);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Transaction History"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void buyStock() {
        SwingWorker<BigDecimal, Void> worker = new SwingWorker<>() {
            @Override
            protected BigDecimal doInBackground() throws Exception {
                String symbol = txtTradeSymbol.getText().trim().toUpperCase();
                int quantity = Integer.parseInt(txtTradeQuantity.getText().trim());

                if (quantity <= 0) {
                    throw new IllegalArgumentException("Quantity must be positive");
                }

                return customerService.buyStock(symbol, quantity);
            }

            @Override
            protected void done() {
                try {
                    BigDecimal price = get();
                    int quantity = Integer.parseInt(txtTradeQuantity.getText().trim());
                    BigDecimal total = price.multiply(new BigDecimal(quantity));

                    String result = String.format(
                            "═══════════════════════════════════════\n" +
                                    "  ✓ BUY ORDER EXECUTED\n" +
                                    "═══════════════════════════════════════\n" +
                                    "Symbol:        %s\n" +
                                    "Quantity:      %d shares\n" +
                                    "Price/Share:   $%.2f\n" +
                                    "Total Cost:    $%.2f\n" +
                                    "Time:          %tc\n" +
                                    "═══════════════════════════════════════\n\n",
                            txtTradeSymbol.getText().toUpperCase(),
                            quantity,
                            price,
                            total,
                            System.currentTimeMillis()
                    );

                    txtTradeResult.append(result);
                    txtTradeResult.setCaretPosition(txtTradeResult.getDocument().getLength());

                    // Refresh portfolio
                    loadMyPortfolio();

                    JOptionPane.showMessageDialog(CustomerClientGUI.this,
                            String.format("Successfully bought %d shares of %s at $%.2f per share",
                                    quantity, txtTradeSymbol.getText().toUpperCase(), price),
                            "Trade Successful", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception e) {
                    log.error("Buy failed", e);
                    String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    txtTradeResult.append("✗ BUY FAILED: " + errorMsg + "\n\n");
                    JOptionPane.showMessageDialog(CustomerClientGUI.this,
                            "Failed to buy: " + errorMsg,
                            "Trade Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void sellStock() {
        SwingWorker<BigDecimal, Void> worker = new SwingWorker<>() {
            @Override
            protected BigDecimal doInBackground() throws Exception {
                String symbol = txtTradeSymbol.getText().trim().toUpperCase();
                int quantity = Integer.parseInt(txtTradeQuantity.getText().trim());

                if (quantity <= 0) {
                    throw new IllegalArgumentException("Quantity must be positive");
                }

                return customerService.sellStock(symbol, quantity);
            }

            @Override
            protected void done() {
                try {
                    BigDecimal price = get();
                    int quantity = Integer.parseInt(txtTradeQuantity.getText().trim());
                    BigDecimal total = price.multiply(new BigDecimal(quantity));

                    String result = String.format(
                            "═══════════════════════════════════════\n" +
                                    "  ✓ SELL ORDER EXECUTED\n" +
                                    "═══════════════════════════════════════\n" +
                                    "Symbol:        %s\n" +
                                    "Quantity:      %d shares\n" +
                                    "Price/Share:   $%.2f\n" +
                                    "Total Proceeds: $%.2f\n" +
                                    "Time:          %tc\n" +
                                    "═══════════════════════════════════════\n\n",
                            txtTradeSymbol.getText().toUpperCase(),
                            quantity,
                            price,
                            total,
                            System.currentTimeMillis()
                    );

                    txtTradeResult.append(result);
                    txtTradeResult.setCaretPosition(txtTradeResult.getDocument().getLength());

                    // Refresh portfolio
                    loadMyPortfolio();

                    JOptionPane.showMessageDialog(CustomerClientGUI.this,
                            String.format("Successfully sold %d shares of %s at $%.2f per share",
                                    quantity, txtTradeSymbol.getText().toUpperCase(), price),
                            "Trade Successful", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception e) {
                    log.error("Sell failed", e);
                    String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    txtTradeResult.append("✗ SELL FAILED: " + errorMsg + "\n\n");
                    JOptionPane.showMessageDialog(CustomerClientGUI.this,
                            "Failed to sell: " + errorMsg,
                            "Trade Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // ==================== STOCK SEARCH TAB ====================
    private JPanel createStockSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Available Stocks"));
        searchPanel.add(new JLabel("Company Name:"));
        txtStockSearch = new JTextField(25);
        searchPanel.add(txtStockSearch);

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> searchStocks());
        searchPanel.add(btnSearch);

        panel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"Symbol", "Company Name", "Last Trade Price", "Change"};
        stockTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        stockTable = new JTable(stockTableModel);
        stockTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        // Double-click to copy symbol to trading tab
        stockTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = stockTable.getSelectedRow();
                    if (row >= 0) {
                        String symbol = (String) stockTable.getValueAt(row, 0);
                        txtTradeSymbol.setText(symbol);
                        tabbedPane.setSelectedIndex(1); // Switch to trading tab
                        JOptionPane.showMessageDialog(CustomerClientGUI.this,
                                "Symbol " + symbol + " copied to trading tab",
                                "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Search Results (Double-click to trade)"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void searchStocks() {
        SwingWorker<List<StockQuoteDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<StockQuoteDTO> doInBackground() throws Exception {
                String query = txtStockSearch.getText().trim();
                if (query.isEmpty()) {
                    throw new IllegalArgumentException("Please enter a company name");
                }
                return customerService.findStockQuotesByCompanyName(query);
            }

            @Override
            protected void done() {
                try {
                    List<StockQuoteDTO> quotes = get();
                    displayStocks(quotes);

                    if (quotes.isEmpty()) {
                        JOptionPane.showMessageDialog(CustomerClientGUI.this,
                                "No stocks found matching '" + txtStockSearch.getText() + "'",
                                "No Results", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    log.error("Failed to search stocks", e);
                    JOptionPane.showMessageDialog(CustomerClientGUI.this,
                            "Search failed: " + e.getMessage(),
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

    // ==================== MAIN ====================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            String username = JOptionPane.showInputDialog("Enter username (customer):");
            String password = JOptionPane.showInputDialog("Enter password:");

            if (username == null || password == null) {
                System.exit(0);
            }

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

            CustomerBankService service = jndiHelper.lookupUsingJBossEjbClient(
                    "CustomerBankServiceBean",
                    CustomerBankService.class,
                    true
            );

            // Force authz check early (so wrong role/password fails before GUI opens)
            service.whoAmI();

            SwingUtilities.invokeLater(() -> {
                CustomerClientGUI gui = new CustomerClientGUI(service, username);
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
