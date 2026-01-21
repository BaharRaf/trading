package at.ac.csw.dke.bank.ejb;

import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;
import net.froihofer.dsfinance.bank.service.StockSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * EJB implementation for stock search operations.
 * Accessible to both employees and customers.
 */
@Stateless
@DeclareRoles({"employee", "customer"})
public class StockSearchServiceBean implements StockSearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(StockSearchServiceBean.class);
    
    @Resource
    private SessionContext sessionContext;
    
    @Override
    @RolesAllowed({"employee", "customer"})
    public List<StockQuoteDTO> findStocksByCompanyName(String query, int maxResults) {
        String username = sessionContext.getCallerPrincipal().getName();
        logger.debug("User '{}' searching for stocks: {}", username, query);
        
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        
        // TODO: Call external TradingService to search stocks
        // Return up to maxResults matching stocks
        
        throw new UnsupportedOperationException("Implementation pending");
    }
    
    @Override
    @RolesAllowed({"employee", "customer"})
    public Optional<StockQuoteDTO> findStockBySymbol(String symbol) {
        String username = sessionContext.getCallerPrincipal().getName();
        logger.debug("User '{}' searching for stock symbol: {}", username, symbol);
        
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Stock symbol cannot be empty");
        }
        
        // TODO: Call external TradingService to get stock by symbol
        // Return Optional.empty() if not found
        
        throw new UnsupportedOperationException("Implementation pending");
    }
    
    @Override
    @RolesAllowed({"employee", "customer"})
    public Optional<StockQuoteDTO> getCurrentStockQuote(String symbol) {
        String username = sessionContext.getCallerPrincipal().getName();
        logger.debug("User '{}' getting quote for: {}", username, symbol);
        
        // TODO: Call external TradingService to get real-time quote
        
        throw new UnsupportedOperationException("Implementation pending");
    }
}
