package net.froihofer.dsfinance.bank.ejb;

import jakarta.ejb.Stateless;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class TradingServiceAdapterBean {

  public List<StockQuoteDTO> findStockQuotesByCompanyName(String companyNameQuery) {
    // TODO: Implement after WSDL client generation
    // For now, return mock data
    List<StockQuoteDTO> result = new ArrayList<>();

    // Mock Apple stock
    if (companyNameQuery.toLowerCase().contains("apple") ||
            companyNameQuery.equalsIgnoreCase("AAPL")) {
      result.add(new StockQuoteDTO("AAPL", "Apple Inc.", new BigDecimal("150.00")));
    }

    // Mock Microsoft stock
    if (companyNameQuery.toLowerCase().contains("microsoft") ||
            companyNameQuery.equalsIgnoreCase("MSFT")) {
      result.add(new StockQuoteDTO("MSFT", "Microsoft Corporation", new BigDecimal("380.00")));
    }

    // Mock Google stock
    if (companyNameQuery.toLowerCase().contains("google") ||
            companyNameQuery.equalsIgnoreCase("GOOGL")) {
      result.add(new StockQuoteDTO("GOOGL", "Alphabet Inc.", new BigDecimal("140.00")));
    }

    return result;
  }
}
