package net.froihofer.dsfinance.bank.client;

import java.util.List;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.*;

import net.froihofer.dsfinance.bank.api.CustomerBankService;
import net.froihofer.dsfinance.bank.api.EmployeeBankService;
import net.froihofer.dsfinance.bank.client.gui.CustomerClientGUI;
import net.froihofer.dsfinance.bank.client.gui.EmployeeClientGUI;
import net.froihofer.dsfinance.bank.dto.CustomerDTO;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;
import net.froihofer.util.AuthCallbackHandler;
import net.froihofer.util.WildflyJndiLookupHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for DS Finance Bank Client Application.
 * Provides a GUI launcher dialog and also contains the CLI logic for Part 2 demo.
 */
public class BankClient {
    private static final Logger log = LoggerFactory.getLogger(BankClient.class);

    private WildflyJndiLookupHelper createJndiHelper(String username, String password) throws NamingException {
        AuthCallbackHandler.setUsername(username);
        AuthCallbackHandler.setPassword(password);

        Properties props = new Properties();
        props.put(Context.SECURITY_PRINCIPAL, AuthCallbackHandler.getUsername());
        props.put(Context.SECURITY_CREDENTIALS, AuthCallbackHandler.getPassword());

        return new WildflyJndiLookupHelper(new InitialContext(props), "ds-finance-bank-ear", "ds-finance-bank-ejb", "");
    }

    /**
     * Runs the Part 2 CLI demo:
     *  - performs a remote EJB call (client -> server)
     *  - triggers a TradingService WebService call (server-side adapter)
     *  - persists a JPA entity (createCustomer)
     */
    private void runPart2Demo() {
        try {
            // 1) Client -> Server remote call (Employee)
            WildflyJndiLookupHelper employeeJndi = createJndiHelper("employee", "employeepass");
            EmployeeBankService employeeService =
                    employeeJndi.lookupUsingJBossEjbClient("EmployeeBankServiceBean", EmployeeBankService.class, true);

            log.info("Remote EJB lookup successful: {}", employeeService.getClass());

            // 3) Persist a JPA Entity (Customer)
            CustomerDTO newCustomer = new CustomerDTO(null, "C-10001", "Ada", "Lovelace", "Example Street 1, 1010 Vienna");

            // IMPORTANT: must match the WildFly login principal used by the Customer GUI
            newCustomer.setUsername("customer");

            // Optional: if server-side createCustomer() creates WildFly users when password is provided
            newCustomer.setInitialPassword("customerpass");

            long customerId = employeeService.createCustomer(newCustomer);
            log.info("Created customer with DB id={}", customerId);

            CustomerDTO loaded = employeeService.findCustomerById(customerId);
            log.info("Loaded customer: {}", loaded);

            // 2) Web Service operation call via TradingServiceAdapter (server-side)
            List<StockQuoteDTO> quotes = employeeService.findStockQuotesByCompanyName("Apple");
            log.info("TradingService returned {} quote(s)", quotes.size());
            for (int i = 0; i < Math.min(quotes.size(), 5); i++) {
                log.info("Quote {}: {}", i + 1, quotes.get(i));
            }

            // Optional: Customer identity demo (requires a customer user configured in WildFly)
            WildflyJndiLookupHelper customerJndi = createJndiHelper("customer", "customerpass");
            CustomerBankService customerService =
                    customerJndi.lookupUsingJBossEjbClient("CustomerBankServiceBean", CustomerBankService.class, true);
            log.info("whoAmI() = {}", customerService.whoAmI());

        } catch (Exception e) {
            log.error("Client failed", e);
        }
    }

    public static void main(String[] args) {
        // Set system look and feel for better UI appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Show launcher dialog
        String[] options = {
                "Employee GUI",
                "Customer GUI",
                "Part 2 CLI Demo",
                "Exit"
        };

        int choice = JOptionPane.showOptionDialog(
                null,
                "Which client do you want to start?",
                "DS Finance Bank - Client Launcher",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            // Launch Employee GUI
            EmployeeClientGUI.main(new String[0]);
        } else if (choice == 1) {
            // Launch Customer GUI
            CustomerClientGUI.main(new String[0]);
        } else if (choice == 2) {
            // Run Part 2 CLI Demo
            new BankClient().runPart2Demo();
        } else {
            // Exit
            System.exit(0);
        }
    }
}
