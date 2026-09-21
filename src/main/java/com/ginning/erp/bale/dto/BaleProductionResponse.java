package com.ginning.erp.bale.dto;

import com.ginning.erp.bale.entity.BaleProduction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class BaleProductionResponse {
    private UUID id;
    private String baleNumber;
    private UUID lotId;
    private String lotNumber;
    private String lotStatus;
    private LocalDate productionDate;
    private Long serialNo;
    private BigDecimal baleWeight;
    private BigDecimal candy;
    private BigDecimal quintals;
    private UUID createdBy;
    private LocalDateTime createdAt;

    public static BaleProductionResponse from(BaleProduction bale) {
        BaleProductionResponse response = new BaleProductionResponse();
        response.id = bale.getId();
        response.baleNumber = bale.getBaleNumber();
        response.lotId = bale.getLotId();
        response.lotNumber = bale.getLotNumber();
        response.lotStatus = bale.getLot() != null && bale.getLot().getLotStatus() != null ? bale.getLot().getLotStatus() : "RUNNING";
        response.productionDate = bale.getProductionDate();
        response.serialNo = bale.getSerialNo();
        response.baleWeight = bale.getBaleWeight();
        response.candy = bale.getCandy();
        response.quintals = bale.getQuintals();
        response.createdBy = bale.getCreatedBy();
        response.createdAt = bale.getCreatedAt();
        return response;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getBaleNumber() { return baleNumber; }
    public void setBaleNumber(String baleNumber) { this.baleNumber = baleNumber; }

    public UUID getLotId() { return lotId; }
    public void setLotId(UUID lotId) { this.lotId = lotId; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public String getLotStatus() { return lotStatus; }
    public void setLotStatus(String lotStatus) { this.lotStatus = lotStatus; }

    public LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }

    public Long getSerialNo() { return serialNo; }
    public void setSerialNo(Long serialNo) { this.serialNo = serialNo; }

    public BigDecimal getBaleWeight() { return baleWeight; }
    public void setBaleWeight(BigDecimal baleWeight) { this.baleWeight = baleWeight; }

    public BigDecimal getCandy() { return candy; }
    public void setCandy(BigDecimal candy) { this.candy = candy; }

    public BigDecimal getQuintals() { return quintals; }
    public void setQuintals(BigDecimal quintals) { this.quintals = quintals; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
