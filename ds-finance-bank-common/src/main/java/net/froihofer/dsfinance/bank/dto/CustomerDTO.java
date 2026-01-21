package net.froihofer.dsfinance.bank.dto;

import java.io.Serializable;

public class CustomerDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long customerId;
    private String customerNumber;
    private String firstName;
    private String lastName;
    private String address;

    // NEW: must match WildFly login principal
    private String username;

    // NEW: optional - only used when creating the WildFly account from server side
    private String initialPassword;

    public CustomerDTO() {}

    public CustomerDTO(Long customerId, String customerNumber, String firstName, String lastName, String address) {
        this.customerId = customerId;
        this.customerNumber = customerNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }

    public CustomerDTO(Long customerId, String customerNumber, String firstName, String lastName, String address, String username) {
        this(customerId, customerNumber, firstName, lastName, address);
        this.username = username;
    }

    // Alias methods for getId/setId (used by service beans)
    public Long getId() { return customerId; }
    public void setId(Long id) { this.customerId = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getInitialPassword() { return initialPassword; }
    public void setInitialPassword(String initialPassword) { this.initialPassword = initialPassword; }

    @Override
    public String toString() {
        return "CustomerDTO{id=" + customerId
                + ", nr='" + customerNumber
                + "', name='" + firstName + " " + lastName
                + "', username='" + username
                + "'}";
    }
}