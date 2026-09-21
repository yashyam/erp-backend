package com.ginning.erp.kapas.controller;

import com.ginning.erp.common.dto.ApiResponse;
import com.ginning.erp.kapas.dto.KapasPurchaseReportResponse;
import com.ginning.erp.kapas.service.KapasPurchaseReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
public class KapasPurchaseReportController {
    private final KapasPurchaseReportService service;

    public KapasPurchaseReportController(KapasPurchaseReportService service) {
        this.service = service;
    }

    @GetMapping("/kapas-purchases")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ApiResponse<KapasPurchaseReportResponse> report(
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String lotNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ApiResponse.success(service.build(supplierName, lotNumber, fromDate, toDate));
    }
}
