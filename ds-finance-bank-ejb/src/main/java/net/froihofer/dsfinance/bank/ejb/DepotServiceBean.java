package net.froihofer.dsfinance.bank.ejb;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import net.froihofer.dsfinance.bank.api.DepotServiceLocal;
import net.froihofer.dsfinance.bank.dto.PortfolioDTO;
import net.froihofer.dsfinance.bank.dto.PortfolioPositionDTO;
import net.froihofer.dsfinance.bank.entity.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dedicated service bean for portfolio (depot) management.
 * Handles all stock position operations and portfolio calculations.
 */
@Stateless
public class DepotServiceBean implements DepotServiceLocal {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private TradingServiceAdapterBean tradingAdapter;

    @Override
    public void ensureDepotExists(long customerId) {
        getOrCreateDepotEntity(customerId);
    }

    @Override
    public void addStockPosition(long customerId, String stockSymbol, int quantity, BigDecimal purchasePrice) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (purchasePrice == null || purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Purchase price must be positive");
        }

        DepotEntity depot = getOrCreateDepotEntity(customerId);
        String symbol = normalizeSymbol(stockSymbol);
        StockEntity stock = findOrCreateStock(symbol);

        // Find existing position or create new one
        DepotPositionEntity position = findPosition(depot, stock);

        if (position == null) {
            // Create new position
            position = new DepotPositionEntity();
            position.setDepot(depot);
            position.setStock(stock);
            position.setQuantity(quantity);
            position.setAveragePurchasePrice(purchasePrice);
            depot.getPositions().add(position);
            em.persist(position);
        } else {
            // Update existing position using weighted average
            position.addQuantity(quantity, purchasePrice);
        }

