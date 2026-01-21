package at.ac.csw.dke.bank.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Data Transfer Object for a single position in a portfolio.
 */
public class PortfolioPositionDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String stockSymbol;
    private String companyName;
    private Integer quantity;
    private BigDecimal averagePurchasePrice;
    private BigDecimal currentPrice;
    private BigDecimal totalValue;
    private BigDecimal gainLoss;
    private BigDecimal gainLossPercent;
    
    public PortfolioPositionDTO() {
    }
    
    public PortfolioPositionDTO(String stockSymbol, String companyName, Integer quantity,
                                BigDecimal averagePurchasePrice, BigDecimal currentPrice) {
        this.stockSymbol = stockSymbol;
        this.companyName = companyName;
        this.quantity = quantity;
        this.averagePurchasePrice = averagePurchasePrice;
        this.currentPrice = currentPrice;
        calculateValues();
    }
    
    private void calculateValues() {
        if (quantity != null && currentPrice != null) {
            this.totalValue = currentPrice.multiply(new BigDecimal(quantity));
        }
        if (averagePurchasePrice != null && currentPrice != null) {
            this.gainLoss = currentPrice.subtract(averagePurchasePrice);
            if (averagePurchasePrice.compareTo(BigDecimal.ZERO) > 0) {
                this.gainLossPercent = gainLoss.divide(averagePurchasePrice, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal(100));
            }
        }
    }
    
    // Getters and Setters
    public String getStockSymbol() {
        return stockSymbol;
    }
    
    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
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
        calculateValues();
    }
    
    public BigDecimal getAveragePurchasePrice() {
        return averagePurchasePrice;
    }
    
    public void setAveragePurchasePrice(BigDecimal averagePurchasePrice) {
        this.averagePurchasePrice = averagePurchasePrice;
        calculateValues();
    }
    
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    
    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
        calculateValues();
    }
    
    public BigDecimal getTotalValue() {
        return totalValue;
    }
    
    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }
    
    public BigDecimal getGainLoss() {
        return gainLoss;
    }
    
    public void setGainLoss(BigDecimal gainLoss) {
        this.gainLoss = gainLoss;
    }
    
    public BigDecimal getGainLossPercent() {
        return gainLossPercent;
    }
    
    public void setGainLossPercent(BigDecimal gainLossPercent) {
        this.gainLossPercent = gainLossPercent;
    }
    
    @Override
    public String toString() {
        return "PortfolioPositionDTO{" +
                "stockSymbol='" + stockSymbol + '\'' +
                ", quantity=" + quantity +
                ", averagePurchasePrice=" + averagePurchasePrice +
                ", currentPrice=" + currentPrice +
                ", totalValue=" + totalValue +
                '}';
    }
}
