package net.froihofer.dsfinance.bank.ejb;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.froihofer.dsfinance.bank.api.EmployeeBankService;
import net.froihofer.dsfinance.bank.dto.CustomerDTO;
import net.froihofer.dsfinance.bank.dto.PortfolioDTO;
import net.froihofer.dsfinance.bank.dto.PortfolioPositionDTO;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;
import net.froihofer.dsfinance.bank.entity.*;

@Stateless
@RolesAllowed("employee")
public class EmployeeBankServiceBean implements EmployeeBankService {

  @PersistenceContext
  private EntityManager em;

  @EJB
  private TradingServiceAdapterBean tradingAdapter;

  @Override
  public long createCustomer(CustomerDTO customer) {
    if (customer == null) throw new IllegalArgumentException("customer must not be null");
    if (customer.getCustomerNumber() == null || customer.getCustomerNumber().isBlank())
      throw new IllegalArgumentException("customerNumber must not be blank");
    CustomerEntity entity = new CustomerEntity(
            customer.getCustomerNumber(),
            customer.getFirstName(),
            customer.getLastName(),
            customer.getAddress()
    );
    em.persist(entity);
    em.flush();
    return entity.getId();
  }

  @Override
  public CustomerDTO findCustomerById(long customerId) {
    CustomerEntity c = em.find(CustomerEntity.class, customerId);
    return c == null ? null : toDto(c);
  }

  @Override
  public List<CustomerDTO> findCustomersByName(String firstName, String lastName) {
    List<CustomerEntity> results = em.createNamedQuery("Customer.findByName", CustomerEntity.class)
            .setParameter("first", firstName)
            .setParameter("last", lastName)
            .getResultList();
    return results.stream().map(this::toDto).collect(Collectors.toList());
  }

  @Override
  public List<StockQuoteDTO> findStockQuotesByCompanyName(String companyNameQuery) {
    return tradingAdapter.findStockQuotesByCompanyName(companyNameQuery);
  }

  @Override
  public BigDecimal buyStockForCustomer(long customerId, String symbol, int quantity) {
    if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

    // Get current stock price
    BigDecimal pricePerShare = getCurrentPrice(symbol);
    BigDecimal totalCost = pricePerShare.multiply(BigDecimal.valueOf(quantity));

    // Find customer and ensure depot exists
    CustomerEntity customer = em.find(CustomerEntity.class, customerId);
    if (customer == null) throw new IllegalArgumentException("Customer not found");

    if (customer.getDepot() == null) {
      DepotEntity depot = new DepotEntity();
      depot.setCustomer(customer);
      customer.setDepot(depot);
      em.persist(depot);
    }

    // Check bank volume
    BankEntity bank = getBankEntity();
    if (bank.getAvailableVolume().compareTo(totalCost) < 0) {
      throw new IllegalStateException("Insufficient bank volume");
    }

    // Find or create stock entity
    StockEntity stock = findOrCreateStock(symbol);

    // Update depot position
    DepotPositionEntity position = findOrCreatePosition(customer.getDepot(), stock);
    int oldQty = position.getQuantity() != null ? position.getQuantity() : 0;
    BigDecimal oldAvg = position.getAveragePurchasePrice() != null ?
            position.getAveragePurchasePrice() : BigDecimal.ZERO;

    // Calculate new average price
    BigDecimal totalOldValue = oldAvg.multiply(BigDecimal.valueOf(oldQty));
    BigDecimal totalNewValue = totalOldValue.add(totalCost);
    int newQty = oldQty + quantity;
    BigDecimal newAvg = totalNewValue.divide(BigDecimal.valueOf(newQty), 2, RoundingMode.HALF_UP);

    position.setQuantity(newQty);
    position.setAveragePurchasePrice(newAvg);

    // Update bank volume
    bank.setAvailableVolume(bank.getAvailableVolume().subtract(totalCost));

    return pricePerShare;
  }

