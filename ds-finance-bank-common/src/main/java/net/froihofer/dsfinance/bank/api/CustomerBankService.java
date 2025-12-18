package net.froihofer.dsfinance.bank.api;

import jakarta.ejb.Remote;
import java.util.List;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;

@Remote
public interface CustomerBankService {

  /**
   * WebService demo call (TradingService) for customers, too.
   */
  List<StockQuoteDTO> findStockQuotesByCompanyName(String companyNameQuery);

  /**
   * For the course project: customer identity is derived from the authenticated principal.
   * This method is included as a placeholder for Part 2; actual trading operations will be implemented later.
   */
  String whoAmI();
}
