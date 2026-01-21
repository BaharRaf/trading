package net.froihofer.dsfinance.bank.ejb;

import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;
import java.io.File;
import net.froihofer.util.jboss.WildflyAuthDBHelper;
import java.util.Locale;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.froihofer.dsfinance.bank.api.CustomerServiceLocal;
import net.froihofer.dsfinance.bank.api.DepotServiceLocal;
import net.froihofer.dsfinance.bank.api.EmployeeBankService;
import net.froihofer.dsfinance.bank.dto.CustomerDTO;
import net.froihofer.dsfinance.bank.dto.PortfolioDTO;
import net.froihofer.dsfinance.bank.dto.PortfolioPositionDTO;
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
  private CustomerServiceBean customerServiceBean;

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
    public List<CustomerDTO> findCustomersByName(String firstName, String lastName) {
        // Delegate to CustomerServiceLocal
        return customerService.searchByName(firstName, lastName);
    }

    @Override
    public List<StockQuoteDTO> findStockQuotesByCompanyName(String companyNameQuery) {
        return tradingAdapter.findStockQuotesByCompanyName(companyNameQuery);
    }

    @Override
    public BigDecimal buyStockForCustomer(long customerId, String symbol, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        // Validate customer access (employees can access any customer)
        validateCustomerAccess(customerId);

        String sym = normalizeSymbol(symbol);
        BigDecimal pricePerShare = getCurrentPriceBySymbol(sym);
        BigDecimal totalCost = pricePerShare.multiply(BigDecimal.valueOf(quantity));

        // Get bank entity and use business method
        BankEntity bank = getBankEntity();
        bank.decreaseVolume(totalCost);  // Uses business method with validation

        // Use DepotService to add position (handles weighted average automatically)
        depotService.addStockPosition(customerId, sym, quantity, pricePerShare);

        em.flush();
        return pricePerShare;
    }

    @Override
    public BigDecimal sellStockForCustomer(long customerId, String symbol, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        // Validate customer access
        validateCustomerAccess(customerId);

        String sym = normalizeSymbol(symbol);
        BigDecimal pricePerShare = getCurrentPriceBySymbol(sym);
        BigDecimal totalRevenue = pricePerShare.multiply(BigDecimal.valueOf(quantity));

        // Use DepotService to remove position
        depotService.removeStockPosition(customerId, sym, quantity);

        // Get bank entity and use business method
        BankEntity bank = getBankEntity();
        bank.increaseVolume(totalRevenue);  // Uses business method

        em.flush();
        return pricePerShare;
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
    private BigDecimal getCurrentPrice(String symbol) {
        List<StockQuoteDTO> quotes = tradingAdapter.findStockQuotesByCompanyName(symbol);
        for (StockQuoteDTO quote : quotes) {
            if (quote.getSymbol().equalsIgnoreCase(symbol)) {
                return quote.getLastTradePrice();
            }
        }
        throw new IllegalArgumentException("Stock not found: " + symbol);
    }

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
            bank.setTotalInvestableVolume(new BigDecimal("1000000"));
            bank.setAvailableVolume(new BigDecimal("1000000"));
            em.persist(bank);
            return bank;
        }
        return banks.get(0);
    }
}
