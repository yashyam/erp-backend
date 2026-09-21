package com.ginning.erp.bale.repository;

import com.ginning.erp.bale.entity.BaleProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BaleProductionRepository extends JpaRepository<BaleProduction, UUID> {
    List<BaleProduction> findByDeletedAtIsNullOrderByProductionDateDescCreatedAtDesc();

    Optional<BaleProduction> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByBaleNumberIgnoreCaseAndDeletedAtIsNull(String baleNumber);

    @Query(value = "select coalesce(max(serial_no), 0) from bale_production where lot_number = :lotNumber", nativeQuery = true)
    long getMaxSerialNoForLot(@Param("lotNumber") String lotNumber);

    @Query(value = "select pg_advisory_xact_lock(hashtextextended(:lotNumber, 0))", nativeQuery = true)
    void lockLotForSerialAllocation(@Param("lotNumber") String lotNumber);

    @Query(value = "select coalesce(sum(bale_weight), 0) from bale_production where lot_id = :lotId and deleted_at is null", nativeQuery = true)
    java.math.BigDecimal getProducedWeightForLot(@Param("lotId") UUID lotId);

    @Query(value = "select count(*) from bale_production where lot_id = :lotId and deleted_at is null", nativeQuery = true)
    long countActiveForLot(@Param("lotId") UUID lotId);

    @Query("select b from BaleProduction b where b.deletedAt is null and b.productionDate between :fromDate and :toDate order by b.productionDate desc, b.lotNumber asc, b.serialNo asc")
    List<BaleProduction> findByProductionDateBetweenAndDeletedAtIsNull(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "select nextval('bale_number_seq')", nativeQuery = true)
    long nextNumber();
}
