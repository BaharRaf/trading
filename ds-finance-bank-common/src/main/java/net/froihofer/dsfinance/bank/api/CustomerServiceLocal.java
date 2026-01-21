package net.froihofer.dsfinance.bank.api;

import jakarta.ejb.Local;
import net.froihofer.dsfinance.bank.dto.CustomerDTO;
import net.froihofer.dsfinance.bank.entity.CustomerEntity;

import java.util.List;

/**
 * Local service interface for customer management operations.
 * Used internally by other EJBs.
 */
@Local
public interface CustomerServiceLocal {
    
    /**
     * Creates a new customer.
     * @param customer Customer data transfer object
     * @return The created customer's ID
     */
    long createCustomer(CustomerDTO customer);
    
    /**
     * Finds a customer by ID.
     * @param id Customer ID
     * @return Customer entity or null if not found
     */
    CustomerEntity findById(long id);
    
    /**
     * Finds a customer by customer number.
     * @param customerNumber Unique customer number
     * @return Customer entity or null if not found
     */
    CustomerEntity findByCustomerNumber(String customerNumber);
    
    /**
     * Finds a customer by username (for authentication).
     * @param username WildFly username
     * @return Customer entity or null if not found
     */
    CustomerEntity findByUsername(String username);
    
    /**
     * Searches customers by name (partial match).
     * @param firstName First name (can be partial)
     * @param lastName Last name (can be partial)
     * @return List of matching customers
     */
    List<CustomerDTO> searchByName(String firstName, String lastName);
    
    /**
     * Updates an existing customer.
     * @param customer Customer entity with updated data
     */
    void updateCustomer(CustomerEntity customer);
}
