package net.froihofer.dsfinance.bank.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class PortfolioPositionDTO implements Serializable {
    private String symbol;
    private String companyName;
    private Integer quantity;
    private BigDecimal averagePurchasePrice;
    private BigDecimal currentPrice;
    private BigDecimal totalValue;           // Current market value (quantity * currentPrice)
    private BigDecimal purchaseValue;        // Original purchase value (quantity * averagePurchasePrice)
    private BigDecimal profitLoss;           // Profit or loss (totalValue - purchaseValue)

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
        
        // Calculate purchase value and profit/loss
        if (quantity != null && averagePurchasePrice != null) {
            this.purchaseValue = averagePurchasePrice.multiply(new BigDecimal(quantity));
            
            if (totalValue != null) {
                this.profitLoss = totalValue.subtract(this.purchaseValue);
            }
        }
    }

    /**
     * Full constructor with all fields including calculated values.
     */
    public PortfolioPositionDTO(String symbol, String companyName, Integer quantity,
                                BigDecimal averagePurchasePrice, BigDecimal currentPrice,
                                BigDecimal totalValue, BigDecimal purchaseValue,
                                BigDecimal profitLoss) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.quantity = quantity;
        this.averagePurchasePrice = averagePurchasePrice;
        this.currentPrice = currentPrice;
        this.totalValue = totalValue;
        this.purchaseValue = purchaseValue;
        this.profitLoss = profitLoss;
    }

    /**
     * Recalculates purchase value and profit/loss based on current field values.
     * Call this after setting quantity, averagePurchasePrice, or totalValue.
     */
    public void recalculateValues() {
        if (quantity != null && averagePurchasePrice != null) {
            this.purchaseValue = averagePurchasePrice.multiply(new BigDecimal(quantity));
            
            if (totalValue != null) {
                this.profitLoss = totalValue.subtract(this.purchaseValue);
            }
        }
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

    public BigDecimal getPurchaseValue() {
        return purchaseValue;
    }

    public void setPurchaseValue(BigDecimal purchaseValue) {
        this.purchaseValue = purchaseValue;
    }

    public BigDecimal getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(BigDecimal profitLoss) {
        this.profitLoss = profitLoss;
    }

    @Override
    public String toString() {
        return "PortfolioPositionDTO{" +
                "symbol='" + symbol + '\'' +
                ", companyName='" + companyName + '\'' +
                ", quantity=" + quantity +
                ", averagePurchasePrice=" + averagePurchasePrice +
                ", currentPrice=" + currentPrice +
                ", totalValue=" + totalValue +
                ", purchaseValue=" + purchaseValue +
                ", profitLoss=" + profitLoss +
                '}';
    }
}
