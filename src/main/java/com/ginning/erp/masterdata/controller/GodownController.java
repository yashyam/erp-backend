package com.ginning.erp.masterdata.controller;
import com.ginning.erp.common.dto.ApiResponse; import com.ginning.erp.masterdata.dto.*; import com.ginning.erp.masterdata.service.GodownService;
import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.util.List; import java.util.UUID;
@RestController @RequestMapping("/api/v1/master-data/godowns")
public class GodownController {
 private final GodownService service; public GodownController(GodownService service){this.service=service;}
 @GetMapping @PreAuthorize("hasAuthority('MASTER_DATA_READ')") public ApiResponse<List<GodownResponse>> list(){return ApiResponse.success(service.list());}
 @GetMapping("/{id}") @PreAuthorize("hasAuthority('MASTER_DATA_READ')") public ApiResponse<GodownResponse> get(@PathVariable UUID id){return ApiResponse.success(service.get(id));}
 @PostMapping @PreAuthorize("hasAuthority('MASTER_DATA_WRITE')") public ResponseEntity<ApiResponse<GodownResponse>> create(@Valid @RequestBody GodownRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(r),"Godown created successfully"));}
 @PutMapping("/{id}") @PreAuthorize("hasAuthority('MASTER_DATA_WRITE')") public ApiResponse<GodownResponse> update(@PathVariable UUID id,@Valid @RequestBody GodownRequest r){return ApiResponse.success(service.update(id,r),"Godown updated successfully");}
 @DeleteMapping("/{id}") @PreAuthorize("hasAuthority('MASTER_DATA_WRITE')") public ApiResponse<Void> delete(@PathVariable UUID id){service.delete(id);return ApiResponse.success(null,"Godown deleted successfully");}
}
