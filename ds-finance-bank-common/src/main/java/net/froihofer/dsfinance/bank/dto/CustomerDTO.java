package net.froihofer.dsfinance.bank.dto;

import java.io.Serializable;

public class CustomerDTO implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long customerId;
  private String customerNumber;
  private String firstName;
  private String lastName;
  private String address;

  public CustomerDTO() {}

  public CustomerDTO(Long customerId, String customerNumber, String firstName, String lastName, String address) {
    this.customerId = customerId;
    this.customerNumber = customerNumber;
    this.firstName = firstName;
    this.lastName = lastName;
    this.address = address;
  }

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

  @Override
  public String toString() {
    return "CustomerDTO{id=" + customerId + ", nr='" + customerNumber + "', name='" + firstName + " " + lastName + "'}";
  }
}
