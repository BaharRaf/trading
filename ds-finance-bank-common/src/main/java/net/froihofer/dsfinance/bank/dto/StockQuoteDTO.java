package net.froihofer.dsfinance.bank.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class StockQuoteDTO implements Serializable {
  private String symbol;
  private String companyName;
  private BigDecimal lastTradePrice;
  private BigDecimal change;
  private String exchange;

  public StockQuoteDTO() {
  }

  public StockQuoteDTO(String symbol, String companyName, BigDecimal lastTradePrice) {
    this.symbol = symbol;
    this.companyName = companyName;
    this.lastTradePrice = lastTradePrice;
  }

  public StockQuoteDTO(String symbol, String companyName, BigDecimal lastTradePrice,
                       BigDecimal change, String exchange) {
    this.symbol = symbol;
    this.companyName = companyName;
    this.lastTradePrice = lastTradePrice;
    this.change = change;
    this.exchange = exchange;
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

  public BigDecimal getLastTradePrice() {
    return lastTradePrice;
  }

  public void setLastTradePrice(BigDecimal lastTradePrice) {
    this.lastTradePrice = lastTradePrice;
  }

  public BigDecimal getChange() {
    return change;
  }

  public void setChange(BigDecimal change) {
    this.change = change;
  }

  public String getExchange() {
    return exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }

  @Override
  public String toString() {
    return "StockQuoteDTO{" +
            "symbol='" + symbol + '\'' +
            ", companyName='" + companyName + '\'' +
            ", lastTradePrice=" + lastTradePrice +
            ", change=" + change +
            ", exchange='" + exchange + '\'' +
            '}';
  }
}
