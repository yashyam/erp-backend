package com.ginning.erp.bale.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class BaleProductionRequest {
    private UUID lotId;

    @NotBlank(message = "Lot number is required")
    private String lotNumber;

    @NotNull(message = "Production date is required")
    private LocalDate productionDate;

    @NotNull(message = "Bale weight is required")
    @DecimalMin(value = "0.001", message = "Bale weight must be greater than zero")
    private BigDecimal baleWeight;

    private BigDecimal candy;
    private BigDecimal quintals;

    public UUID getLotId() { return lotId; }
    public void setLotId(UUID lotId) { this.lotId = lotId; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }

    public BigDecimal getBaleWeight() { return baleWeight; }
    public void setBaleWeight(BigDecimal baleWeight) { this.baleWeight = baleWeight; }

    public BigDecimal getCandy() { return candy; }
    public void setCandy(BigDecimal candy) { this.candy = candy; }

    public BigDecimal getQuintals() { return quintals; }
    public void setQuintals(BigDecimal quintals) { this.quintals = quintals; }
}
