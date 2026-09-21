package com.ginning.erp.document.service;

import com.ginning.erp.audit.entity.AuditLog; import com.ginning.erp.audit.repository.AuditLogRepository; import com.ginning.erp.common.exception.ResourceNotFoundException; import com.ginning.erp.document.dto.*; import com.ginning.erp.document.entity.*; import com.ginning.erp.document.parser.*; import com.ginning.erp.document.repository.TextExtractionRepository; import org.springframework.data.domain.*; import org.springframework.data.jpa.domain.Specification; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.*;

@Service
public class TextExtractionService {
 private final TextExtractionRepository repo; private final TextParser parser; private final AuditLogRepository audit; private final FileTextExtractor fileTextExtractor;
 public TextExtractionService(TextExtractionRepository repo,TextParser parser,AuditLogRepository audit,FileTextExtractor fileTextExtractor){this.repo=repo;this.parser=parser;this.audit=audit;this.fileTextExtractor=fileTextExtractor;}
 private String number(){return String.format("EXT-%d-%06d", java.time.Year.now().getValue(), repo.nextNumber());}
 @Transactional public TextExtraction create(CreateTextExtractionRequest req){ TextExtraction e=new TextExtraction(number(),req.sourceText()); e.setStatus(ExtractionStatus.PROCESSING); try{apply(e,parser.parse(req.sourceText())); e.setExtractionMethod(ExtractionMethod.RULE_BASED); e.setStatus(e.getConfidenceScore()!=null&&e.getConfidenceScore().compareTo(new BigDecimal("0.5"))>=0?ExtractionStatus.COMPLETED:ExtractionStatus.REVIEW_REQUIRED);}catch(Exception ex){e.setStatus(ExtractionStatus.FAILED);e.setErrorMessage(ex.getMessage());} TextExtraction saved=repo.save(e); audit(saved,"CREATE"); return saved; }
 /** Extracts text from an attached bill (PDF/txt) and runs the same parsing pipeline. */
 @Transactional public TextExtraction createFromFile(org.springframework.web.multipart.MultipartFile file){
  TextExtraction saved=create(new CreateTextExtractionRequest(fileTextExtractor.extract(file)));
  saved.setSourceFileName(file.getOriginalFilename());
  saved.setSourceFileType(file.getContentType());
  saved.setSourceFileSize(file.getSize());
  return repo.save(saved);
 }
 private void apply(TextExtraction e,ExtractionResult r){e.setSupplierName(r.supplierName());e.setInvoiceNumber(r.invoiceNumber());e.setInvoiceDate(r.invoiceDate());e.setVehicleNumber(r.vehicleNumber());e.setQuantity(r.quantity());e.setRate(r.rate());e.setTax(r.tax());e.setTotalAmount(r.totalAmount());e.setExtractedData(r.data());e.setConfidenceScore(r.confidenceScore());}
 public TextExtraction get(UUID id){return repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Text extraction not found"));}
 public TextExtraction getByNumber(String n){return repo.findByExtractionNumber(n).orElseThrow(()->new ResourceNotFoundException("Text extraction not found"));}
 public Page<TextExtraction> list(int page,int size,ExtractionStatus status,ExtractionMethod method,LocalDate from,LocalDate to){Specification<TextExtraction> s=Specification.where(null); if(status!=null)s=s.and((r,q,c)->c.equal(r.get("status"),status)); if(method!=null)s=s.and((r,q,c)->c.equal(r.get("extractionMethod"),method)); if(from!=null)s=s.and((r,q,c)->c.greaterThanOrEqualTo(r.get("createdAt"),from.atStartOfDay())); if(to!=null)s=s.and((r,q,c)->c.lessThan(r.get("createdAt"),to.plusDays(1).atStartOfDay())); return repo.findAll(s,PageRequest.of(Math.max(0,page),Math.min(Math.max(size,1),100),Sort.by(Sort.Direction.DESC,"createdAt")));}
 @Transactional public TextExtraction update(UUID id,UpdateTextExtractionRequest r){TextExtraction e=get(id);e.setSupplierName(r.supplierName());e.setInvoiceNumber(r.invoiceNumber());e.setInvoiceDate(r.invoiceDate());e.setVehicleNumber(r.vehicleNumber());e.setQuantity(r.quantity());e.setRate(r.rate());e.setTax(r.tax());e.setTotalAmount(r.totalAmount());e.setExtractedData(r.extractedData());e.setExtractionMethod(ExtractionMethod.MANUAL);e.setStatus(ExtractionStatus.REVIEW_REQUIRED);e.setErrorMessage(null);TextExtraction saved=repo.save(e);audit(saved,"MANUAL_UPDATE");return saved;}
 private void audit(TextExtraction e,String action){try{audit.save(new AuditLog(null,action,"TEXT_EXTRACTION",e.getId()==null?e.getExtractionNumber():e.getId().toString()));}catch(Exception ignored){}}
}
