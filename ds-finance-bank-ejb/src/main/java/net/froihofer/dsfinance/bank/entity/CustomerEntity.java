package net.froihofer.dsfinance.bank.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "CUSTOMER")
@NamedQuery(
        name = "Customer.findByName",
        query = "SELECT c FROM CustomerEntity c WHERE c.firstName LIKE :first AND c.lastName LIKE :last"
)
public class CustomerEntity implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String customerNumber;

  private String firstName;
  private String lastName;
  private String address;

  @Column(unique = true)
  private String username;

  @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private DepotEntity depot;

  @Version
  private Long version;

  public CustomerEntity() {
  }

  public CustomerEntity(String customerNumber, String firstName, String lastName, String address) {
    this.customerNumber = customerNumber;
    this.firstName = firstName;
    this.lastName = lastName;
    this.address = address;
    // Generate username from customer number
    this.username = "cust_" + customerNumber;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCustomerNumber() {
    return customerNumber;
  }

  public void setCustomerNumber(String customerNumber) {
    this.customerNumber = customerNumber;
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

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public DepotEntity getDepot() {
    return depot;
  }

  public void setDepot(DepotEntity depot) {
    this.depot = depot;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
