package net.froihofer.dsfinance.bank.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CUSTOMER")
@NamedQueries({
    @NamedQuery(name = "Customer.findByName",
        query = "SELECT c FROM CustomerEntity c WHERE " +
                "(:first IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :first, '%'))) AND " +
                "(:last  IS NULL OR LOWER(c.lastName)  LIKE LOWER(CONCAT('%', :last,  '%')))"
    )
})
public class CustomerEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "CUSTOMER_NUMBER", nullable = false, unique = true, length = 32)
  private String customerNumber;

  @Column(name = "FIRST_NAME", nullable = false, length = 80)
  private String firstName;

  @Column(name = "LAST_NAME", nullable = false, length = 80)
  private String lastName;

  @Column(name = "ADDRESS", nullable = false, length = 255)
  private String address;

  public CustomerEntity() {}

  public CustomerEntity(String customerNumber, String firstName, String lastName, String address) {
    this.customerNumber = customerNumber;
    this.firstName = firstName;
    this.lastName = lastName;
    this.address = address;
  }

  public Long getId() { return id; }

  public String getCustomerNumber() { return customerNumber; }
  public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }

  public String getFirstName() { return firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }

  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }

  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }
}
