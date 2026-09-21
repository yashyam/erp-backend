package com.ginning.erp.sales.entity;

import com.ginning.erp.common.entity.BaseEntity;
import com.ginning.erp.kapas.entity.KapasPurchaseEntry;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sales")
public class Sale extends BaseEntity {
    @Column(name="sales_id", nullable=false, unique=true, length=40) private String salesId;
    @Column(name="bill_id", nullable=false, unique=true, length=100) private String billId;
    @Column(name="kapas_purchase_entry_id", nullable=false) private UUID kapasPurchaseEntryId;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="kapas_purchase_entry_id", insertable=false, updatable=false) private KapasPurchaseEntry purchaseEntry;
    @Column(name="customer_id") private UUID customerId;
    @Column(name="customer_name", nullable=false, length=255) private String customerName;
    @Column(name="supplier_id") private UUID supplierId;
    @Column(name="supplier_name", nullable=false, length=255) private String supplierName;
    @Column(name="lot_number", length=100) private String lotNumber;
    @Column(name="raw_material_in_date", nullable=false) private LocalDate rawMaterialInDate;
    @Column(name="raw_material_out_date", nullable=false) private LocalDate rawMaterialOutDate;
    @Column(name="raw_material_net_weight", nullable=false, precision=19, scale=3) private BigDecimal rawMaterialNetWeight;
    @Column(name="raw_material_cost", nullable=false, precision=19, scale=2) private BigDecimal rawMaterialCost;
    @Column(name="bale_kg", precision=19, scale=3) private BigDecimal baleKg;
    @Column(name="seed_kg", precision=19, scale=3) private BigDecimal seedKg;
    @Column(name="sold_product_weight", nullable=false, precision=19, scale=3) private BigDecimal soldProductWeight;
    @Column(name="sale_price", precision=19, scale=2) private BigDecimal salePrice;
    @Column(name="sale_rate", precision=19, scale=2) private BigDecimal saleRate;
    @Column(name="sales_value", nullable=false, precision=19, scale=2) private BigDecimal salesValue;
    @Column(name="buyer_contact", length=255) private String buyerContact;
    @Column(name="buyer_address", length=500) private String buyerAddress;
    @Column(length=2000) private String notes;
    @Column(name="deleted_at") private LocalDateTime deletedAt;

    public String getSalesId(){return salesId;} public void setSalesId(String v){salesId=v;}
    public String getBillId(){return billId;} public void setBillId(String v){billId=v;}
    public UUID getKapasPurchaseEntryId(){return kapasPurchaseEntryId;} public void setKapasPurchaseEntryId(UUID v){kapasPurchaseEntryId=v;}
    public KapasPurchaseEntry getPurchaseEntry(){return purchaseEntry;}
    public UUID getCustomerId(){return customerId;} public void setCustomerId(UUID v){customerId=v;}
    public String getCustomerName(){return customerName;} public void setCustomerName(String v){customerName=v;}
    public UUID getSupplierId(){return supplierId;} public void setSupplierId(UUID v){supplierId=v;}
    public String getSupplierName(){return supplierName;} public void setSupplierName(String v){supplierName=v;}
    public String getLotNumber(){return lotNumber;} public void setLotNumber(String v){lotNumber=v;}
    public LocalDate getRawMaterialInDate(){return rawMaterialInDate;} public void setRawMaterialInDate(LocalDate v){rawMaterialInDate=v;}
    public LocalDate getRawMaterialOutDate(){return rawMaterialOutDate;} public void setRawMaterialOutDate(LocalDate v){rawMaterialOutDate=v;}
    public BigDecimal getRawMaterialNetWeight(){return rawMaterialNetWeight;} public void setRawMaterialNetWeight(BigDecimal v){rawMaterialNetWeight=v;}
    public BigDecimal getRawMaterialCost(){return rawMaterialCost;} public void setRawMaterialCost(BigDecimal v){rawMaterialCost=v;}
    public BigDecimal getBaleKg(){return baleKg;} public void setBaleKg(BigDecimal v){baleKg=v;}
    public BigDecimal getSeedKg(){return seedKg;} public void setSeedKg(BigDecimal v){seedKg=v;}
    public BigDecimal getSoldProductWeight(){return soldProductWeight;} public void setSoldProductWeight(BigDecimal v){soldProductWeight=v;}
    public BigDecimal getSalePrice(){return salePrice;} public void setSalePrice(BigDecimal v){salePrice=v;}
    public BigDecimal getSaleRate(){return saleRate;} public void setSaleRate(BigDecimal v){saleRate=v;}
    public BigDecimal getSalesValue(){return salesValue;} public void setSalesValue(BigDecimal v){salesValue=v;}
    public String getBuyerContact(){return buyerContact;} public void setBuyerContact(String v){buyerContact=v;}
    public String getBuyerAddress(){return buyerAddress;} public void setBuyerAddress(String v){buyerAddress=v;}
    public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
    public LocalDateTime getDeletedAt(){return deletedAt;} public void setDeletedAt(LocalDateTime v){deletedAt=v;}
}
