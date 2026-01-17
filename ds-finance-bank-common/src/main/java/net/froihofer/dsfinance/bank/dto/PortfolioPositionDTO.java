package net.froihofer.dsfinance.bank.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class PortfolioPositionDTO implements Serializable {
    private String symbol;
    private String companyName;
    private Integer quantity;
    private BigDecimal averagePurchasePrice;
    private BigDecimal currentPrice;
    private BigDecimal totalValue;

    public PortfolioPositionDTO() {
    }

    public PortfolioPositionDTO(String symbol, String companyName, Integer quantity,
                                BigDecimal averagePurchasePrice, BigDecimal currentPrice,
                                BigDecimal totalValue) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.quantity = quantity;
        this.averagePurchasePrice = averagePurchasePrice;
        this.currentPrice = currentPrice;
        this.totalValue = totalValue;
    }

    // Getters and Setters
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAveragePurchasePrice() {
        return averagePurchasePrice;
    }

    public void setAveragePurchasePrice(BigDecimal averagePurchasePrice) {
        this.averagePurchasePrice = averagePurchasePrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }
}
