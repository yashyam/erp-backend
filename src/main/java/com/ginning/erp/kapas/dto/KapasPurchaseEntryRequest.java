package com.ginning.erp.kapas.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class KapasPurchaseEntryRequest {
    @NotNull private LocalDate billDate;
    @NotBlank @Size(max = 255) private String supplierName;
    private UUID supplierId;
    @NotBlank @Size(max = 100) private String billNumber;
    @NotBlank @Size(max = 50) private String vehicleNumber;
    @NotNull @Digits(integer = 19, fraction = 0) @Min(1) private BigDecimal numberOfBags;
    @NotNull @DecimalMin(value = "0.0", inclusive = false) private BigDecimal grossWeight;
    @NotNull @DecimalMin(value = "0.0") private BigDecimal tareWeight;
    @DecimalMin(value = "0.0") private BigDecimal netKapasWeight;
    @NotNull @DecimalMin(value = "0.0", inclusive = false) private BigDecimal rate;
    @DecimalMin(value = "0.0") private BigDecimal amount;
    @NotBlank @Size(max = 100) private String lotNumber;
    @NotNull private UUID godownId;
    private UUID textExtractionId;

    public LocalDate getBillDate() { return billDate; } public void setBillDate(LocalDate v) { billDate = v; }
    public String getSupplierName() { return supplierName; } public void setSupplierName(String v) { supplierName = v; }
    public UUID getSupplierId() { return supplierId; } public void setSupplierId(UUID v) { supplierId = v; }
    public String getBillNumber() { return billNumber; } public void setBillNumber(String v) { billNumber = v; }
    public String getVehicleNumber() { return vehicleNumber; } public void setVehicleNumber(String v) { vehicleNumber = v; }
    public BigDecimal getNumberOfBags() { return numberOfBags; } public void setNumberOfBags(BigDecimal v) { numberOfBags = v; }
    public BigDecimal getGrossWeight() { return grossWeight; } public void setGrossWeight(BigDecimal v) { grossWeight = v; }
    public BigDecimal getTareWeight() { return tareWeight; } public void setTareWeight(BigDecimal v) { tareWeight = v; }
    public BigDecimal getNetKapasWeight() { return netKapasWeight; } public void setNetKapasWeight(BigDecimal v) { netKapasWeight = v; }
    public BigDecimal getRate() { return rate; } public void setRate(BigDecimal v) { rate = v; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal v) { amount = v; }
    public String getLotNumber() { return lotNumber; } public void setLotNumber(String v) { lotNumber = v; }
    public UUID getGodownId() { return godownId; } public void setGodownId(UUID v) { godownId = v; }
    public UUID getTextExtractionId() { return textExtractionId; } public void setTextExtractionId(UUID v) { textExtractionId = v; }
}
