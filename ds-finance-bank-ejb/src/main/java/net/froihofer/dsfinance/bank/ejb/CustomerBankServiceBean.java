package net.froihofer.dsfinance.bank.ejb;

import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJB;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.List;

import net.froihofer.dsfinance.bank.api.CustomerBankService;
import net.froihofer.dsfinance.bank.api.CustomerServiceLocal;
import net.froihofer.dsfinance.bank.api.DepotServiceLocal;
import net.froihofer.dsfinance.bank.api.EmployeeBankService;
import net.froihofer.dsfinance.bank.dto.PortfolioDTO;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;
import net.froihofer.dsfinance.bank.entity.CustomerEntity;

/**
 * Customer bank service with security context validation.
 * Ensures customers can only access their own accounts.
 */
@Stateless(name = "CustomerBankServiceBean")
@RolesAllowed("customer")
@RunAs("employee")
@DeclareRoles({"customer", "employee"})
public class CustomerBankServiceBean implements CustomerBankService {

    @EJB
    private TradingServiceAdapterBean trading;

    @EJB
    private CustomerServiceLocal customerService;

    @EJB
    private DepotServiceLocal depotService;

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
        
        // Get authenticated customer
        CustomerEntity customer = getAuthenticatedCustomer();
        
        // Customers can only buy for themselves (validation happens here)
        validateCustomerAccess(customer.getId());
        
        return employeeService.buyStockForCustomer(customer.getId(), sym, quantity);
    }

    @Override
    public BigDecimal sellStock(String symbol, int quantity) {
        // Get authenticated customer
        CustomerEntity customer = getAuthenticatedCustomer();
        
        // Customers can only sell from their own account
        validateCustomerAccess(customer.getId());
        
        return employeeService.sellStockForCustomer(customer.getId(), symbol, quantity);
    }

    @Override
    public PortfolioDTO getMyPortfolio() {
        // Get authenticated customer
        CustomerEntity customer = getAuthenticatedCustomer();
        
        // Use depot service for portfolio retrieval
        return depotService.getCustomerPortfolio(customer.getId());
    }

    /**
     * Gets the currently authenticated customer from the security context.
     * @return Customer entity for the authenticated user
     * @throws IllegalStateException if customer not found
     */
    private CustomerEntity getAuthenticatedCustomer() {
        String username = sessionContext.getCallerPrincipal().getName();
        CustomerEntity customer = customerService.findByUsername(username);
        
        if (customer == null) {
            throw new IllegalStateException("No customer found for username: " + username);
        }
        
        return customer;
    }

    /**
     * Validates that the currently authenticated user is accessing their own account.
     * This prevents customers from manipulating client code to access other accounts.
     * 
     * @param customerId The customer ID being accessed
     * @throws SecurityException if customer tries to access another account
     */
    private void validateCustomerAccess(long customerId) {
        if (sessionContext.isCallerInRole("customer")) {
            String username = sessionContext.getCallerPrincipal().getName();
            CustomerEntity authenticatedCustomer = customerService.findByUsername(username);
            
            if (authenticatedCustomer == null) {
                throw new SecurityException("Customer not found for username: " + username);
            }
            
            if (authenticatedCustomer.getId() != customerId) {
                throw new SecurityException(
                    "Access denied: Customer can only access own account. " +
                    "Authenticated ID: " + authenticatedCustomer.getId() + 
                    ", Requested ID: " + customerId
                );
            }
        }
        // Employees can access any customer account - no validation needed
    }
}
