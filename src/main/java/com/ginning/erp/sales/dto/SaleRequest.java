package com.ginning.erp.sales.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class SaleRequest {
    @NotNull private UUID kapasPurchaseEntryId;
    private UUID customerId;
    @NotBlank @Size(max=255) private String customerName;
    @NotBlank @Size(max=100) private String billId;
    @NotNull private LocalDate rawMaterialOutDate;
    @NotNull @DecimalMin("0.0") private BigDecimal baleKg;
    @NotNull @DecimalMin("0.0") private BigDecimal seedKg;
    @NotNull @DecimalMin("0.0") private BigDecimal soldProductWeight;
    @DecimalMin("0.0") private BigDecimal salePrice;
    @DecimalMin("0.0") private BigDecimal saleRate;
    @NotNull @DecimalMin("0.0") private BigDecimal salesValue;
    @Size(max=255) private String buyerContact;
    @Size(max=500) private String buyerAddress;
    @Size(max=2000) private String notes;
    public UUID getKapasPurchaseEntryId(){return kapasPurchaseEntryId;} public void setKapasPurchaseEntryId(UUID v){kapasPurchaseEntryId=v;}
    public UUID getCustomerId(){return customerId;} public void setCustomerId(UUID v){customerId=v;}
    public String getCustomerName(){return customerName;} public void setCustomerName(String v){customerName=v;}
    public String getBillId(){return billId;} public void setBillId(String v){billId=v;}
    public LocalDate getRawMaterialOutDate(){return rawMaterialOutDate;} public void setRawMaterialOutDate(LocalDate v){rawMaterialOutDate=v;}
    public BigDecimal getBaleKg(){return baleKg;} public void setBaleKg(BigDecimal v){baleKg=v;}
    public BigDecimal getSeedKg(){return seedKg;} public void setSeedKg(BigDecimal v){seedKg=v;}
    public BigDecimal getSoldProductWeight(){return soldProductWeight;} public void setSoldProductWeight(BigDecimal v){soldProductWeight=v;}
    public BigDecimal getSalePrice(){return salePrice;} public void setSalePrice(BigDecimal v){salePrice=v;}
    public BigDecimal getSaleRate(){return saleRate;} public void setSaleRate(BigDecimal v){saleRate=v;}
    public BigDecimal getSalesValue(){return salesValue;} public void setSalesValue(BigDecimal v){salesValue=v;}
    public String getBuyerContact(){return buyerContact;} public void setBuyerContact(String v){buyerContact=v;}
    public String getBuyerAddress(){return buyerAddress;} public void setBuyerAddress(String v){buyerAddress=v;}
    public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
}
