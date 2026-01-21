package net.froihofer.dsfinance.bank.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DEPOT")
public class DepotEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @OneToMany(mappedBy = "depot", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<DepotPositionEntity> positions = new ArrayList<>();

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

    public List<DepotPositionEntity> getPositions() {
        return positions;
    }

    public void setPositions(List<DepotPositionEntity> positions) {
        this.positions = positions;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
