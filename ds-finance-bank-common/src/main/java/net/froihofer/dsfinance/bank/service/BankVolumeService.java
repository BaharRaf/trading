package net.froihofer.dsfinance.bank.service;

import jakarta.ejb.Remote;

import java.math.BigDecimal;

/**
 * Remote interface for bank volume management.
 * Only accessible to employees.
 * 
 * Authentication is handled by WildFly container.
 */
@Remote
public interface BankVolumeService {
    
    /**
     * Gets the bank's current available trading budget.
     * This is the amount of money the bank can still use to buy stocks.
     * 
     * @return Available trading volume in USD
     */
    BigDecimal getAvailableTradingBudget();
    
    /**
     * Gets the total market value of all customer portfolios.
     * This value fluctuates with stock prices.
     * 
     * @return Total market value across all depots in USD
     */
    BigDecimal getTotalMarketValueAcrossAllDepots();
    
    /**
     * Gets the bank's total investable volume limit.
     * This is the maximum amount set by the exchange (initially 1 billion USD).
     * 
     * @return Total investable volume in USD
     */
    BigDecimal getTotalInvestableVolume();
}
