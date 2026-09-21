package com.ginning.erp.masterdata.controller;
import com.ginning.erp.common.dto.ApiResponse; import com.ginning.erp.masterdata.dto.*; import com.ginning.erp.masterdata.service.SupplierService;
import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.util.List; import java.util.UUID;
@RestController @RequestMapping("/api/v1/master-data/suppliers")
public class SupplierController {
 private final SupplierService service; public SupplierController(SupplierService service){this.service=service;}
 @GetMapping @PreAuthorize("hasAuthority('MASTER_DATA_READ')") public ApiResponse<List<SupplierResponse>> list(){return ApiResponse.success(service.list());}
 @GetMapping("/{id}") @PreAuthorize("hasAuthority('MASTER_DATA_READ')") public ApiResponse<SupplierResponse> get(@PathVariable UUID id){return ApiResponse.success(service.get(id));}
 @PostMapping @PreAuthorize("hasAuthority('MASTER_DATA_WRITE')") public ResponseEntity<ApiResponse<SupplierResponse>> create(@Valid @RequestBody SupplierRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(r),"Supplier created successfully"));}
 @PutMapping("/{id}") @PreAuthorize("hasAuthority('MASTER_DATA_WRITE')") public ApiResponse<SupplierResponse> update(@PathVariable UUID id,@Valid @RequestBody SupplierRequest r){return ApiResponse.success(service.update(id,r),"Supplier updated successfully");}
 @DeleteMapping("/{id}") @PreAuthorize("hasAuthority('MASTER_DATA_WRITE')") public ApiResponse<Void> delete(@PathVariable UUID id){service.delete(id);return ApiResponse.success(null,"Supplier deleted successfully");}
}
