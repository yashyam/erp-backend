package com.ginning.erp.document.entity;

import com.ginning.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "text_extractions")
public class TextExtraction extends BaseEntity {
    @Column(name="extraction_number", nullable=false, unique=true, length=40)
    private String extractionNumber;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private ExtractionStatus status = ExtractionStatus.PENDING;
    @Column(name="source_text", nullable=false, updatable=false, columnDefinition="text") private String sourceText;
    private String supplierName;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private String vehicleNumber;
    private BigDecimal quantity;
    private BigDecimal rate;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name="extracted_data", columnDefinition="jsonb") private Map<String,Object> extractedData;
    @Enumerated(EnumType.STRING) @Column(name="extraction_method", length=20) private ExtractionMethod extractionMethod;
    @Column(precision=5, scale=4) private BigDecimal confidenceScore;
    @Column(columnDefinition="text") private String errorMessage;
    @Column(name="source_file_name") private String sourceFileName;
    @Column(name="source_file_type", length=100) private String sourceFileType;
    @Column(name="source_file_size") private Long sourceFileSize;

    protected TextExtraction() {}
    public TextExtraction(String number, String sourceText) { this.extractionNumber=number; this.sourceText=sourceText; }
    public String getExtractionNumber(){return extractionNumber;} public void setExtractionNumber(String v){this.extractionNumber=v;}
    public ExtractionStatus getStatus(){return status;} public void setStatus(ExtractionStatus v){this.status=v;}
    public String getSourceText(){return sourceText;}
    public String getSupplierName(){return supplierName;} public void setSupplierName(String v){supplierName=v;}
    public String getInvoiceNumber(){return invoiceNumber;} public void setInvoiceNumber(String v){invoiceNumber=v;}
    public LocalDate getInvoiceDate(){return invoiceDate;} public void setInvoiceDate(LocalDate v){invoiceDate=v;}
    public String getVehicleNumber(){return vehicleNumber;} public void setVehicleNumber(String v){vehicleNumber=v;}
    public BigDecimal getQuantity(){return quantity;} public void setQuantity(BigDecimal v){quantity=v;}
    public BigDecimal getRate(){return rate;} public void setRate(BigDecimal v){rate=v;}
    public BigDecimal getTax(){return tax;} public void setTax(BigDecimal v){tax=v;}
    public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){totalAmount=v;}
    public Map<String,Object> getExtractedData(){return extractedData;} public void setExtractedData(Map<String,Object> v){extractedData=v;}
    public ExtractionMethod getExtractionMethod(){return extractionMethod;} public void setExtractionMethod(ExtractionMethod v){extractionMethod=v;}
    public BigDecimal getConfidenceScore(){return confidenceScore;} public void setConfidenceScore(BigDecimal v){confidenceScore=v;}
    public String getErrorMessage(){return errorMessage;} public void setErrorMessage(String v){errorMessage=v;}
    public String getSourceFileName(){return sourceFileName;} public void setSourceFileName(String v){sourceFileName=v;}
    public String getSourceFileType(){return sourceFileType;} public void setSourceFileType(String v){sourceFileType=v;}
    public Long getSourceFileSize(){return sourceFileSize;} public void setSourceFileSize(Long v){sourceFileSize=v;}
    @Transient
    public List<String> getMissingFields(){
        List<String> missing = new ArrayList<>();
        Map<String,Object> data = extractedData == null ? Map.of() : extractedData;
        if (supplierName == null || supplierName.isBlank()) missing.add("Supplier name");
        if (invoiceNumber == null || invoiceNumber.isBlank()) missing.add("Bill number");
        if (invoiceDate == null) missing.add("Bill date");
        if (vehicleNumber == null || vehicleNumber.isBlank()) missing.add("Vehicle number");
        if (data.get("numberOfBags") == null) missing.add("No. of bags");
        if (data.get("grossWeight") == null) missing.add("Gross weight");
        if (data.get("tareWeight") == null) missing.add("Tare weight");
        return missing;
    }
}