  @Override
  public BigDecimal sellStockForCustomer(long customerId, String symbol, int quantity) {
    if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

    CustomerEntity customer = em.find(CustomerEntity.class, customerId);
    if (customer == null || customer.getDepot() == null) {
      throw new IllegalArgumentException("Customer not found or has no portfolio");
    }

    StockEntity stock = findStockBySymbol(symbol);
    if (stock == null) throw new IllegalArgumentException("Stock not found");

    DepotPositionEntity position = findPosition(customer.getDepot(), stock);
    if (position == null || position.getQuantity() < quantity) {
      throw new IllegalArgumentException("Insufficient shares to sell");
    }

    BigDecimal pricePerShare = getCurrentPrice(symbol);
    BigDecimal totalRevenue = pricePerShare.multiply(BigDecimal.valueOf(quantity));

    // Update position
    int newQty = position.getQuantity() - quantity;
    if (newQty == 0) {
      customer.getDepot().getPositions().remove(position);
      em.remove(position);
    } else {
      position.setQuantity(newQty);
    }

    // Update bank volume
    BankEntity bank = getBankEntity();
    bank.setAvailableVolume(bank.getAvailableVolume().add(totalRevenue));

    return pricePerShare;
  }

  @Override
  public PortfolioDTO getCustomerPortfolio(long customerId) {
    CustomerEntity customer = em.find(CustomerEntity.class, customerId);
    if (customer == null || customer.getDepot() == null) {
      return new PortfolioDTO(customerId, new ArrayList<>(), BigDecimal.ZERO);
    }

    List<PortfolioPositionDTO> positions = new ArrayList<>();
    BigDecimal totalValue = BigDecimal.ZERO;

    for (DepotPositionEntity pos : customer.getDepot().getPositions()) {
      BigDecimal currentPrice = getCurrentPrice(pos.getStock().getSymbol());
      BigDecimal posValue = currentPrice.multiply(BigDecimal.valueOf(pos.getQuantity()));

      positions.add(new PortfolioPositionDTO(
              pos.getStock().getSymbol(),
              pos.getStock().getCompanyName(),
              pos.getQuantity(),
              pos.getAveragePurchasePrice(),
              currentPrice,
              posValue
      ));

      totalValue = totalValue.add(posValue);
    }

    return new PortfolioDTO(customerId, positions, totalValue);
  }

  @Override
  public BigDecimal getInvestableVolume() {
    BankEntity bank = getBankEntity();
    return bank.getAvailableVolume();
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

  private StockEntity findOrCreateStock(String symbol) {
    try {
      return em.createQuery("SELECT s FROM StockEntity s WHERE s.symbol = :symbol", StockEntity.class)
              .setParameter("symbol", symbol)
              .getSingleResult();
    } catch (NoResultException e) {
      // Get company name from trading service
      String companyName = symbol; // Default
      List<StockQuoteDTO> quotes = tradingAdapter.findStockQuotesByCompanyName(symbol);
      for (StockQuoteDTO quote : quotes) {
        if (quote.getSymbol().equalsIgnoreCase(symbol)) {
          companyName = quote.getCompanyName();
          break;
        }
      }

      StockEntity stock = new StockEntity(symbol, companyName);
      em.persist(stock);
      return stock;
    }
  }

  private StockEntity findStockBySymbol(String symbol) {
    try {
      return em.createQuery("SELECT s FROM StockEntity s WHERE s.symbol = :symbol", StockEntity.class)
              .setParameter("symbol", symbol)
              .getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  private DepotPositionEntity findOrCreatePosition(DepotEntity depot, StockEntity stock) {
    DepotPositionEntity position = findPosition(depot, stock);
    if (position == null) {
      position = new DepotPositionEntity();
      position.setDepot(depot);
      position.setStock(stock);
      position.setQuantity(0);
      position.setAveragePurchasePrice(BigDecimal.ZERO);
      depot.getPositions().add(position);
      em.persist(position);
    }
    return position;
  }

  private DepotPositionEntity findPosition(DepotEntity depot, StockEntity stock) {
    for (DepotPositionEntity pos : depot.getPositions()) {
      if (pos.getStock().getId().equals(stock.getId())) {
        return pos;
      }
    }
    return null;
  }

  private BankEntity getBankEntity() {
    List<BankEntity> banks = em.createQuery("SELECT b FROM BankEntity b", BankEntity.class)
            .getResultList();
    if (banks.isEmpty()) {
      // Initialize bank with default volume
      BankEntity bank = new BankEntity();
      bank.setTotalInvestableVolume(new BigDecimal("1000000"));
      bank.setAvailableVolume(new BigDecimal("1000000"));
      em.persist(bank);
      return bank;
    }
    return banks.get(0);
  }

  private CustomerDTO toDto(CustomerEntity c) {
    return new CustomerDTO(c.getId(), c.getCustomerNumber(), c.getFirstName(), c.getLastName(), c.getAddress());
  }
}
