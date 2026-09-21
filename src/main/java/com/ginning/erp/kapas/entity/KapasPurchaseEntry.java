package com.ginning.erp.kapas.entity;

import com.ginning.erp.common.entity.BaseEntity;
import com.ginning.erp.masterdata.entity.Godown;
import com.ginning.erp.masterdata.entity.Supplier;
import com.ginning.erp.auth.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kapas_purchase_entries")
public class KapasPurchaseEntry extends BaseEntity {
    @Column(name="entry_number", nullable=false, unique=true, length=40) private String entryNumber;
    @Column(name="bill_date", nullable=false) private LocalDate billDate;
    @Column(name="supplier_name", nullable=false, length=255) private String supplierName;
    @Column(name="supplier_id") private UUID supplierId;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="supplier_id", insertable=false, updatable=false) private Supplier supplier;
    @Column(name="bill_number", nullable=false, length=100) private String billNumber;
    @Column(name="vehicle_number", length=50) private String vehicleNumber;
    @Column(name="number_of_bags", precision=19, scale=3, nullable=false) private BigDecimal numberOfBags;
    @Column(name="gross_weight", precision=19, scale=3) private BigDecimal grossWeight;
    @Column(name="tare_weight", precision=19, scale=3) private BigDecimal tareWeight;
    @Column(name="net_kapas_weight", precision=19, scale=3) private BigDecimal netKapasWeight;
    @Column(precision=19, scale=2) private BigDecimal rate;
    @Column(precision=19, scale=3) private BigDecimal quintals;
    @Column(precision=19, scale=2) private BigDecimal amount;
    @Column(name="lot_number", length=100) private String lotNumber;
    @Column(name="lot_status", length=30, nullable=false) private String lotStatus = "YET_TO_START";
    @Column(name="status_updated_at") private LocalDateTime statusUpdatedAt;
    @Column(name="godown_id") private UUID godownId;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="godown_id", insertable=false, updatable=false) private Godown godown;
    @Column(name="text_extraction_id") private UUID textExtractionId;
    @Column(name="created_by") private UUID createdBy;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="created_by", insertable=false, updatable=false) private User creator;
    @Column(name="deleted_at") private LocalDateTime deletedAt;

    public String getEntryNumber(){return entryNumber;} public void setEntryNumber(String v){entryNumber=v;}
    public LocalDate getBillDate(){return billDate;} public void setBillDate(LocalDate v){billDate=v;}
    public String getSupplierName(){return supplierName;} public void setSupplierName(String v){supplierName=v;}
    public UUID getSupplierId(){return supplierId;} public void setSupplierId(UUID v){supplierId=v;}
    public Supplier getSupplier(){return supplier;}
    public String getBillNumber(){return billNumber;} public void setBillNumber(String v){billNumber=v;}
    public String getVehicleNumber(){return vehicleNumber;} public void setVehicleNumber(String v){vehicleNumber=v;}
    public BigDecimal getNumberOfBags(){return numberOfBags;} public void setNumberOfBags(BigDecimal v){numberOfBags=v;}
    public BigDecimal getGrossWeight(){return grossWeight;} public void setGrossWeight(BigDecimal v){grossWeight=v;}
    public BigDecimal getTareWeight(){return tareWeight;} public void setTareWeight(BigDecimal v){tareWeight=v;}
    public BigDecimal getNetKapasWeight(){return netKapasWeight;} public void setNetKapasWeight(BigDecimal v){netKapasWeight=v;}
    public BigDecimal getRate(){return rate;} public void setRate(BigDecimal v){rate=v;}
    public BigDecimal getQuintals(){return quintals;} public void setQuintals(BigDecimal v){quintals=v;}
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
    public String getLotNumber(){return lotNumber;} public void setLotNumber(String v){lotNumber=v;}
    public String getLotStatus(){return lotStatus;} public void setLotStatus(String v){lotStatus=v;}
    public LocalDateTime getStatusUpdatedAt(){return statusUpdatedAt;} public void setStatusUpdatedAt(LocalDateTime v){statusUpdatedAt=v;}
    public UUID getGodownId(){return godownId;} public void setGodownId(UUID v){godownId=v;}
    public Godown getGodown(){return godown;}
    public UUID getTextExtractionId(){return textExtractionId;} public void setTextExtractionId(UUID v){textExtractionId=v;}
    public UUID getCreatedBy(){return createdBy;} public void setCreatedBy(UUID v){createdBy=v;}
    public LocalDateTime getDeletedAt(){return deletedAt;} public void setDeletedAt(LocalDateTime v){deletedAt=v;}
}
