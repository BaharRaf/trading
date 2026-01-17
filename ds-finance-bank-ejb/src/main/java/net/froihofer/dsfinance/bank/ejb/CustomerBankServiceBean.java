package net.froihofer.dsfinance.bank.ejb;

import jakarta.annotation.Resource;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;
import net.froihofer.dsfinance.bank.api.CustomerBankService;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;

@Stateless(name = "CustomerBankServiceBean")
public class CustomerBankServiceBean implements CustomerBankService {

  @Inject
  private TradingServiceAdapterBean trading;

  @Resource
  private SessionContext sessionContext;

  @Override
  public List<StockQuoteDTO> findStockQuotesByCompanyName(String companyNameQuery) {
    return trading.findStockQuotesByCompanyName(companyNameQuery);
  }

  @Override
  public String whoAmI() {
    return sessionContext != null && sessionContext.getCallerPrincipal() != null
        ? sessionContext.getCallerPrincipal().getName()
        : "UNKNOWN";
  }
}
