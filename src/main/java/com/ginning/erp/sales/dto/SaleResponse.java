package com.ginning.erp.sales.dto;

import com.ginning.erp.sales.entity.Sale;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class SaleResponse {
    private UUID id, kapasPurchaseEntryId, customerId, supplierId;
    private String salesId, billId, customerName, supplierName, lotNumber, buyerContact, buyerAddress, notes;
    private LocalDate rawMaterialInDate, rawMaterialOutDate;
    private BigDecimal rawMaterialNetWeight, rawMaterialCost, baleKg, seedKg, soldProductWeight, salePrice, saleRate, salesValue;
    private LocalDateTime createdAt, updatedAt;
    public static SaleResponse from(Sale s) {
        SaleResponse r=new SaleResponse(); r.id=s.getId(); r.salesId=s.getSalesId(); r.billId=s.getBillId();
        r.kapasPurchaseEntryId=s.getKapasPurchaseEntryId(); r.customerId=s.getCustomerId(); r.customerName=s.getCustomerName();
        r.supplierId=s.getSupplierId(); r.supplierName=s.getSupplierName(); r.lotNumber=s.getLotNumber();
        r.rawMaterialInDate=s.getRawMaterialInDate(); r.rawMaterialOutDate=s.getRawMaterialOutDate();
        r.rawMaterialNetWeight=s.getRawMaterialNetWeight(); r.rawMaterialCost=s.getRawMaterialCost();
        r.baleKg=s.getBaleKg(); r.seedKg=s.getSeedKg(); r.soldProductWeight=s.getSoldProductWeight();
        r.salePrice=s.getSalePrice(); r.saleRate=s.getSaleRate(); r.salesValue=s.getSalesValue();
        r.buyerContact=s.getBuyerContact(); r.buyerAddress=s.getBuyerAddress(); r.notes=s.getNotes();
        r.createdAt=s.getCreatedAt(); r.updatedAt=s.getUpdatedAt(); return r;
    }
    public UUID getId(){return id;} public UUID getKapasPurchaseEntryId(){return kapasPurchaseEntryId;} public UUID getCustomerId(){return customerId;} public UUID getSupplierId(){return supplierId;}
    public String getSalesId(){return salesId;} public String getBillId(){return billId;} public String getCustomerName(){return customerName;} public String getSupplierName(){return supplierName;} public String getLotNumber(){return lotNumber;} public String getBuyerContact(){return buyerContact;} public String getBuyerAddress(){return buyerAddress;} public String getNotes(){return notes;}
    public LocalDate getRawMaterialInDate(){return rawMaterialInDate;} public LocalDate getRawMaterialOutDate(){return rawMaterialOutDate;}
    public BigDecimal getRawMaterialNetWeight(){return rawMaterialNetWeight;} public BigDecimal getRawMaterialCost(){return rawMaterialCost;} public BigDecimal getBaleKg(){return baleKg;} public BigDecimal getSeedKg(){return seedKg;} public BigDecimal getSoldProductWeight(){return soldProductWeight;} public BigDecimal getSalePrice(){return salePrice;} public BigDecimal getSaleRate(){return saleRate;} public BigDecimal getSalesValue(){return salesValue;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
