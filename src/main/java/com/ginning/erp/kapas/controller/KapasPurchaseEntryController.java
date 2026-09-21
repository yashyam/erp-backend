package com.ginning.erp.kapas.controller;
import com.ginning.erp.common.dto.ApiResponse; import com.ginning.erp.kapas.dto.*; import com.ginning.erp.kapas.service.KapasPurchaseEntryService;
import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.util.List; import java.util.UUID;
@RestController @RequestMapping("/api/v1/kapas-purchases")
public class KapasPurchaseEntryController {
 private final KapasPurchaseEntryService service; public KapasPurchaseEntryController(KapasPurchaseEntryService service){this.service=service;}
 @GetMapping @PreAuthorize("hasAuthority('KAPAS_PURCHASE_READ')") public ApiResponse<List<KapasPurchaseEntryResponse>> list(){return ApiResponse.success(service.list());}
 @GetMapping("/{id}") @PreAuthorize("hasAuthority('KAPAS_PURCHASE_READ')") public ApiResponse<KapasPurchaseEntryResponse> get(@PathVariable UUID id){return ApiResponse.success(service.get(id));}
 @PostMapping @PreAuthorize("hasAuthority('KAPAS_PURCHASE_CREATE')") public ResponseEntity<ApiResponse<KapasPurchaseEntryResponse>> create(@Valid @RequestBody KapasPurchaseEntryRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(r),"Kapas purchase entry created successfully"));}
 @PutMapping("/{id}") @PreAuthorize("hasAnyAuthority('KAPAS_PURCHASE_UPDATE','ROLE_ADMIN','ROLE_SUPERVISOR','ROLE_SUPER_ADMIN')") public ApiResponse<KapasPurchaseEntryResponse> update(@PathVariable UUID id,@Valid @RequestBody KapasPurchaseEntryRequest r){return ApiResponse.success(service.update(id,r),"Kapas purchase entry updated successfully");}
 @DeleteMapping("/{id}") @PreAuthorize("hasAnyAuthority('KAPAS_PURCHASE_DELETE','ROLE_ADMIN','ROLE_SUPER_ADMIN')") public ApiResponse<Void> delete(@PathVariable UUID id){service.delete(id);return ApiResponse.success(null,"Kapas purchase entry deleted successfully");}
 @PostMapping("/from-extraction/{extractionId}") @PreAuthorize("hasAuthority('KAPAS_PURCHASE_CREATE')") public ResponseEntity<ApiResponse<KapasPurchaseEntryResponse>> fromExtraction(@PathVariable UUID extractionId){return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createFromExtraction(extractionId),"Kapas purchase entry created from extraction"));}
}
