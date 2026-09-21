package com.ginning.erp.kapas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ginning.erp.audit.entity.AuditLog;
import com.ginning.erp.audit.repository.AuditLogRepository;
import com.ginning.erp.auth.repository.UserRepository;
import com.ginning.erp.common.exception.ApplicationException;
import com.ginning.erp.common.exception.ResourceNotFoundException;
import com.ginning.erp.document.entity.TextExtraction;
import com.ginning.erp.document.repository.TextExtractionRepository;
import com.ginning.erp.kapas.dto.KapasPurchaseEntryRequest;
import com.ginning.erp.kapas.dto.KapasPurchaseEntryResponse;
import com.ginning.erp.kapas.entity.KapasPurchaseEntry;
import com.ginning.erp.kapas.repository.KapasPurchaseEntryRepository;
import com.ginning.erp.masterdata.entity.Godown;
import com.ginning.erp.masterdata.entity.Supplier;
import com.ginning.erp.masterdata.repository.GodownRepository;
import com.ginning.erp.masterdata.repository.SupplierRepository;

@Service
public class KapasPurchaseEntryService {

    private final KapasPurchaseEntryRepository repository;
    private final GodownRepository godownRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final TextExtractionRepository extractionRepository;
    private final AuditLogRepository auditLogRepository;

