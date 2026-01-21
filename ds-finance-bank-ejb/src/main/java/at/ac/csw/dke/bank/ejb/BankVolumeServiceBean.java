package at.ac.csw.dke.bank.ejb;

import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import net.froihofer.dsfinance.bank.service.BankVolumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * EJB implementation for bank volume management.
 * Only accessible to employees.
 */
@Stateless
@DeclareRoles({"employee", "customer"})
public class BankVolumeServiceBean implements BankVolumeService {
    
    private static final Logger logger = LoggerFactory.getLogger(BankVolumeServiceBean.class);
    
    @Resource
    private SessionContext sessionContext;
    
    @Override
    @RolesAllowed("employee")
    public BigDecimal getAvailableTradingBudget() {
        String employeeUsername = sessionContext.getCallerPrincipal().getName();
        logger.debug("Employee '{}' checking available trading budget", employeeUsername);
        
        // TODO: Load Bank entity and return availableVolume
        throw new UnsupportedOperationException("Implementation pending");
    }
    
    @Override
    @RolesAllowed("employee")
    public BigDecimal getTotalMarketValueAcrossAllDepots() {
        String employeeUsername = sessionContext.getCallerPrincipal().getName();
        logger.debug("Employee '{}' checking total market value", employeeUsername);
        
        // TODO: Sum all depot position values across all customers
        throw new UnsupportedOperationException("Implementation pending");
    }
    
    @Override
    @RolesAllowed("employee")
    public BigDecimal getTotalInvestableVolume() {
        String employeeUsername = sessionContext.getCallerPrincipal().getName();
        logger.debug("Employee '{}' checking total investable volume", employeeUsername);
        
        // TODO: Load Bank entity and return totalInvestableVolume
        throw new UnsupportedOperationException("Implementation pending");
    }
}
