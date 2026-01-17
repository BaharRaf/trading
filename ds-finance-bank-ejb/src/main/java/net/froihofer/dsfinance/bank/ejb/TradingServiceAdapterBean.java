package net.froihofer.dsfinance.bank.ejb;

import jakarta.ejb.Stateless;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for TradingService - uses mock data until WSDL classes are generated
 */
@Stateless
public class TradingServiceAdapterBean {

  /**
   * Find stock quotes by company name
   * Currently returns mock data - will be replaced with real SOAP calls after build
   */
  public List<StockQuoteDTO> findStockQuotesByCompanyName(String companyNameQuery) {
    List<StockQuoteDTO> result = new ArrayList<>();

    if (companyNameQuery == null || companyNameQuery.isEmpty()) {
      // Return default stocks if no query
      result.add(new StockQuoteDTO("AAPL", "Apple Inc.", new BigDecimal("150.00")));
      result.add(new StockQuoteDTO("MSFT", "Microsoft Corporation", new BigDecimal("380.00")));
      result.add(new StockQuoteDTO("GOOGL", "Alphabet Inc.", new BigDecimal("140.00")));
      result.add(new StockQuoteDTO("AMZN", "Amazon.com Inc.", new BigDecimal("180.00")));
      result.add(new StockQuoteDTO("TSLA", "Tesla Inc.", new BigDecimal("250.00")));
    } else {
      String lowerQuery = companyNameQuery.toLowerCase();

      // Mock search logic
      if (lowerQuery.contains("apple") || lowerQuery.contains("aapl")) {
        result.add(new StockQuoteDTO("AAPL", "Apple Inc.", new BigDecimal("150.00")));
      }
      if (lowerQuery.contains("microsoft") || lowerQuery.contains("msft")) {
        result.add(new StockQuoteDTO("MSFT", "Microsoft Corporation", new BigDecimal("380.00")));
      }
      if (lowerQuery.contains("google") || lowerQuery.contains("googl") || lowerQuery.contains("alphabet")) {
        result.add(new StockQuoteDTO("GOOGL", "Alphabet Inc.", new BigDecimal("140.00")));
      }
      if (lowerQuery.contains("amazon") || lowerQuery.contains("amzn")) {
        result.add(new StockQuoteDTO("AMZN", "Amazon.com Inc.", new BigDecimal("180.00")));
      }
      if (lowerQuery.contains("tesla") || lowerQuery.contains("tsla")) {
        result.add(new StockQuoteDTO("TSLA", "Tesla Inc.", new BigDecimal("250.00")));
      }
      if (lowerQuery.contains("meta") || lowerQuery.contains("facebook") || lowerQuery.contains("fb")) {
        result.add(new StockQuoteDTO("META", "Meta Platforms Inc.", new BigDecimal("480.00")));
      }
      if (lowerQuery.contains("netflix") || lowerQuery.contains("nflx")) {
        result.add(new StockQuoteDTO("NFLX", "Netflix Inc.", new BigDecimal("600.00")));
      }
    }

    return result;
  }
}