package com.ginning.erp.kapas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record KapasPurchaseReportResponse(
        Summary summary,
        List<SupplierSummary> supplierWise,
        List<GodownSummary> godownWise,
        List<LotSummary> lotWise
) {
    public record Summary(long purchaseCount, BigDecimal totalBags, BigDecimal totalNetWeight,
                          BigDecimal averageRate, BigDecimal totalPurchaseValue) {}
    public record SupplierSummary(String supplier, BigDecimal totalBags, BigDecimal totalNetWeight,
                                  BigDecimal averageRate, BigDecimal totalPurchaseValue) {}
    public record GodownSummary(String godown, BigDecimal totalBags, BigDecimal totalNetWeight,
                                long lotCount, BigDecimal totalPurchaseValue) {}
    public record LotSummary(String lotNumber, String supplier, LocalDate date, BigDecimal netWeight,
                             BigDecimal rate, String godown) {}
}
