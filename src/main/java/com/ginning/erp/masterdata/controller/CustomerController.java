package com.ginning.erp.masterdata.controller;
import com.ginning.erp.common.dto.ApiResponse; import com.ginning.erp.masterdata.dto.*; import com.ginning.erp.masterdata.service.CustomerService;
import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.util.List; import java.util.UUID;
@RestController @RequestMapping("/api/v1/master-data/customers")
public class CustomerController {
 private final CustomerService service; public CustomerController(CustomerService service){this.service=service;}
 @GetMapping @PreAuthorize("hasAuthority('MASTER_DATA_READ')") public ApiResponse<List<CustomerResponse>> list(){return ApiResponse.success(service.list());}
 @GetMapping("/{id}") @PreAuthorize("hasAuthority('MASTER_DATA_READ')") public ApiResponse<CustomerResponse> get(@PathVariable UUID id){return ApiResponse.success(service.get(id));}
 @PostMapping @PreAuthorize("hasAuthority('MASTER_DATA_WRITE')") public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CustomerRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(r),"Customer created successfully"));}
 @PutMapping("/{id}") @PreAuthorize("hasAuthority('MASTER_DATA_WRITE')") public ApiResponse<CustomerResponse> update(@PathVariable UUID id,@Valid @RequestBody CustomerRequest r){return ApiResponse.success(service.update(id,r),"Customer updated successfully");}
 @DeleteMapping("/{id}") @PreAuthorize("hasAuthority('MASTER_DATA_WRITE')") public ApiResponse<Void> delete(@PathVariable UUID id){service.delete(id);return ApiResponse.success(null,"Customer deleted successfully");}
}
