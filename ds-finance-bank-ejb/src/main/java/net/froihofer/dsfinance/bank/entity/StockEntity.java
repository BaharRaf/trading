package net.froihofer.dsfinance.bank.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "STOCK")
public class StockEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String symbol;

    private String companyName;

    @Version
    private Long version;

    public StockEntity() {
    }

    public StockEntity(String symbol, String companyName) {
        this.symbol = symbol;
        this.companyName = companyName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
