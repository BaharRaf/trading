package net.froihofer.dsfinance.bank.service;
import jakarta.ejb.Remote;
import net.froihofer.dsfinance.bank.dto.PortfolioDTO;

/**
 * Remote interface for portfolio viewing operations.
 * Provides separate methods for employee and customer access.
 * 
 * Authentication is handled by WildFly container.
 */
@Remote
public interface PortfolioService {
    
    /**
     * Gets the portfolio for a specific customer (employee operation).
     * Employee must have "employee" role.
     * 
     * @param customerId Customer ID whose portfolio to retrieve
     * @return PortfolioDTO containing all positions and total value
     * @throws IllegalArgumentException if customer doesn't exist
     */
    PortfolioDTO getCustomerPortfolio(long customerId);
    
    /**
     * Gets the portfolio for the authenticated customer (customer operation).
     * Customer identity is derived from SessionContext - no customerId parameter needed.
     * Customer must have "customer" role.
     * 
     * @return PortfolioDTO containing all positions and total value
     */
    PortfolioDTO getMyPortfolio();
}
