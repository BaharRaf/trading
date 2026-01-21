package net.froihofer.dsfinance.bank.service;
import net.froihofer.dsfinance.bank.exception.InsufficientFundsException;
import net.froihofer.dsfinance.bank.exception.InsufficientSharesException;

import net.froihofer.dsfinance.bank.exception.InsufficientFundsException;
import java.math.BigDecimal;
import jakarta.ejb.Remote;

import java.math.BigDecimal;

/**
 * Remote interface for stock trading operations.
 * Provides separate methods for employee and customer trading.
 * 
 * Authentication is handled by WildFly container.
 */
@Remote
public interface TradingService {
    
    /**
     * Buys stock for a specific customer (employee operation).
     * Employee must have "employee" role.
     * 
     * @param customerId Customer ID to buy stock for
     * @param symbol Stock symbol to buy
     * @param shares Number of shares to buy
     * @return Price per share at which the stock was purchased
     * @throws InsufficientFundsException if bank doesn't have enough volume
     * @throws IllegalArgumentException if customer doesn't exist or parameters invalid
     */
    BigDecimal buyStockForCustomer(long customerId, String symbol, int shares) 
            throws InsufficientFundsException;
    
    /**
     * Sells stock for a specific customer (employee operation).
     * Employee must have "employee" role.
     * 
     * @param customerId Customer ID to sell stock for
     * @param symbol Stock symbol to sell
     * @param shares Number of shares to sell
     * @return Price per share at which the stock was sold
     * @throws InsufficientSharesException if customer doesn't own enough shares
     * @throws IllegalArgumentException if customer doesn't exist or parameters invalid
     */
    BigDecimal sellStockForCustomer(long customerId, String symbol, int shares) 
            throws InsufficientSharesException;
    
    /**
     * Buys stock for the authenticated customer (customer operation).
     * Customer identity is derived from SessionContext - no customerId parameter needed.
     * Customer must have "customer" role.
     * 
     * @param symbol Stock symbol to buy
     * @param shares Number of shares to buy
     * @return Price per share at which the stock was purchased
     * @throws InsufficientFundsException if bank doesn't have enough volume
     */
    BigDecimal buyStock(String symbol, int shares) throws InsufficientFundsException;
    
    /**
     * Sells stock for the authenticated customer (customer operation).
     * Customer identity is derived from SessionContext - no customerId parameter needed.
     * Customer must have "customer" role.
     * 
     * @param symbol Stock symbol to sell
     * @param shares Number of shares to sell
     * @return Price per share at which the stock was sold
     * @throws InsufficientSharesException if customer doesn't own enough shares
     */
    BigDecimal sellStock(String symbol, int shares) throws InsufficientSharesException;
}
