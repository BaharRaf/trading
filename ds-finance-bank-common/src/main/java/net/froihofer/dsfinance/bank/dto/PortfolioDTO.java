package net.froihofer.dsfinance.bank.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class PortfolioDTO implements Serializable {
    private Long customerId;
    private List<PortfolioPositionDTO> positions;
    private BigDecimal totalValue;

    public PortfolioDTO() {
    }

    public PortfolioDTO(Long customerId, List<PortfolioPositionDTO> positions, BigDecimal totalValue) {
        this.customerId = customerId;
        this.positions = positions;
        this.totalValue = totalValue;
    }

    // Getters and Setters
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<PortfolioPositionDTO> getPositions() {
        return positions;
    }

    public void setPositions(List<PortfolioPositionDTO> positions) {
        this.positions = positions;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }
}
