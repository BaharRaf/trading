package at.ac.csw.dke.bank.dto;

import java.io.Serializable;

/**
 * Data Transfer Object for person information.
 * Used when creating new customers.
 */
public class PersonDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String firstName;
    private String lastName;
    private String address;
    private String loginUsername;  // For WildFly authentication
    private String loginPassword;  // For WildFly authentication
    
    public PersonDTO() {
    }
    
    public PersonDTO(String firstName, String lastName, String address, 
                     String loginUsername, String loginPassword) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.loginUsername = loginUsername;
        this.loginPassword = loginPassword;
    }
    
    // Getters and Setters
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
    
    public String getLoginUsername() {
        return loginUsername;
    }
    
    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }
    
    public String getLoginPassword() {
        return loginPassword;
    }
    
    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }
    
    @Override
    public String toString() {
        return "PersonDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", address='" + address + '\'' +
                ", loginUsername='" + loginUsername + '\'' +
                '}';
    }
}