    public KapasPurchaseEntryService(KapasPurchaseEntryRepository repository, GodownRepository godownRepository,
                                     SupplierRepository supplierRepository, UserRepository userRepository,
                                     TextExtractionRepository extractionRepository, AuditLogRepository auditLogRepository) {
        this.repository = repository; this.godownRepository = godownRepository;
        this.supplierRepository = supplierRepository; this.userRepository = userRepository;
        this.extractionRepository = extractionRepository; this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<KapasPurchaseEntryResponse> list() {
        return repository.findByDeletedAtIsNullOrderByBillDateDescCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public KapasPurchaseEntryResponse get(UUID id) { return toResponse(find(id)); }

    @Transactional
    public KapasPurchaseEntryResponse create(KapasPurchaseEntryRequest request) {
        request.setLotNumber(nextLotNumber());
        resolveSupplier(request);
        validate(request);
        if (repository.existsByBillNumberIgnoreCaseAndSupplierNameIgnoreCaseAndDeletedAtIsNull(request.getBillNumber().trim(), request.getSupplierName().trim()))
            throw duplicate();
        KapasPurchaseEntry entry = new KapasPurchaseEntry();
        entry.setEntryNumber(nextEntryNumber());
        currentUserId().ifPresent(entry::setCreatedBy);
        copy(request, entry);
        KapasPurchaseEntry saved = repository.save(entry);
        audit(saved, "CREATE");
        return toResponse(saved);
    }

    @Transactional
    public KapasPurchaseEntryResponse update(UUID id, KapasPurchaseEntryRequest request) {
        resolveSupplier(request);
        validate(request);
        KapasPurchaseEntry entry = find(id);
        if (repository.existsByBillNumberIgnoreCaseAndSupplierNameIgnoreCaseAndDeletedAtIsNullAndIdNot(request.getBillNumber().trim(), request.getSupplierName().trim(), id))
            throw duplicate();
        copy(request, entry);
        KapasPurchaseEntry saved = repository.save(entry);
        audit(saved, "UPDATE");
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        KapasPurchaseEntry entry = find(id);
        entry.setDeletedAt(LocalDateTime.now());
        repository.save(entry);
        audit(entry, "DELETE");
    }

    /** Explicitly user-invoked creation of an entry pre-filled from a reviewed text extraction. */
    @Transactional
    public KapasPurchaseEntryResponse createFromExtraction(UUID extractionId) {
        TextExtraction extraction = extractionRepository.findById(extractionId)
            .orElseThrow(() -> new ResourceNotFoundException("Text extraction not found"));
        KapasPurchaseEntryRequest request = requestFromExtraction(extraction);
        return create(request);
    }

        @Transactional(readOnly = true)
        public String duplicateMessageFromExtraction(UUID extractionId) {
        TextExtraction extraction = extractionRepository.findById(extractionId)
            .orElseThrow(() -> new ResourceNotFoundException("Text extraction not found"));
        KapasPurchaseEntryRequest request = requestFromExtraction(extraction);
        return repository.existsByBillNumberIgnoreCaseAndSupplierNameIgnoreCaseAndDeletedAtIsNull(
            request.getBillNumber().trim(), request.getSupplierName().trim())
            ? "A kapas purchase entry with this bill number already exists for this supplier"
            : null;
        }

        private KapasPurchaseEntryRequest requestFromExtraction(TextExtraction extraction) {
        Map<String, Object> data = extraction.getExtractedData() == null ? Map.of() : extraction.getExtractedData();
        KapasPurchaseEntryRequest request = new KapasPurchaseEntryRequest();
        request.setBillDate(extraction.getInvoiceDate() != null ? extraction.getInvoiceDate() : LocalDate.now());
        request.setSupplierName(blankToDefault(extraction.getSupplierName(), "Unknown supplier"));
        request.setBillNumber(blankToDefault(extraction.getInvoiceNumber(), extraction.getExtractionNumber()));
        request.setVehicleNumber(extraction.getVehicleNumber());
        request.setNumberOfBags(decimal(data.get("numberOfBags")));
        BigDecimal grossWeight = decimal(data.get("grossWeight"));
        BigDecimal tareWeight = decimal(data.get("tareWeight"));
        request.setGrossWeight(grossWeight);
        request.setTareWeight(tareWeight);
        request.setNetKapasWeight(grossWeight != null && tareWeight != null
            ? grossWeight.subtract(tareWeight)
            : decimal(data.get("netWeight")) != null ? decimal(data.get("netWeight")) : extraction.getQuantity());
        request.setRate(extraction.getRate());
        if (request.getNetKapasWeight() != null && extraction.getRate() != null)
            request.setAmount(request.getNetKapasWeight().divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP)
                .multiply(extraction.getRate()).setScale(2, RoundingMode.HALF_UP));
        request.setLotNumber(text(data.get("lotNumber")));
        request.setTextExtractionId(extraction.getId());
        String godownName = text(data.get("godown"));
        if (godownName != null) godownRepository.findByDeletedAtIsNullOrderByNameAsc().stream()
            .filter(g -> g.getName() != null && g.getName().equalsIgnoreCase(godownName))
            .findFirst().ifPresent(g -> request.setGodownId(g.getId()));
        return request;
        }

    private KapasPurchaseEntry find(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kapas purchase entry not found"));
    }

    private String nextEntryNumber() {
        return String.format("KPE-%d-%06d", java.time.Year.now().getValue(), repository.nextNumber());
    }

    private String nextLotNumber() {
        return String.format("LOT-%d-%06d", java.time.Year.now().getValue(), repository.nextLotNumber());
    }

    private void validate(KapasPurchaseEntryRequest r) {
        if (r.getSupplierId() == null)
            throw new ApplicationException("SUPPLIER_REQUIRED", "Select a supplier from the supplier master", 400);
        if (r.getBillDate().isAfter(LocalDate.now()))
            throw new ApplicationException("KAPAS_FUTURE_DATE", "Purchase date cannot be in the future", 400);
        requireNonNegative("grossWeight", r.getGrossWeight());
        requireNonNegative("tareWeight", r.getTareWeight());
        requireNonNegative("numberOfBags", r.getNumberOfBags());
        requireNonNegative("netKapasWeight", r.getNetKapasWeight());
        if (r.getRate() == null || r.getRate().signum() <= 0)
            throw new ApplicationException("KAPAS_RATE_REQUIRED", "Rate per quintal must be greater than zero", 400);
        if (r.getNetKapasWeight() == null && r.getGrossWeight() != null && r.getTareWeight() != null) {
            BigDecimal net = r.getGrossWeight().subtract(r.getTareWeight());
            if (net.signum() <= 0)
                throw new ApplicationException("KAPAS_INVALID_WEIGHTS", "Tare weight cannot be greater than or equal to gross weight", 400);
            r.setNetKapasWeight(net.setScale(3, RoundingMode.HALF_UP));
        }
        if (r.getGrossWeight() != null && r.getTareWeight() != null) {
            BigDecimal net = r.getGrossWeight().subtract(r.getTareWeight());
            if (net.signum() <= 0)
                throw new ApplicationException("KAPAS_INVALID_WEIGHTS", "Tare weight cannot be greater than or equal to gross weight", 400);
            r.setNetKapasWeight(net.setScale(3, RoundingMode.HALF_UP));
        }
        if (r.getNetKapasWeight() != null && r.getRate() != null) {
            r.setAmount(r.getNetKapasWeight().divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP)
                .multiply(r.getRate()).setScale(2, RoundingMode.HALF_UP));
        }
        if (r.getGodownId() != null && godownRepository.findByIdAndDeletedAtIsNull(r.getGodownId()).isEmpty())
            throw new ApplicationException("GODOWN_NOT_FOUND", "Godown not found", 404);
        if (r.getTextExtractionId() != null && !extractionRepository.existsById(r.getTextExtractionId()))
            throw new ResourceNotFoundException("Text extraction not found");
    }

