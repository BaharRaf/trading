package net.froihofer.dsfinance.bank.ejb;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;
import net.froihofer.dsfinance.bank.api.EmployeeBankService;
import net.froihofer.dsfinance.bank.dto.CustomerDTO;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;
import net.froihofer.dsfinance.bank.entity.CustomerEntity;

@Stateless(name = "EmployeeBankServiceBean")
@RolesAllowed("employee")
public class EmployeeBankServiceBean implements EmployeeBankService {

  @PersistenceContext(unitName = "ds-finance-bank-ref-persunit")
  private EntityManager em;

  @Inject
  private TradingServiceAdapterBean trading;

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
    em.flush(); // ensure ID assigned
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
    return trading.findStockQuotesByCompanyName(companyNameQuery);
  }

  private CustomerDTO toDto(CustomerEntity c) {
    return new CustomerDTO(c.getId(), c.getCustomerNumber(), c.getFirstName(), c.getLastName(), c.getAddress());
  }
}
