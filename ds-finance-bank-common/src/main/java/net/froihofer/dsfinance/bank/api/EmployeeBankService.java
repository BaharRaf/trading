package net.froihofer.dsfinance.bank.api;

import jakarta.ejb.Remote;
import java.math.BigDecimal;
import java.util.List;
import net.froihofer.dsfinance.bank.dto.CustomerDTO;
import net.froihofer.dsfinance.bank.dto.PortfolioDTO;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;

@Remote
public interface EmployeeBankService {

  long createCustomer(CustomerDTO customer);

  CustomerDTO findCustomerById(long customerId);

  /**
   * Find customer by customer number (e.g., "C-12345").
   * @param customerNumber The unique customer number
   * @return CustomerDTO or null if not found
   */
  CustomerDTO findCustomerByCustomerNumber(String customerNumber);

  List<CustomerDTO> findCustomersByName(String firstName, String lastName);

  List<StockQuoteDTO> findStockQuotesByCompanyName(String companyNameQuery);

  StockQuoteDTO findStockQuoteBySymbol(String symbol);

  // Trading operations
  BigDecimal buyStockForCustomer(long customerId, String symbol, int quantity);

  BigDecimal sellStockForCustomer(long customerId, String symbol, int quantity);

  PortfolioDTO getCustomerPortfolio(long customerId);

  BigDecimal getInvestableVolume();
}