    private void requireNonNegative(String field, BigDecimal value) {
        if (value != null && value.signum() < 0)
            throw new ApplicationException("KAPAS_NEGATIVE_VALUE", field + " cannot be negative", 400);
    }

    private void copy(KapasPurchaseEntryRequest r, KapasPurchaseEntry e) {
        e.setBillDate(r.getBillDate());
        e.setSupplierName(r.getSupplierName().trim());
        e.setSupplierId(r.getSupplierId());
        e.setBillNumber(r.getBillNumber().trim());
        e.setVehicleNumber(trim(r.getVehicleNumber()));
        e.setNumberOfBags(r.getNumberOfBags());
        e.setGrossWeight(r.getGrossWeight());
        e.setTareWeight(r.getTareWeight());
        e.setNetKapasWeight(r.getNetKapasWeight());
        e.setRate(r.getRate());
        e.setQuintals(r.getNetKapasWeight().divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP));
        e.setAmount(r.getAmount());
        e.setLotNumber(trim(r.getLotNumber()));
        e.setGodownId(r.getGodownId());
        e.setTextExtractionId(r.getTextExtractionId());
    }

    private KapasPurchaseEntryResponse toResponse(KapasPurchaseEntry e) {
        String godownName = null;
        if (e.getGodownId() != null)
            godownName = godownRepository.findById(e.getGodownId()).map(Godown::getName).orElse(null);
        return KapasPurchaseEntryResponse.from(e, godownName);
    }

    private void resolveSupplier(KapasPurchaseEntryRequest request) {
        Supplier supplier = request.getSupplierId() != null
            ? supplierRepository.findByIdAndDeletedAtIsNull(request.getSupplierId()).orElseThrow(() -> new ApplicationException("SUPPLIER_NOT_FOUND", "Supplier not found", 404))
            : supplierRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(request.getSupplierName().trim()).orElse(null);
        if (supplier != null) {
            request.setSupplierId(supplier.getId());
            request.setSupplierName(supplier.getName());
        }
    }

    private Optional<UUID> currentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return Optional.empty();
        return userRepository.findByUsernameAndDeletedAtIsNull(authentication.getName()).map(com.ginning.erp.auth.entity.User::getId);
    }

    private ApplicationException duplicate() {
        return new ApplicationException("KAPAS_BILL_EXISTS", "A kapas purchase entry with this bill number already exists for this supplier", 409);
    }

    private void audit(KapasPurchaseEntry e, String action) {
        try {
            auditLogRepository.save(new AuditLog(null, action, "KAPAS_PURCHASE_ENTRY",
                    e.getId() == null ? e.getEntryNumber() : e.getId().toString()));
        } catch (Exception ignored) { }
    }

    private static String trim(String v) { return v == null || v.isBlank() ? null : v.trim(); }
    private static String blankToDefault(String v, String fallback) { return v == null || v.isBlank() ? fallback : v.trim(); }
    private static String text(Object v) { return v == null || v.toString().isBlank() ? null : v.toString().trim(); }

    private static BigDecimal decimal(Object v) {
        if (v == null) return null;
        if (v instanceof BigDecimal b) return b;
        if (v instanceof Number n) return new BigDecimal(n.toString());
        try { return new BigDecimal(v.toString().replace(",", "").trim()); } catch (NumberFormatException e) { return null; }
    }
}
