package net.froihofer.dsfinance.bank.service;
import jakarta.ejb.Remote;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;

import java.util.List;
import java.util.Optional;

/**
 * Remote interface for stock search operations.
 * Accessible to both employees and customers.
 * 
 * Authentication is handled by WildFly container.
 */
@Remote
public interface StockSearchService {
    
    /**
     * Searches for stocks by company name (partial match).
     * Returns up to maxResults stocks matching the query.
     * 
     * @param query Company name or partial name to search for
     * @param maxResults Maximum number of results to return (default: 50)
     * @return List of matching stocks, empty if none found
     * @throws IllegalArgumentException if query is null or empty
     */
    List<StockQuoteDTO> findStocksByCompanyName(String query, int maxResults);
    
    /**
     * Finds a stock by its exact symbol (ticker).
     * 
     * @param symbol Stock symbol (e.g., "MSFT")
     * @return Optional containing the stock if found, empty otherwise
     * @throws IllegalArgumentException if symbol is null or empty
     */
    Optional<StockQuoteDTO> findStockBySymbol(String symbol);
    
    /**
     * Gets current market data for a specific stock.
     * This calls the external TradingService to get real-time data.
     * 
     * @param symbol Stock symbol
     * @return Optional containing current stock data, empty if not found
     */
    Optional<StockQuoteDTO> getCurrentStockQuote(String symbol);
}