        em.flush();
    }

    @Override
    public void removeStockPosition(long customerId, String stockSymbol, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        CustomerEntity customer = findCustomerEntityById(customerId);
        if (customer == null || customer.getDepot() == null) {
            throw new IllegalArgumentException("Customer has no portfolio");
        }

        String symbol = normalizeSymbol(stockSymbol);
        StockEntity stock = findStockBySymbol(symbol);
        if (stock == null) {
            throw new IllegalArgumentException("Stock not found: " + symbol);
        }

        DepotPositionEntity position = findPosition(customer.getDepot(), stock);
        if (position == null) {
            throw new IllegalArgumentException("No position found for stock: " + symbol);
        }

        int currentQty = position.getQuantity() != null ? position.getQuantity() : 0;
        if (currentQty < quantity) {
            throw new IllegalArgumentException(
                    "Insufficient shares. Available: " + currentQty + ", Requested: " + quantity
            );
        }

        // Remove quantity
        position.removeQuantity(quantity);

        // If no shares left, remove the position entirely
        if (position.getQuantity() == 0) {
            customer.getDepot().getPositions().remove(position);
            em.remove(position);
        }

        em.flush();
    }

    @Override
    public List<PortfolioPositionDTO> getDepotPositions(long customerId) {
        CustomerEntity customer = findCustomerEntityById(customerId);
        if (customer == null || customer.getDepot() == null) {
            return new ArrayList<>();
        }

        List<PortfolioPositionDTO> positions = new ArrayList<>();

        for (DepotPositionEntity pos : customer.getDepot().getPositions()) {
            BigDecimal currentPrice = getCurrentPriceBySymbol(pos.getStock().getSymbol());
            BigDecimal totalValue = currentPrice.multiply(new BigDecimal(pos.getQuantity()));
            BigDecimal purchaseValue = pos.getAveragePurchasePrice().multiply(new BigDecimal(pos.getQuantity()));
            BigDecimal profitLoss = totalValue.subtract(purchaseValue);

            PortfolioPositionDTO dto = new PortfolioPositionDTO(
                    pos.getStock().getSymbol(),
                    pos.getStock().getCompanyName(),
                    pos.getQuantity(),
                    pos.getAveragePurchasePrice(),
                    currentPrice,
                    totalValue,
                    purchaseValue,
                    profitLoss
            );

            positions.add(dto);
        }

        return positions;
    }

    @Override
    public BigDecimal calculateTotalValue(long customerId) {
        List<PortfolioPositionDTO> positions = getDepotPositions(customerId);

        BigDecimal totalValue = BigDecimal.ZERO;
        for (PortfolioPositionDTO pos : positions) {
            if (pos.getTotalValue() != null) {
                totalValue = totalValue.add(pos.getTotalValue());
            }
        }

        return totalValue;
    }

    @Override
    public PortfolioDTO getCustomerPortfolio(long customerId) {
        List<PortfolioPositionDTO> positions = getDepotPositions(customerId);
        BigDecimal totalValue = calculateTotalValue(customerId);

        return new PortfolioDTO(customerId, positions, totalValue);
    }

    // Internal helper methods

    /**
     * Find customer entity directly using EntityManager.
     * This avoids circular EJB dependencies.
     */
    private CustomerEntity findCustomerEntityById(long customerId) {
        return em.find(CustomerEntity.class, customerId);
    }

    /**
     * Internal method to get or create depot entity.
     * Not exposed in interface to avoid entity dependencies.
     */
    private DepotEntity getOrCreateDepotEntity(long customerId) {
        CustomerEntity customer = findCustomerEntityById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        }

        if (customer.getDepot() == null) {
            DepotEntity depot = new DepotEntity();
            depot.setCustomer(customer);
            customer.setDepot(depot);
            em.persist(depot);
            em.flush();
        }

        return customer.getDepot();
    }

    private String normalizeSymbol(String symbol) {
        if (symbol == null) return null;
        String s = symbol.trim();
        return s.isEmpty() ? null : s.toUpperCase(Locale.ROOT);
    }

    private BigDecimal getCurrentPriceBySymbol(String symbol) {
        try {
            // First try to get from cached stock entity
            StockEntity stock = findStockBySymbol(symbol);
            if (stock != null && stock.getCompanyName() != null) {
                var quotes = tradingAdapter.findStockQuotesByCompanyName(stock.getCompanyName());
                if (quotes != null && !quotes.isEmpty()) {
                    for (var q : quotes) {
                        if (q.getSymbol() != null && q.getSymbol().equalsIgnoreCase(symbol)) {
                            return q.getLastTradePrice();
                        }
                    }
                    // Return first match if exact symbol not found
                    if (quotes.get(0).getLastTradePrice() != null) {
                        return quotes.get(0).getLastTradePrice();
                    }
                }
            }

            // Fallback: try direct symbol search
            var quotes = tradingAdapter.findStockQuotesByCompanyName(symbol);
            if (quotes != null && !quotes.isEmpty() && quotes.get(0).getLastTradePrice() != null) {
                return quotes.get(0).getLastTradePrice();
            }

            // Default fallback price
            return BigDecimal.ZERO;
        } catch (Exception e) {
            // Return zero on error to avoid breaking portfolio display
            return BigDecimal.ZERO;
        }
    }

    private StockEntity findOrCreateStock(String symbol) {
        symbol = normalizeSymbol(symbol);

        try {
            return em.createQuery(
                            "SELECT s FROM StockEntity s WHERE s.symbol = :symbol",
                            StockEntity.class
                    )
                    .setParameter("symbol", symbol)
                    .getSingleResult();
        } catch (NoResultException e) {
            // Create new stock
            String companyName = symbol;

            try {
                var quotes = tradingAdapter.findStockQuotesByCompanyName(symbol);
                if (quotes != null && !quotes.isEmpty() && quotes.get(0).getCompanyName() != null) {
                    companyName = quotes.get(0).getCompanyName();
                }
            } catch (Exception ignored) {
                // Use symbol as fallback
            }

            StockEntity stock = new StockEntity(symbol, companyName);
            em.persist(stock);
            return stock;
        }
    }

    private StockEntity findStockBySymbol(String symbol) {
        String sym = normalizeSymbol(symbol);
        if (sym == null) return null;

        try {
            return em.createQuery(
                            "SELECT s FROM StockEntity s WHERE s.symbol = :symbol",
                            StockEntity.class
                    )
                    .setParameter("symbol", sym)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private DepotPositionEntity findPosition(DepotEntity depot, StockEntity stock) {
        if (depot == null || depot.getPositions() == null || stock == null) {
            return null;
        }

        for (DepotPositionEntity pos : depot.getPositions()) {
            if (pos.getStock() != null && pos.getStock().getId().equals(stock.getId())) {
                return pos;
            }
        }

        return null;
    }
}