package at.ac.csw.dke.bank.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for customer information.
 */
public class CustomerDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long customerId;
    private String firstName;
    private String lastName;
    private String address;
    private String customerNumber;
    private LocalDateTime createdAt;
    
    public CustomerDTO() {
    }
    
    public CustomerDTO(Long customerId, String firstName, String lastName, 
                       String address, String customerNumber, LocalDateTime createdAt) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.customerNumber = customerNumber;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCustomerNumber() {
        return customerNumber;
    }
    
    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "CustomerDTO{" +
                "customerId=" + customerId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", customerNumber='" + customerNumber + '\'' +
                '}';
    }
}
