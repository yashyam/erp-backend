package com.ginning.erp.kapas.dto;

import com.ginning.erp.kapas.entity.KapasPurchaseEntry;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class KapasPurchaseEntryResponse {
    private UUID id; private String entryNumber; private LocalDate billDate; private String supplierName; private String billNumber;
    private String vehicleNumber; private BigDecimal numberOfBags; private BigDecimal grossWeight; private BigDecimal tareWeight; private BigDecimal netKapasWeight;
    private BigDecimal rate; private BigDecimal quintals; private BigDecimal amount; private String lotNumber; private String lotStatus; private UUID godownId; private String godownName;
    private UUID supplierId; private UUID textExtractionId; private UUID createdBy; private LocalDateTime createdAt; private LocalDateTime updatedAt;

    public static KapasPurchaseEntryResponse from(KapasPurchaseEntry e, String godownName) {
        KapasPurchaseEntryResponse r = new KapasPurchaseEntryResponse();
        r.id = e.getId(); r.entryNumber = e.getEntryNumber(); r.billDate = e.getBillDate(); r.supplierName = e.getSupplierName();
        r.billNumber = e.getBillNumber(); r.vehicleNumber = e.getVehicleNumber(); r.numberOfBags = e.getNumberOfBags(); r.grossWeight = e.getGrossWeight();
        r.tareWeight = e.getTareWeight(); r.netKapasWeight = e.getNetKapasWeight(); r.rate = e.getRate(); r.quintals = e.getQuintals(); r.amount = e.getAmount();
        r.lotNumber = e.getLotNumber(); r.lotStatus = e.getLotStatus(); r.godownId = e.getGodownId(); r.godownName = godownName; r.supplierId = e.getSupplierId(); r.textExtractionId = e.getTextExtractionId(); r.createdBy = e.getCreatedBy();
        r.createdAt = e.getCreatedAt(); r.updatedAt = e.getUpdatedAt();
        return r;
    }

    public UUID getId(){return id;} public String getEntryNumber(){return entryNumber;} public LocalDate getBillDate(){return billDate;}
    public String getSupplierName(){return supplierName;} public String getBillNumber(){return billNumber;}
    public UUID getSupplierId(){return supplierId;}
    public String getVehicleNumber(){return vehicleNumber;} public BigDecimal getGrossWeight(){return grossWeight;}
    public BigDecimal getNumberOfBags(){return numberOfBags;}
    public BigDecimal getTareWeight(){return tareWeight;} public BigDecimal getNetKapasWeight(){return netKapasWeight;}
    public BigDecimal getRate(){return rate;} public BigDecimal getAmount(){return amount;} public String getLotNumber(){return lotNumber;} public String getLotStatus(){return lotStatus;}
    public BigDecimal getQuintals(){return quintals;}
    public UUID getGodownId(){return godownId;} public String getGodownName(){return godownName;}
    public UUID getTextExtractionId(){return textExtractionId;}
    public UUID getCreatedBy(){return createdBy;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
