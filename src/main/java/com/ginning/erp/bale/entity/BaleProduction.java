package com.ginning.erp.bale.entity;

import com.ginning.erp.auth.entity.User;
import com.ginning.erp.common.entity.BaseEntity;
import com.ginning.erp.kapas.entity.KapasPurchaseEntry;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bale_production")
public class BaleProduction extends BaseEntity {
    @Column(name = "bale_number", nullable = false, unique = true, length = 40)
    private String baleNumber;

    @Column(name = "lot_id", nullable = false)
    private UUID lotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", insertable = false, updatable = false)
    private KapasPurchaseEntry lot;

    @Column(name = "lot_number", nullable = false, length = 100)
    private String lotNumber;

    @Column(name = "production_date", nullable = false)
    private LocalDate productionDate;

    @Column(name = "serial_no", nullable = false)
    private Long serialNo;

    @Column(name = "bale_weight", nullable = false, precision = 19, scale = 3)
    private BigDecimal baleWeight;

    @Column(precision = 19, scale = 3)
    private BigDecimal candy;

    @Column(precision = 19, scale = 3)
    private BigDecimal quintals;

    @Column(name = "created_by")
    private UUID createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User creator;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public String getBaleNumber() { return baleNumber; }
    public void setBaleNumber(String baleNumber) { this.baleNumber = baleNumber; }

    public UUID getLotId() { return lotId; }
    public void setLotId(UUID lotId) { this.lotId = lotId; }

    public KapasPurchaseEntry getLot() { return lot; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

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

    public User getCreator() { return creator; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
