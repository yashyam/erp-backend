package com.ginning.erp.document.controller;
import com.ginning.erp.common.dto.ApiResponse; import com.ginning.erp.document.dto.*; import com.ginning.erp.document.entity.*; import com.ginning.erp.document.service.TextExtractionService; import com.ginning.erp.kapas.dto.KapasPurchaseEntryResponse; import com.ginning.erp.kapas.service.KapasPurchaseEntryService; import jakarta.validation.Valid; import org.springframework.data.domain.Page; import org.springframework.format.annotation.DateTimeFormat; import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import org.springframework.web.multipart.MultipartFile; import java.time.LocalDate; import java.util.UUID;
@RestController @RequestMapping("/api/v1/document/text-extractions")
public class TextExtractionController {
 private final TextExtractionService service; private final KapasPurchaseEntryService kapasService;
 public TextExtractionController(TextExtractionService service,KapasPurchaseEntryService kapasService){this.service=service;this.kapasService=kapasService;}
 /** Attach a bill (PDF or text). Extraction is persisted for review; kapas creation requires explicit confirmation. */
 @PostMapping(value="/upload", consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @PreAuthorize("hasAuthority('KAPAS_PURCHASE_CREATE')")
 public ResponseEntity<ApiResponse<AttachBillResponse>> upload(@RequestParam("file") MultipartFile file,
                                                               @RequestParam(name="createKapasEntry", defaultValue="true") boolean createKapasEntry){
  TextExtraction extraction=service.createFromFile(file);
  String error=createKapasEntry ? kapasService.duplicateMessageFromExtraction(extraction.getId()) : null;
  boolean duplicate=error!=null;
  String message=duplicate ? "Duplicate bill detected. Review the extracted data; no kapas entry was created."
          : "Bill extracted. Review the extracted data before creating a kapas purchase entry.";
  return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(new AttachBillResponse(extraction,null,error,duplicate),message));
 }
 @PostMapping @PreAuthorize("hasAuthority('KAPAS_PURCHASE_CREATE')") public ResponseEntity<ApiResponse<TextExtraction>> create(@Valid @RequestBody CreateTextExtractionRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(r),"Text extraction created"));}
 @GetMapping @PreAuthorize("hasAuthority('KAPAS_PURCHASE_READ')") public ApiResponse<Page<TextExtraction>> list(@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size,@RequestParam(required=false)ExtractionStatus status,@RequestParam(required=false)ExtractionMethod method,@RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE)LocalDate from,@RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE)LocalDate to){return ApiResponse.success(service.list(page,size,status,method,from,to));}
 @GetMapping("/{id}") @PreAuthorize("hasAuthority('KAPAS_PURCHASE_READ')") public ApiResponse<TextExtraction> get(@PathVariable UUID id){return ApiResponse.success(service.get(id));}
 @GetMapping("/number/{number}") @PreAuthorize("hasAuthority('KAPAS_PURCHASE_READ')") public ApiResponse<TextExtraction> byNumber(@PathVariable String number){return ApiResponse.success(service.getByNumber(number));}
 @PutMapping("/{id}") @PreAuthorize("hasAuthority('KAPAS_PURCHASE_UPDATE')") public ApiResponse<TextExtraction> update(@PathVariable UUID id,@RequestBody UpdateTextExtractionRequest r){return ApiResponse.success(service.update(id,r),"Extraction reviewed");}
}
