package com.ginning.erp.kapas.service;

import com.ginning.erp.kapas.dto.KapasPurchaseEntryResponse;
import com.ginning.erp.kapas.dto.KapasPurchaseReportResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class KapasPurchaseReportService {
    private final KapasPurchaseEntryService purchaseService;

    public KapasPurchaseReportService(KapasPurchaseEntryService purchaseService) {
        this.purchaseService = purchaseService;
    }

    public KapasPurchaseReportResponse build(String supplierName, String lotNumber, LocalDate fromDate, LocalDate toDate) {
        LocalDate today = LocalDate.now();
        LocalDate effectiveFromDate = capDateToToday(fromDate, today);
        LocalDate effectiveToDate = capDateToToday(toDate, today);
        List<KapasPurchaseEntryResponse> purchases = purchaseService.list().stream()
                .filter(p -> filterBySupplier(p, supplierName))
                .filter(p -> filterByLot(p, lotNumber))
            .filter(p -> filterByDateRange(p, effectiveFromDate, effectiveToDate))
                .toList();

        BigDecimal bags = sum(purchases, KapasPurchaseEntryResponse::getNumberOfBags);
        BigDecimal net = sum(purchases, KapasPurchaseEntryResponse::getNetKapasWeight);
        BigDecimal value = sum(purchases, KapasPurchaseEntryResponse::getAmount);
        BigDecimal rateTotal = sum(purchases, KapasPurchaseEntryResponse::getRate);
        BigDecimal averageRate = purchases.isEmpty() ? BigDecimal.ZERO : rateTotal.divide(BigDecimal.valueOf(purchases.size()), 2, RoundingMode.HALF_UP);

        Map<String, List<KapasPurchaseEntryResponse>> bySupplier = purchases.stream().collect(Collectors.groupingBy(p -> key(p.getSupplierName()), LinkedHashMap::new, Collectors.toList()));
        List<KapasPurchaseReportResponse.SupplierSummary> supplierWise = new ArrayList<>();
        bySupplier.values().forEach(rows -> supplierWise.add(new KapasPurchaseReportResponse.SupplierSummary(
                rows.get(0).getSupplierName(), sum(rows, KapasPurchaseEntryResponse::getNumberOfBags),
                sum(rows, KapasPurchaseEntryResponse::getNetKapasWeight), average(rows, KapasPurchaseEntryResponse::getRate),
                sum(rows, KapasPurchaseEntryResponse::getAmount))));

        Map<String, List<KapasPurchaseEntryResponse>> byGodown = purchases.stream().collect(Collectors.groupingBy(p -> key(p.getGodownName()), LinkedHashMap::new, Collectors.toList()));
        List<KapasPurchaseReportResponse.GodownSummary> godownWise = new ArrayList<>();
        byGodown.values().forEach(rows -> godownWise.add(new KapasPurchaseReportResponse.GodownSummary(
                rows.get(0).getGodownName(), sum(rows, KapasPurchaseEntryResponse::getNumberOfBags),
                sum(rows, KapasPurchaseEntryResponse::getNetKapasWeight),
                rows.stream().map(KapasPurchaseEntryResponse::getLotNumber).filter(Objects::nonNull).distinct().count(),
                sum(rows, KapasPurchaseEntryResponse::getAmount))));

        List<KapasPurchaseReportResponse.LotSummary> lotWise = purchases.stream().map(p -> new KapasPurchaseReportResponse.LotSummary(
                p.getLotNumber(), p.getSupplierName(), p.getBillDate(), p.getNetKapasWeight(), p.getRate(), p.getGodownName())).toList();
        return new KapasPurchaseReportResponse(new KapasPurchaseReportResponse.Summary(purchases.size(), bags, net, averageRate, value), supplierWise, godownWise, lotWise);
    }

    private LocalDate capDateToToday(LocalDate date, LocalDate today) {
        return date != null && date.isAfter(today) ? today : date;
    }

    private boolean filterBySupplier(KapasPurchaseEntryResponse purchase, String supplierName) {
        if (supplierName == null || supplierName.isBlank()) {
            return true;
        }
        return purchase.getSupplierName() != null && purchase.getSupplierName().equalsIgnoreCase(supplierName.trim());
    }

    private boolean filterByLot(KapasPurchaseEntryResponse purchase, String lotNumber) {
        if (lotNumber == null || lotNumber.isBlank()) {
            return true;
        }
        return purchase.getLotNumber() != null && purchase.getLotNumber().equalsIgnoreCase(lotNumber.trim());
    }

    private boolean filterByDateRange(KapasPurchaseEntryResponse purchase, LocalDate fromDate, LocalDate toDate) {
        if (purchase.getBillDate() == null) {
            return fromDate == null && toDate == null;
        }
        if (fromDate != null && purchase.getBillDate().isBefore(fromDate)) {
            return false;
        }
        if (toDate != null && purchase.getBillDate().isAfter(toDate)) {
            return false;
        }
        return true;
    }

    private static BigDecimal sum(List<KapasPurchaseEntryResponse> rows, Function<KapasPurchaseEntryResponse, BigDecimal> getter) {
        return rows.stream().map(getter).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private static BigDecimal average(List<KapasPurchaseEntryResponse> rows, Function<KapasPurchaseEntryResponse, BigDecimal> getter) {
        long count = rows.stream().map(getter).filter(Objects::nonNull).count();
        return count == 0 ? BigDecimal.ZERO : sum(rows, getter).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }
    private static String key(String value) { return value == null ? "" : value; }
}
