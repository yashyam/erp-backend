package com.ginning.erp.bale.service;

import com.ginning.erp.auth.repository.UserRepository;
import com.ginning.erp.bale.dto.BaleDailySummaryResponse;
import com.ginning.erp.bale.dto.BaleProductionRequest;
import com.ginning.erp.bale.dto.BaleProductionResponse;
import com.ginning.erp.bale.entity.BaleProduction;
import com.ginning.erp.bale.repository.BaleProductionRepository;
import com.ginning.erp.common.exception.ApplicationException;
import com.ginning.erp.common.exception.ResourceNotFoundException;
import com.ginning.erp.kapas.entity.KapasPurchaseEntry;
import com.ginning.erp.kapas.repository.KapasPurchaseEntryRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BaleProductionService {
    private final BaleProductionRepository repository;
    private final KapasPurchaseEntryRepository purchaseRepository;
    private final UserRepository userRepository;

    public BaleProductionService(BaleProductionRepository repository,
                                KapasPurchaseEntryRepository purchaseRepository,
                                UserRepository userRepository) {
        this.repository = repository;
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<BaleProductionResponse> list() {
        return repository.findByDeletedAtIsNullOrderByProductionDateDescCreatedAtDesc().stream()
                .map(BaleProductionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BaleProductionResponse get(UUID id) {
        return BaleProductionResponse.from(find(id));
    }

    @Transactional
    public void delete(UUID id) {
        BaleProduction bale = find(id);
        repository.lockLotForSerialAllocation(bale.getLotNumber());
        bale.setDeletedAt(LocalDateTime.now());
        repository.save(bale);

        if (repository.countActiveForLot(bale.getLotId()) == 0) {
            KapasPurchaseEntry lot = purchaseRepository.findByIdAndDeletedAtIsNull(bale.getLotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lot not found"));
            lot.setLotStatus("YET_TO_START");
            lot.setStatusUpdatedAt(LocalDateTime.now());
            purchaseRepository.save(lot);
        }
    }

    @Transactional(readOnly = true)
    public List<BaleDailySummaryResponse> dailySummary(LocalDate fromDate, LocalDate toDate) {
        LocalDate start = fromDate == null ? LocalDate.now().minusDays(30) : fromDate;
        LocalDate end = toDate == null ? LocalDate.now() : toDate;
        if (start.isAfter(end)) {
            throw new ApplicationException("SUMMARY_DATE_RANGE_INVALID", "From date cannot be after to date", 400);
        }

        Map<String, BaleDailySummaryResponse> grouped = new LinkedHashMap<>();
        for (BaleProduction bale : repository.findByProductionDateBetweenAndDeletedAtIsNull(start, end)) {
            String key = bale.getProductionDate() + "|" + bale.getLotNumber();
            BaleDailySummaryResponse summary = grouped.computeIfAbsent(key, ignored -> {
                BaleDailySummaryResponse item = new BaleDailySummaryResponse();
                item.setProductionDate(bale.getProductionDate());
                item.setLotNumber(bale.getLotNumber());
                item.setTotalBales(0);
                item.setStartSerialNo(bale.getSerialNo());
                item.setEndSerialNo(bale.getSerialNo());
                item.setTotalBaleWeight(BigDecimal.ZERO);
                item.setTotalQuintals(BigDecimal.ZERO);
                item.setTotalCandy(BigDecimal.ZERO);
                return item;
            });

            summary.setTotalBales(summary.getTotalBales() + 1);
            summary.setStartSerialNo(Math.min(summary.getStartSerialNo(), bale.getSerialNo()));
            summary.setEndSerialNo(Math.max(summary.getEndSerialNo(), bale.getSerialNo()));
            if (summary.getFirstBaleNumber() == null || bale.getBaleNumber().compareTo(summary.getFirstBaleNumber()) < 0) {
                summary.setFirstBaleNumber(bale.getBaleNumber());
            }
            if (summary.getLastBaleNumber() == null || bale.getBaleNumber().compareTo(summary.getLastBaleNumber()) > 0) {
                summary.setLastBaleNumber(bale.getBaleNumber());
            }
            summary.setTotalBaleWeight(summary.getTotalBaleWeight().add(bale.getBaleWeight()));
            summary.setTotalQuintals(summary.getTotalQuintals().add(bale.getQuintals() != null ? bale.getQuintals() : computeQuintals(bale.getBaleWeight())));
            summary.setTotalCandy(summary.getTotalCandy().add(bale.getCandy() != null ? bale.getCandy() : computeCandy(bale.getBaleWeight())));
        }

        return new ArrayList<>(grouped.values());
    }

    @Transactional
    public BaleProductionResponse create(BaleProductionRequest request) {
        String lotNumber = request.getLotNumber() == null ? null : request.getLotNumber().trim();
        if (lotNumber == null || lotNumber.isBlank()) {
            throw new ApplicationException("BALE_LOT_REQUIRED", "Lot number is required", 400);
        }
        if (request.getProductionDate() == null) {
            throw new ApplicationException("BALE_DATE_REQUIRED", "Production date is required", 400);
        }
        if (request.getBaleWeight() == null || request.getBaleWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApplicationException("BALE_WEIGHT_INVALID", "Bale weight must be greater than zero", 400);
        }
        if (request.getCandy() != null && request.getCandy().compareTo(BigDecimal.ZERO) < 0) {
            throw new ApplicationException("BALE_CANDY_INVALID", "Candy must not be negative", 400);
        }
        if (request.getQuintals() != null && request.getQuintals().compareTo(BigDecimal.ZERO) < 0) {
            throw new ApplicationException("BALE_QUINTALS_INVALID", "Quintals must not be negative", 400);
        }

        KapasPurchaseEntry lot = resolveLot(request.getLotId(), lotNumber);
        if (request.getLotId() != null && !lot.getLotNumber().equalsIgnoreCase(lotNumber)) {
            throw new ApplicationException("BALE_LOT_MISMATCH", "Selected lot does not match the submitted lot number", 400);
        }
        if (request.getProductionDate().isBefore(lot.getBillDate())) {
            throw new ApplicationException("BALE_DATE_INVALID", "Production date cannot be before the lot purchase date", 400);
        }

        repository.lockLotForSerialAllocation(lot.getLotNumber());
        long nextSerialNo = repository.getMaxSerialNoForLot(lot.getLotNumber()) + 1L;
        BigDecimal producedWeight = repository.getProducedWeightForLot(lot.getId());
        BigDecimal requestedWeight = request.getBaleWeight().setScale(3, RoundingMode.HALF_UP);
        BigDecimal availableWeight = lot.getNetKapasWeight();
        if (availableWeight != null && producedWeight.add(requestedWeight).compareTo(availableWeight) > 0) {
            throw new ApplicationException("BALE_WEIGHT_EXCEEDS_LOT", "Bale production weight exceeds the available lot weight", 400);
        }
        String baleNumber = String.format("BALE-%d-%06d", LocalDate.now().getYear(), repository.nextNumber());

        BaleProduction bale = new BaleProduction();
        bale.setBaleNumber(baleNumber);
        bale.setLotId(lot.getId());
        bale.setLotNumber(lot.getLotNumber());
        bale.setProductionDate(request.getProductionDate());
        bale.setSerialNo(nextSerialNo);
        bale.setBaleWeight(requestedWeight);
        bale.setCandy(request.getCandy() != null ? request.getCandy().setScale(3, RoundingMode.HALF_UP) : computeCandy(request.getBaleWeight()));
        bale.setQuintals(request.getQuintals() != null ? request.getQuintals().setScale(3, RoundingMode.HALF_UP) : computeQuintals(request.getBaleWeight()));
        currentUserId().ifPresent(bale::setCreatedBy);

        lot.setLotStatus("RUNNING");
        lot.setStatusUpdatedAt(LocalDateTime.now());
        purchaseRepository.save(lot);

        BaleProduction saved = repository.save(bale);
        return BaleProductionResponse.from(saved);
    }

    private KapasPurchaseEntry resolveLot(UUID lotId, String lotNumber) {
        if (lotId != null) {
            return purchaseRepository.findByIdAndDeletedAtIsNull(lotId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lot not found"));
        }
        return purchaseRepository.findByLotNumberIgnoreCaseAndDeletedAtIsNull(lotNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Lot not found"));
    }

    private BaleProduction find(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bale not found"));
    }

    private static BigDecimal computeQuintals(BigDecimal baleWeight) {
        return baleWeight.divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP);
    }

    private static BigDecimal computeCandy(BigDecimal baleWeight) {
        return baleWeight.divide(BigDecimal.valueOf(355), 3, RoundingMode.HALF_UP);
    }

    private static java.util.Optional<UUID> currentUserId() {
        String principal = SecurityContextHolder.getContext().getAuthentication() == null ? null : SecurityContextHolder.getContext().getAuthentication().getName();
        if (principal == null || principal.isBlank()) return java.util.Optional.empty();
        return java.util.Optional.ofNullable(principal).flatMap(value -> {
            try {
                return java.util.Optional.of(UUID.fromString(value));
            } catch (IllegalArgumentException ignored) {
                return java.util.Optional.empty();
            }
        });
    }
}
