package net.froihofer.dsfinance.bank.entity;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Depot entity representing a customer's securities account.
 * 
 * PROFESSOR FEEDBACK APPLIED:
 * - NO List<DepotPositionEntity> here!
 * - "For one-to-many relationships that can grow large, having a list on the 
 *    'one' side that contains all 'many' elements is problematic."
 * - "Better modeling: use unidirectional associations from the many side to the one side."
 * - Positions are queried via: SELECT p FROM DepotPositionEntity p WHERE p.depot.id = :depotId
 */
@Entity
@Table(name = "DEPOT")
@NamedQueries({
    @NamedQuery(
        name = "Depot.findByCustomerId",
        query = "SELECT d FROM DepotEntity d WHERE d.customer.id = :customerId"
    )
})
public class DepotEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    /*
     * REMOVED per professor feedback:
     * @OneToMany(mappedBy = "depot", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
     * private List<DepotPositionEntity> positions = new ArrayList<>();
     * 
     * Instead, use query: "SELECT p FROM DepotPositionEntity p WHERE p.depot.id = :depotId"
     */

    @Version
    private Long version;

    public DepotEntity() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerEntity customer) {
        this.customer = customer;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
