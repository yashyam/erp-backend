package com.ginning.erp.sales.controller;
import com.ginning.erp.common.dto.ApiResponse;
import com.ginning.erp.sales.dto.*;
import com.ginning.erp.sales.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/sales")
public class SaleController {
    private final SaleService service;
    public SaleController(SaleService service){this.service=service;}
    @GetMapping @PreAuthorize("hasAuthority('SALES_READ')") public ApiResponse<List<SaleResponse>> list(){return ApiResponse.success(service.list());}
    @GetMapping("/{id}") @PreAuthorize("hasAuthority('SALES_READ')") public ApiResponse<SaleResponse> get(@PathVariable UUID id){return ApiResponse.success(service.get(id));}
    @GetMapping("/lookup") @PreAuthorize("hasAuthority('SALES_READ')") public ApiResponse<SaleResponse> lookup(@RequestParam(required=false) String salesId,@RequestParam(required=false) String billId){return ApiResponse.success(service.lookup(salesId,billId));}
    @GetMapping("/lookup/lot") @PreAuthorize("hasAuthority('SALES_READ')") public ApiResponse<List<SaleResponse>> lookupByLot(@RequestParam String lotNumber){return ApiResponse.success(service.lookupByLot(lotNumber));}
    @PostMapping @PreAuthorize("hasAuthority('SALES_CREATE')") public ResponseEntity<ApiResponse<SaleResponse>> create(@Valid @RequestBody SaleRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(request),"Sale created successfully"));}
}
