package net.froihofer.dsfinance.bank.api;

import jakarta.ejb.Remote;
import java.math.BigDecimal;
import java.util.List;
import net.froihofer.dsfinance.bank.dto.PortfolioDTO;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;

@Remote
public interface CustomerBankService {

  List<StockQuoteDTO> findStockQuotesByCompanyName(String companyNameQuery);

  String whoAmI();

  // Customer operations (NO customer ID parameter)
  BigDecimal buyStock(String symbol, int quantity);

  BigDecimal sellStock(String symbol, int quantity);

  PortfolioDTO getMyPortfolio();
}
