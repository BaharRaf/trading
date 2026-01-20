package net.froihofer.dsfinance.bank.ejb;

import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJB;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.List;

import net.froihofer.dsfinance.bank.api.CustomerBankService;
import net.froihofer.dsfinance.bank.api.EmployeeBankService;
import net.froihofer.dsfinance.bank.dto.PortfolioDTO;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;
import net.froihofer.dsfinance.bank.entity.CustomerEntity;

@Stateless(name = "CustomerBankServiceBean")
@RolesAllowed("customer")
@RunAs("employee")
@DeclareRoles({"customer", "employee"})
public class CustomerBankServiceBean implements CustomerBankService {

    @EJB
    private TradingServiceAdapterBean trading;

    @Resource
    private SessionContext sessionContext;

    @PersistenceContext
    private EntityManager em;

    @EJB
    private EmployeeBankService employeeService;

    @Override
    public List<StockQuoteDTO> findStockQuotesByCompanyName(String companyNameQuery) {
        return trading.findStockQuotesByCompanyName(companyNameQuery);
    }

    @Override
    public String whoAmI() {
        return sessionContext != null && sessionContext.getCallerPrincipal() != null
                ? sessionContext.getCallerPrincipal().getName()
                : "UNKNOWN";
    }

    @Override
    public BigDecimal buyStock(String symbol, int quantity) {
        String sym = symbol == null ? null : symbol.trim();
        String username = sessionContext.getCallerPrincipal().getName();
        CustomerEntity customer = findCustomerByUsername(username);
        return employeeService.buyStockForCustomer(customer.getId(), sym, quantity);
    }

    @Override
    public BigDecimal sellStock(String symbol, int quantity) {
        String username = sessionContext.getCallerPrincipal().getName();
        CustomerEntity customer = findCustomerByUsername(username);
        return employeeService.sellStockForCustomer(customer.getId(), symbol, quantity);
    }

    @Override
    public PortfolioDTO getMyPortfolio() {
        String username = sessionContext.getCallerPrincipal().getName();
        CustomerEntity customer = findCustomerByUsername(username);
        return employeeService.getCustomerPortfolio(customer.getId());
    }

    private CustomerEntity findCustomerByUsername(String username) {
        try {
            return em.createQuery(
                    "SELECT c FROM CustomerEntity c WHERE c.username = :username",
                    CustomerEntity.class
            ).setParameter("username", username).getSingleResult();
        } catch (NoResultException e) {
            throw new IllegalStateException("No customer found for username: " + username);
        }
    }
}
