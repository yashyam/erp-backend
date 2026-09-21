package com.ginning.erp.document.dto;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.Map;
public record UpdateTextExtractionRequest(String supplierName,String invoiceNumber,LocalDate invoiceDate,String vehicleNumber,BigDecimal quantity,BigDecimal rate,BigDecimal tax,BigDecimal totalAmount,Map<String,Object> extractedData) {}
