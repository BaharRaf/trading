package at.ac.csw.dke.bank.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for portfolio information.
 * Contains all positions and total value.
 */
public class PortfolioDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long depotId;
    private Long customerId;
    private String customerName;
    private List<PortfolioPositionDTO> positions = new ArrayList<>();
    private BigDecimal totalValue;
    
    public PortfolioDTO() {
    }
    
    public PortfolioDTO(Long depotId, Long customerId, String customerName) {
        this.depotId = depotId;
        this.customerId = customerId;
        this.customerName = customerName;
    }
    
    // Getters and Setters
    public Long getDepotId() {
        return depotId;
    }
    
    public void setDepotId(Long depotId) {
        this.depotId = depotId;
    }
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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
    
    @Override
    public String toString() {
        return "PortfolioDTO{" +
                "customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", positions=" + positions.size() +
                ", totalValue=" + totalValue +
                '}';
    }
}
