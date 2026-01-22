package net.froihofer.dsfinance.bank.ejb;

import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;
import java.util.Locale;

import java.math.BigDecimal;
import java.util.List;

import net.froihofer.dsfinance.bank.api.CustomerServiceLocal;
import net.froihofer.dsfinance.bank.api.DepotServiceLocal;
import net.froihofer.dsfinance.bank.api.EmployeeBankService;
import net.froihofer.dsfinance.bank.dto.CustomerDTO;
import net.froihofer.dsfinance.bank.dto.PortfolioDTO;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;
import net.froihofer.dsfinance.bank.entity.*;

/**
 * Employee bank service with integrated service layer.
 * Uses CustomerServiceLocal, DepotServiceLocal, and TradingServiceAdapter.
 */
@Stateless
@RolesAllowed("employee")
public class EmployeeBankServiceBean implements EmployeeBankService {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private TradingServiceAdapterBean tradingAdapter;

    @EJB
    private CustomerServiceLocal customerService;

    @EJB
    private DepotServiceLocal depotService;

    @Resource
    private SessionContext sessionContext;

    @Override
    public long createCustomer(CustomerDTO customer) {
        // Delegate to CustomerServiceLocal
        return customerService.createCustomer(customer);
    }

    @Override
    public CustomerDTO findCustomerById(long customerId) {
        return customerService.findById(customerId);
    }

    @Override
    public CustomerDTO findCustomerByCustomerNumber(String customerNumber) {
        return customerService.findByCustomerNumber(customerNumber);
    }

    @Override
    public List<CustomerDTO> findCustomersByName(String firstName, String lastName) {
        // Delegate to CustomerServiceLocal
        return customerService.searchByName(firstName, lastName);
    }

    @Override
    public List<StockQuoteDTO> findStockQuotesByCompanyName(String companyNameQuery) {
        return tradingAdapter.findStockQuotesByCompanyName(companyNameQuery);
    }

    @Override
    public StockQuoteDTO findStockQuoteBySymbol(String symbol) {
        // reuse your already working “find current price by symbol” logic
        return findQuoteBySymbolWithFallback(symbol);
    }

    @Override
    public BigDecimal buyStockForCustomer(long customerId, String symbol, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        // Validate customer access (employees can access any customer)
        validateCustomerAccess(customerId);

        String sym = normalizeSymbol(symbol);
        
        // Step 1: Check bank has sufficient volume (estimate with current price)
        BigDecimal estimatedPrice = getCurrentPriceBySymbol(sym);
        BigDecimal estimatedCost = estimatedPrice.multiply(BigDecimal.valueOf(quantity));
        BankEntity bank = getBankEntity();
        
        if (bank.getAvailableVolume().compareTo(estimatedCost) < 0) {
            throw new IllegalStateException(
                "Insufficient bank volume. Available: " + bank.getAvailableVolume() + 
                ", Estimated cost: " + estimatedCost
            );
        }

        // Step 2: Execute BUY order on stock exchange via WS (CRITICAL!)
        // If this fails, RuntimeException is thrown and transaction rolls back
        BigDecimal executionPrice = tradingAdapter.buy(sym, quantity);
        BigDecimal totalCost = executionPrice.multiply(BigDecimal.valueOf(quantity));

        // Step 3: Decrease bank volume (using actual execution price)
        bank.decreaseVolume(totalCost);

        // Step 4: Add position to customer depot
        depotService.addStockPosition(customerId, sym, quantity, executionPrice);

        em.flush();
        return executionPrice;
    }

    @Override
    public BigDecimal sellStockForCustomer(long customerId, String symbol, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        // Validate customer access
        validateCustomerAccess(customerId);

        String sym = normalizeSymbol(symbol);

        // Step 1: Verify customer has sufficient shares BEFORE calling WS
        // This is done inside removeStockPosition, but we check first to fail fast
        PortfolioDTO portfolio = depotService.getCustomerPortfolio(customerId);
        boolean hasShares = false;
        for (var pos : portfolio.getPositions()) {
            if (pos.getSymbol() != null && pos.getSymbol().equalsIgnoreCase(sym)) {
                if (pos.getQuantity() >= quantity) {
                    hasShares = true;
                    break;
                }
            }
        }
        if (!hasShares) {
            throw new IllegalArgumentException(
                "Insufficient shares of " + sym + " to sell. Requested: " + quantity
            );
        }

        // Step 2: Execute SELL order on stock exchange via WS (CRITICAL!)
        // If this fails, RuntimeException is thrown and transaction rolls back
        BigDecimal executionPrice = tradingAdapter.sell(sym, quantity);
        BigDecimal totalRevenue = executionPrice.multiply(BigDecimal.valueOf(quantity));

        // Step 3: Remove position from customer depot
        depotService.removeStockPosition(customerId, sym, quantity);

        // Step 4: Increase bank volume (using actual execution price)
        BankEntity bank = getBankEntity();
        bank.increaseVolume(totalRevenue);

        em.flush();
        return executionPrice;
    }

