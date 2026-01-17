package net.froihofer.dsfinance.bank.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "BANK")
public class BankEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal totalInvestableVolume;

    private BigDecimal availableVolume;

    @Version
    private Long version;

    public BankEntity() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getTotalInvestableVolume() {
        return totalInvestableVolume;
    }

    public void setTotalInvestableVolume(BigDecimal totalInvestableVolume) {
        this.totalInvestableVolume = totalInvestableVolume;
    }

    public BigDecimal getAvailableVolume() {
        return availableVolume;
    }

    public void setAvailableVolume(BigDecimal availableVolume) {
        this.availableVolume = availableVolume;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
