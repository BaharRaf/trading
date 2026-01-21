package net.froihofer.dsfinance.bank.service;

import jakarta.ejb.Remote;
import net.froihofer.dsfinance.bank.dto.CustomerDTO;
import net.froihofer.dsfinance.bank.dto.PersonDTO;

import java.util.List;

/**
 * Remote interface for customer management operations.
 * Only accessible to employees.
 * 
 * Authentication is handled by WildFly container - no login() method needed.
 * All methods require the caller to have the "employee" role.
 */
@Remote
public interface CustomerManagementService {
    
    /**
     * Creates a new customer account.
     * Automatically creates corresponding WildFly user for authentication.
     * 
     * @param person Customer data including name, address
     * @return The ID of the newly created customer
     * @throws IllegalArgumentException if person data is invalid
     */
    long createCustomer(PersonDTO person);
    
    /**
     * Finds a customer by their unique customer ID.
     * 
     * @param customerId The customer ID
     * @return CustomerDTO with customer details, or null if not found
     */
    CustomerDTO findCustomer(long customerId);
    
    /**
     * Searches for customers by name.
     * Returns all customers matching the given first and/or last name.
     * Either parameter can be null to search only by the other.
     * 
     * @param firstName First name to search for (can be null)
     * @param lastName Last name to search for (can be null)
     * @return List of matching customers, empty list if none found
     */
    List<CustomerDTO> findCustomersByName(String firstName, String lastName);
    
    /**
     * Searches for customers by customer number.
     * 
     * @param customerNumber The unique customer number
     * @return CustomerDTO if found, null otherwise
     */
    CustomerDTO findCustomerByNumber(String customerNumber);
}
