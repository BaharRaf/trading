package at.ac.csw.dke.bank.ejb;
import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import net.froihofer.dsfinance.bank.exception.InsufficientFundsException;
import net.froihofer.dsfinance.bank.exception.InsufficientSharesException;
import net.froihofer.dsfinance.bank.service.TradingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * EJB implementation for stock trading operations.
 * Provides separate methods for employee and customer trading.
 * 
 * IMPORTANT: Customer identity is obtained from SessionContext, not parameters!
 */
@Stateless
@DeclareRoles({"employee", "customer"})
public class TradingServiceBean implements TradingService {
    
    private static final Logger logger = LoggerFactory.getLogger(TradingServiceBean.class);
    
    @Resource
    private SessionContext sessionContext;
    
    // TODO: Inject other required services
    
    @Override
    @RolesAllowed("employee")
    public BigDecimal buyStockForCustomer(long customerId, String symbol, int shares) 
            throws InsufficientFundsException {
        String employeeUsername = sessionContext.getCallerPrincipal().getName();
        logger.info("Employee '{}' buying {} shares of {} for customer ID {}", 
                    employeeUsername, shares, symbol, customerId);
        
        // TODO: Implement buy logic
        // 1. Check bank has enough volume
        // 2. Call external TradingService
        // 3. Update customer depot
        // 4. Update bank volume
        // 5. Record transaction with employeeUsername
        
        throw new UnsupportedOperationException("Implementation pending");
    }
    
    @Override
    @RolesAllowed("employee")
    public BigDecimal sellStockForCustomer(long customerId, String symbol, int shares) 
            throws InsufficientSharesException {
        String employeeUsername = sessionContext.getCallerPrincipal().getName();
        logger.info("Employee '{}' selling {} shares of {} for customer ID {}", 
                    employeeUsername, shares, symbol, customerId);
        
        // TODO: Implement sell logic
        throw new UnsupportedOperationException("Implementation pending");
    }
    
    @Override
    @RolesAllowed("customer")
    public BigDecimal buyStock(String symbol, int shares) throws InsufficientFundsException {
        // CRITICAL: Get customer identity from SessionContext - NOT from parameter!
        String customerUsername = sessionContext.getCallerPrincipal().getName();
        logger.info("Customer '{}' buying {} shares of {}", customerUsername, shares, symbol);
        
        // TODO: 
        // 1. Lookup customer by username
        // 2. Execute buy operation
        // Note: Customer can only trade for themselves!
        
        throw new UnsupportedOperationException("Implementation pending");
    }
    
    @Override
    @RolesAllowed("customer")
    public BigDecimal sellStock(String symbol, int shares) throws InsufficientSharesException {
        // CRITICAL: Get customer identity from SessionContext - NOT from parameter!
        String customerUsername = sessionContext.getCallerPrincipal().getName();
        logger.info("Customer '{}' selling {} shares of {}", customerUsername, shares, symbol);
        
        // TODO: Implement customer sell logic
        throw new UnsupportedOperationException("Implementation pending");
    }
}
