package net.froihofer.dsfinance.bank.ejb;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import net.froihofer.dsfinance.bank.api.CustomerServiceLocal;
import net.froihofer.dsfinance.bank.dto.CustomerDTO;
import net.froihofer.dsfinance.bank.entity.CustomerEntity;
import net.froihofer.util.jboss.WildflyAuthDBHelper;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dedicated service bean for customer management.
 * Handles all customer-related CRUD operations.
 */
@Stateless
public class CustomerServiceBean implements CustomerServiceLocal {

    @PersistenceContext
    private EntityManager em;

    @Override
    public long createCustomer(CustomerDTO customer) {
        if (customer == null) {
            throw new IllegalArgumentException("customer must not be null");
        }

        if (customer.getCustomerNumber() == null || customer.getCustomerNumber().isBlank()) {
            throw new IllegalArgumentException("customerNumber must not be blank");
        }

        // Username MUST match WildFly login principal
        String username = customer.getUsername();
        if (username == null || username.isBlank()) {
            username = "cust-" + customer.getCustomerNumber();
        }

        CustomerEntity entity = new CustomerEntity(
                customer.getCustomerNumber(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getAddress()
        );
        entity.setUsername(username);

        em.persist(entity);
        em.flush();

        // Create corresponding WildFly user
        String initialPassword = customer.getInitialPassword();
        if (initialPassword != null && !initialPassword.isBlank()) {
            try {
                File jbossHome = new File(System.getProperty("jboss.home.dir"));
                new WildflyAuthDBHelper(jbossHome).addUser(
                    username, 
                    initialPassword, 
                    new String[]{"customer"}
                );
            } catch (Exception e) {
                throw new IllegalStateException(
                    "Customer created in DB but WildFly user creation failed for username=" + username, 
                    e
                );
            }
        }

        return entity.getId();
    }

    @Override
    public CustomerEntity findById(long id) {
        return em.find(CustomerEntity.class, id);
    }

    @Override
    public CustomerEntity findByCustomerNumber(String customerNumber) {
        if (customerNumber == null || customerNumber.isBlank()) {
            return null;
        }

        try {
            return em.createQuery(
                    "SELECT c FROM CustomerEntity c WHERE c.customerNumber = :number", 
                    CustomerEntity.class
                )
                .setParameter("number", customerNumber)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public CustomerEntity findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }

        try {
            return em.createQuery(
                    "SELECT c FROM CustomerEntity c WHERE c.username = :username", 
                    CustomerEntity.class
                )
                .setParameter("username", username)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<CustomerDTO> searchByName(String firstName, String lastName) {
        List<CustomerEntity> results = em.createNamedQuery(
                "Customer.findByName", 
                CustomerEntity.class
            )
            .setParameter("first", firstName)
            .setParameter("last", lastName)
            .getResultList();
        
        return results.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public void updateCustomer(CustomerEntity customer) {
        if (customer == null || customer.getId() == null) {
            throw new IllegalArgumentException("Customer and customer ID must not be null");
        }
        
        em.merge(customer);
    }

    // Helper method
    private CustomerDTO toDto(CustomerEntity c) {
        return new CustomerDTO(
            c.getId(),
            c.getCustomerNumber(),
            c.getFirstName(),
            c.getLastName(),
            c.getAddress(),
            c.getUsername()
        );
    }
}
