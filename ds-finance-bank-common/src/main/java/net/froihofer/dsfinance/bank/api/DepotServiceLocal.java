package net.froihofer.dsfinance.bank.api;

import jakarta.ejb.Local;
import net.froihofer.dsfinance.bank.dto.PortfolioDTO;
import net.froihofer.dsfinance.bank.dto.PortfolioPositionDTO;
import net.froihofer.dsfinance.bank.entity.DepotEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * Local service interface for portfolio (depot) management.
 * Handles stock positions and portfolio calculations.
 */
@Local
public interface DepotServiceLocal {
    
    /**
     * Gets or creates a depot for a customer.
     * @param customerId Customer ID
     * @return Depot entity
     */
    DepotEntity getOrCreateDepot(long customerId);
    
    /**
     * Adds shares to a stock position (creates new position if doesn't exist).
     * Calculates weighted average purchase price automatically.
     * 
     * @param customerId Customer ID
     * @param stockSymbol Stock symbol
     * @param quantity Number of shares to add
     * @param purchasePrice Price per share of this purchase
     */
    void addStockPosition(long customerId, String stockSymbol, int quantity, BigDecimal purchasePrice);
    
    /**
     * Removes shares from a stock position.
     * Deletes the position entirely if quantity reaches zero.
     * 
     * @param customerId Customer ID
     * @param stockSymbol Stock symbol
     * @param quantity Number of shares to remove
     * @throws IllegalArgumentException if insufficient shares
     */
    void removeStockPosition(long customerId, String stockSymbol, int quantity);
    
    /**
     * Gets all positions in a customer's depot.
     * @param customerId Customer ID
     * @return List of position DTOs with current values
     */
    List<PortfolioPositionDTO> getDepotPositions(long customerId);
    
    /**
     * Calculates total current value of customer's portfolio.
     * @param customerId Customer ID
     * @return Total portfolio value
     */
    BigDecimal calculateTotalValue(long customerId);
    
    /**
     * Gets complete portfolio with all positions and calculated values.
     * @param customerId Customer ID
     * @return Complete portfolio DTO
     */
    PortfolioDTO getCustomerPortfolio(long customerId);
}
