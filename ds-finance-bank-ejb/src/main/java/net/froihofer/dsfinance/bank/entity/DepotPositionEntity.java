package net.froihofer.dsfinance.bank.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Depot position entity representing a holding of a specific stock.
 * 
 * PROFESSOR FEEDBACK APPLIED:
 * - This entity points TO Depot (Many-to-One) - unidirectional from many side
 * - Depot does NOT have a list pointing back here
 * - Use NamedQueries to find positions for a depot
 */
@Entity
@Table(name = "DEPOT_POSITION")
@NamedQueries({
    @NamedQuery(
        name = "DepotPosition.findByDepotId",
        query = "SELECT p FROM DepotPositionEntity p WHERE p.depot.id = :depotId"
    ),
    @NamedQuery(
        name = "DepotPosition.findByDepotAndStock",
        query = "SELECT p FROM DepotPositionEntity p WHERE p.depot.id = :depotId AND p.stock.id = :stockId"
    ),
    @NamedQuery(
        name = "DepotPosition.findByDepotAndSymbol",
        query = "SELECT p FROM DepotPositionEntity p WHERE p.depot.id = :depotId AND p.stock.symbol = :symbol"
    )
})
public class DepotPositionEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "depot_id")
    private DepotEntity depot;

    @ManyToOne
    @JoinColumn(name = "stock_id")
    private StockEntity stock;

    private Integer quantity;

    private BigDecimal averagePurchasePrice;

    @Version
    private Long version;

    public DepotPositionEntity() {
    }

    /**
     * Adds more shares and recalculates the weighted average purchase price.
     * Formula: new_avg = (old_qty * old_avg + additional_qty * new_price) / (old_qty + additional_qty)
     * 
     * @param additionalQuantity Number of shares to add
     * @param newPrice Price per share of the new purchase
     */
    public void addQuantity(int additionalQuantity, BigDecimal newPrice) {
        if (additionalQuantity <= 0) {
            throw new IllegalArgumentException("Additional quantity must be positive");
        }
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("New price must be positive");
        }

        int oldQty = (this.quantity != null) ? this.quantity : 0;
        BigDecimal oldAvg = (this.averagePurchasePrice != null) ? this.averagePurchasePrice : BigDecimal.ZERO;

        // Calculate weighted average price
        // Total value of existing shares
        BigDecimal totalOldValue = oldAvg.multiply(new BigDecimal(oldQty));
        
        // Value of new shares
        BigDecimal totalNewValue = newPrice.multiply(new BigDecimal(additionalQuantity));
        
        // New total quantity
        int newTotalQty = oldQty + additionalQuantity;
        
        // New weighted average price
        BigDecimal newAvgPrice = totalOldValue.add(totalNewValue)
            .divide(new BigDecimal(newTotalQty), 4, RoundingMode.HALF_UP);

        this.quantity = newTotalQty;
        this.averagePurchasePrice = newAvgPrice;
    }

    /**
     * Removes shares from the position. Does not recalculate average price.
     * 
     * @param quantityToRemove Number of shares to remove
     * @throws IllegalArgumentException if trying to remove more shares than owned
     */
    public void removeQuantity(int quantityToRemove) {
        if (quantityToRemove <= 0) {
            throw new IllegalArgumentException("Quantity to remove must be positive");
        }
        
        int currentQty = (this.quantity != null) ? this.quantity : 0;
        
        if (currentQty < quantityToRemove) {
            throw new IllegalArgumentException(
                "Cannot remove " + quantityToRemove + " shares. Only " + currentQty + " available."
            );
        }
        
        this.quantity = currentQty - quantityToRemove;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DepotEntity getDepot() {
        return depot;
    }

    public void setDepot(DepotEntity depot) {
        this.depot = depot;
    }

    public StockEntity getStock() {
        return stock;
    }

    public void setStock(StockEntity stock) {
        this.stock = stock;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
