package at.ac.csw.dke.bank.ejb;

import at.ac.csw.dke.bank.dto.PortfolioDTO;
import at.ac.csw.dke.bank.service.PortfolioService;
import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EJB implementation for portfolio viewing.
 * Demonstrates separation between employee and customer operations.
 */
@Stateless
@DeclareRoles({"employee", "customer"})
public class PortfolioServiceBean implements PortfolioService {
    
    private static final Logger logger = LoggerFactory.getLogger(PortfolioServiceBean.class);
    
    @Resource
    private SessionContext sessionContext;
    
    @Override
    @RolesAllowed("employee")
    public PortfolioDTO getCustomerPortfolio(long customerId) {
        String employeeUsername = sessionContext.getCallerPrincipal().getName();
        logger.info("Employee '{}' viewing portfolio for customer ID {}", 
                    employeeUsername, customerId);
        
        // TODO: Implement portfolio retrieval
        // 1. Load customer's depot
        // 2. Load all depot positions
        // 3. Get current prices from TradingService
        // 4. Calculate total value
        
        throw new UnsupportedOperationException("Implementation pending");
    }
    
    @Override
    @RolesAllowed("customer")
    public PortfolioDTO getMyPortfolio() {
        // CRITICAL: Get customer identity from SessionContext!
        String customerUsername = sessionContext.getCallerPrincipal().getName();
        logger.info("Customer '{}' viewing own portfolio", customerUsername);
        
        // TODO:
        // 1. Lookup customer by username
        // 2. Load their depot and positions
        // 3. Return portfolio data
        
        throw new UnsupportedOperationException("Implementation pending");
    }
}
