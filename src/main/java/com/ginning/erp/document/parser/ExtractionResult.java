package com.ginning.erp.document.parser;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.Map;
public record ExtractionResult(String supplierName, String invoiceNumber, LocalDate invoiceDate, String vehicleNumber, BigDecimal quantity, BigDecimal rate, BigDecimal tax, BigDecimal totalAmount, Map<String,Object> data, BigDecimal confidenceScore) {}
