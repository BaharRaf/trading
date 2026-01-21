package at.ac.csw.dke.bank.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Data Transfer Object for stock information.
 */
public class StockDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long stockId;
    private String symbol;
    private String companyName;
    private BigDecimal currentPrice;
    private BigDecimal dayChange;
    private BigDecimal dayChangePercent;
    
    public StockDTO() {
    }
    
    public StockDTO(Long stockId, String symbol, String companyName, BigDecimal currentPrice) {
        this.stockId = stockId;
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
    }
    
    // Getters and Setters
    public Long getStockId() {
        return stockId;
    }
    
    public void setStockId(Long stockId) {
        this.stockId = stockId;
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
    
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    
    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
    
    public BigDecimal getDayChange() {
        return dayChange;
    }
    
    public void setDayChange(BigDecimal dayChange) {
        this.dayChange = dayChange;
    }
    
    public BigDecimal getDayChangePercent() {
        return dayChangePercent;
    }
    
    public void setDayChangePercent(BigDecimal dayChangePercent) {
        this.dayChangePercent = dayChangePercent;
    }
    
    @Override
    public String toString() {
        return "StockDTO{" +
                "symbol='" + symbol + '\'' +
                ", companyName='" + companyName + '\'' +
                ", currentPrice=" + currentPrice +
                '}';
    }
}
