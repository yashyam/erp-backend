package com.ginning.erp.bale.controller;

import com.ginning.erp.bale.dto.BaleDailySummaryResponse;
import com.ginning.erp.bale.dto.BaleProductionRequest;
import com.ginning.erp.bale.dto.BaleProductionResponse;
import com.ginning.erp.bale.service.BaleProductionService;
import com.ginning.erp.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bales")
public class BaleProductionController {
    private final BaleProductionService service;

    public BaleProductionController(BaleProductionService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('BALE_READ')")
    public ApiResponse<List<BaleProductionResponse>> list() {
        return ApiResponse.success(service.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BALE_READ')")
    public ApiResponse<BaleProductionResponse> get(@PathVariable UUID id) {
        return ApiResponse.success(service.get(id));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('BALE_READ')")
    public ApiResponse<List<BaleDailySummaryResponse>> summary(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate
    ) {
        return ApiResponse.success(service.dailySummary(fromDate, toDate));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BALE_CREATE')")
    public ResponseEntity<ApiResponse<BaleProductionResponse>> create(@Valid @RequestBody BaleProductionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request), "Bale created successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BALE_DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ApiResponse.success(null, "Bale production record reversed successfully");
    }
}
