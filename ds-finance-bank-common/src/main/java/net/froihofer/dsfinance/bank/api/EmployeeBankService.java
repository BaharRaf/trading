package net.froihofer.dsfinance.bank.api;

import jakarta.ejb.Remote;
import java.util.List;
import net.froihofer.dsfinance.bank.dto.CustomerDTO;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;

@Remote
public interface EmployeeBankService {

  long createCustomer(CustomerDTO customer);

  CustomerDTO findCustomerById(long customerId);

  List<CustomerDTO> findCustomersByName(String firstName, String lastName);

  /**
   * WebService demo call (TradingService):
   * Searches stock quotes by (partial) company name.
   */
  List<StockQuoteDTO> findStockQuotesByCompanyName(String companyNameQuery);
}
