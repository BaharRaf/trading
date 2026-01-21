package net.froihofer.dsfinance.bank.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class StockQuoteDTO implements Serializable {
  private static final long serialVersionUID = 1L;

  private String symbol;
  private String companyName;
  private BigDecimal price;
  private Instant timestamp;

  public StockQuoteDTO() {}

  public StockQuoteDTO(String symbol, String companyName, BigDecimal price, Instant timestamp) {
    this.symbol = symbol;
    this.companyName = companyName;
    this.price = price;
    this.timestamp = timestamp;
  }

  public String getSymbol() { return symbol; }
  public void setSymbol(String symbol) { this.symbol = symbol; }

  public String getCompanyName() { return companyName; }
  public void setCompanyName(String companyName) { this.companyName = companyName; }

  public BigDecimal getPrice() { return price; }
  public void setPrice(BigDecimal price) { this.price = price; }

  public Instant getTimestamp() { return timestamp; }
  public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

  @Override
  public String toString() {
    return "StockQuoteDTO{symbol='" + symbol + "', companyName='" + companyName + "', price=" + price + ", ts=" + timestamp + "}";
  }
}
