package net.froihofer.dsfinance.bank.client;

import java.util.List;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import net.froihofer.dsfinance.bank.api.CustomerBankService;
import net.froihofer.dsfinance.bank.api.EmployeeBankService;
import net.froihofer.dsfinance.bank.dto.CustomerDTO;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;
import net.froihofer.util.AuthCallbackHandler;
import net.froihofer.util.WildflyJndiLookupHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal client for Project Part 2:
 *  - performs a remote EJB call (client -> server)
 *  - triggers a TradingService WebService call (server-side adapter)
 *  - persists a JPA entity (createCustomer)
 */
public class BankClient {
  private static final Logger log = LoggerFactory.getLogger(BankClient.class);

  private WildflyJndiLookupHelper createJndiHelper(String username, String password) throws NamingException {
    AuthCallbackHandler.setUsername(username);
    AuthCallbackHandler.setPassword(password);

    Properties props = new Properties();
    props.put(Context.SECURITY_PRINCIPAL, AuthCallbackHandler.getUsername());
    props.put(Context.SECURITY_CREDENTIALS, AuthCallbackHandler.getPassword());

    // applicationName/moduleName must match the EAR/EJB names used by the build
    return new WildflyJndiLookupHelper(new InitialContext(props), "ds-finance-bank-ear", "ds-finance-bank-ejb", "");
  }

  private void run() {
    try {
      // 1) Client -> Server remote call (Employee)
      WildflyJndiLookupHelper employeeJndi = createJndiHelper("employee", "employeepass");
      EmployeeBankService employeeService =
          employeeJndi.lookupUsingJBossEjbClient("EmployeeBankServiceBean", EmployeeBankService.class, true);

      log.info("Remote EJB lookup successful: {}", employeeService.getClass());

      // 3) Persist a JPA Entity (Customer)
      CustomerDTO newCustomer = new CustomerDTO(null, "C-10001", "Ada", "Lovelace", "Example Street 1, 1010 Vienna");
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
    new BankClient().run();
  }
}
