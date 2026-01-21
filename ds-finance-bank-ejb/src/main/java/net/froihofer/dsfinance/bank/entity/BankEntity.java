package net.froihofer.dsfinance.bank.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "BANK")
public class BankEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal totalInvestableVolume;

    private BigDecimal availableVolume;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @Version
    private Long version;

    public BankEntity() {
    }

    /**
     * Decreases the available investment volume.
     * @param amount Amount to decrease
     * @throws IllegalStateException if insufficient funds
     */
    public void decreaseVolume(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        if (this.availableVolume == null) {
            this.availableVolume = BigDecimal.ZERO;
        }
        
        if (this.availableVolume.compareTo(amount) < 0) {
            throw new IllegalStateException(
                "Insufficient bank investment volume. Available: " + 
                this.availableVolume + ", Requested: " + amount
            );
        }
        
        this.availableVolume = this.availableVolume.subtract(amount);
        this.lastUpdated = new Date();
    }

    /**
     * Increases the available investment volume (when stocks are sold).
     * @param amount Amount to increase
     */
    public void increaseVolume(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        if (this.availableVolume == null) {
            this.availableVolume = BigDecimal.ZERO;
        }
        
        this.availableVolume = this.availableVolume.add(amount);
        this.lastUpdated = new Date();
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

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
