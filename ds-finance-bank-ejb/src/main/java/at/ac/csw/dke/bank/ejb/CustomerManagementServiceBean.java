  // ← FIX: Changed package

import net.froihofer.dsfinance.bank.service.CustomerManagementService;  // ← ADD
import net.froihofer.dsfinance.bank.dto.PersonDTO;  // ← ADD
import net.froihofer.dsfinance.bank.dto.CustomerDTO;  // ← ADD

import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * EJB implementation for customer management.
 * Only accessible to employees.
 *
 * IMPORTANT: No login() method - authentication is container-managed!
 */
@Stateless
@DeclareRoles({"employee", "customer"})
public class CustomerManagementServiceBean implements CustomerManagementService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerManagementServiceBean.class);

    @Resource
    private SessionContext sessionContext;

    // TODO: Inject other required services (CustomerService, etc.)

    @Override
    @RolesAllowed("employee")
    public long createCustomer(PersonDTO person) {
        // Get authenticated employee username from SessionContext
        String employeeUsername = sessionContext.getCallerPrincipal().getName();
        logger.info("Employee '{}' creating new customer: {} {}",
                employeeUsername, person.getFirstName(), person.getLastName());

        // TODO: Implement customer creation logic
        // 1. Create CustomerEntity
        // 2. Generate customerNumber
        // 3. Create WildFly user for customer authentication
        // 4. Create Depot for customer

        throw new UnsupportedOperationException("Implementation pending");
    }

    @Override
    @RolesAllowed("employee")
    public CustomerDTO findCustomer(long customerId) {
        String employeeUsername = sessionContext.getCallerPrincipal().getName();
        logger.debug("Employee '{}' searching for customer ID: {}", employeeUsername, customerId);

        // TODO: Implement customer lookup
        throw new UnsupportedOperationException("Implementation pending");
    }

    @Override
    @RolesAllowed("employee")
    public List<CustomerDTO> findCustomersByName(String firstName, String lastName) {
        String employeeUsername = sessionContext.getCallerPrincipal().getName();
        logger.debug("Employee '{}' searching for customers: {} {}",
                employeeUsername, firstName, lastName);

        // TODO: Implement customer search by name
        throw new UnsupportedOperationException("Implementation pending");
    }

    @Override
    @RolesAllowed("employee")
    public CustomerDTO findCustomerByNumber(String customerNumber) {
        String employeeUsername = sessionContext.getCallerPrincipal().getName();
        logger.debug("Employee '{}' searching for customer number: {}",
                employeeUsername, customerNumber);

        // TODO: Implement customer lookup by number
        throw new UnsupportedOperationException("Implementation pending");
    }
}