    @Override
    public PortfolioDTO getCustomerPortfolio(long customerId) {
        // Validate customer access
        validateCustomerAccess(customerId);

        // Delegate to DepotService (includes profit/loss calculation)
        return depotService.getCustomerPortfolio(customerId);
    }

    @Override
    public BigDecimal getInvestableVolume() {
        BankEntity bank = getBankEntity();
        return bank.getAvailableVolume();
    }

    /**
     * Validates that the caller has access to the customer account.
     * Employees can access any customer.
     * Customers can only access their own account.
     *
     * @param customerId Customer ID being accessed
     * @throws SecurityException if customer tries to access another account
     */
    private void validateCustomerAccess(long customerId) {
        if (sessionContext != null && sessionContext.isCallerInRole("customer")) {
            // Customer role - validate they're accessing their own account
            String username = sessionContext.getCallerPrincipal().getName();
            CustomerDTO authenticatedCustomer = customerService.findByUsername(username);

            if (authenticatedCustomer == null) {
                throw new SecurityException("Customer not found for username: " + username);
            }

            if (authenticatedCustomer.getId() != customerId) {
                throw new SecurityException(
                        "Access denied: Customer can only access own account"
                );
            }
        }
        // Employee role or no session context - access granted
    }

    // Helper methods
    private String normalizeSymbol(String symbol) {
        if (symbol == null) return null;
        String s = symbol.trim();
        return s.isEmpty() ? null : s.toUpperCase(Locale.ROOT);
    }

    private StockQuoteDTO findQuoteBySymbol(String symbol) {
        String sym = normalizeSymbol(symbol);
        if (sym == null) throw new IllegalArgumentException("Symbol must not be blank");

        List<StockQuoteDTO> quotes = tradingAdapter.findStockQuotesByCompanyName(sym);
        if (quotes != null) {
            for (StockQuoteDTO q : quotes) {
                if (q != null && q.getSymbol() != null) {
                    String qs = normalizeSymbol(q.getSymbol());
                    if (sym.equals(qs)) return q;
                }
            }
        }

        if (quotes != null && quotes.size() == 1 && quotes.get(0) != null && quotes.get(0).getLastTradePrice() != null) {
            return quotes.get(0);
        }

        throw new IllegalArgumentException("Stock not found: " + sym);
    }

    private BigDecimal getCurrentPriceBySymbol(String symbol) {
        StockQuoteDTO q = findQuoteBySymbolWithFallback(symbol);
        if (q.getLastTradePrice() == null) {
            throw new IllegalStateException("TradingService returned no price for: " + normalizeSymbol(symbol));
        }
        return q.getLastTradePrice();
    }

    private StockQuoteDTO findQuoteBySymbolWithFallback(String symbol) {
        String sym = normalizeSymbol(symbol);
        if (sym == null) throw new IllegalArgumentException("Symbol must not be blank");

        try {
            return findQuoteBySymbol(sym);
        } catch (RuntimeException firstFail) {
            StockEntity cached = findStockBySymbol(sym);
            if (cached != null && cached.getCompanyName() != null && !cached.getCompanyName().isBlank()) {
                List<StockQuoteDTO> quotes = tradingAdapter.findStockQuotesByCompanyName(cached.getCompanyName());
                if (quotes != null) {
                    for (StockQuoteDTO q : quotes) {
                        if (q != null && q.getSymbol() != null && sym.equals(normalizeSymbol(q.getSymbol()))) {
                            return q;
                        }
                    }
                    if (quotes.size() == 1 && quotes.get(0) != null && quotes.get(0).getLastTradePrice() != null) {
                        return quotes.get(0);
                    }
                }
            }

            throw new IllegalArgumentException(
                    "Stock not found by symbol '" + sym + "'. Hint: search the stock by company name first.",
                    firstFail
            );
        }
    }

    private StockEntity findStockBySymbol(String symbol) {
        String sym = normalizeSymbol(symbol);
        if (sym == null) return null;

        try {
            return em.createQuery("SELECT s FROM StockEntity s WHERE s.symbol = :symbol", StockEntity.class)
                    .setParameter("symbol", sym)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private BankEntity getBankEntity() {
        List<BankEntity> banks = em.createQuery("SELECT b FROM BankEntity b", BankEntity.class)
                .getResultList();
        if (banks.isEmpty()) {
            BankEntity bank = new BankEntity();
            bank.setTotalInvestableVolume(new BigDecimal("1000000000"));
            bank.setAvailableVolume(new BigDecimal("1000000000"));
            em.persist(bank);
            return bank;
        }
        return banks.get(0);
    }
}