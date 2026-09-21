package com.ginning.erp.kapas.repository;
import com.ginning.erp.kapas.entity.KapasPurchaseEntry; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface KapasPurchaseEntryRepository extends JpaRepository<KapasPurchaseEntry, UUID> {
 List<KapasPurchaseEntry> findByDeletedAtIsNullOrderByBillDateDescCreatedAtDesc();
 Optional<KapasPurchaseEntry> findByIdAndDeletedAtIsNull(UUID id);
 Optional<KapasPurchaseEntry> findByLotNumberIgnoreCaseAndDeletedAtIsNull(String lotNumber);
 boolean existsByBillNumberIgnoreCaseAndSupplierNameIgnoreCaseAndDeletedAtIsNull(String billNumber, String supplierName);
 boolean existsByBillNumberIgnoreCaseAndSupplierNameIgnoreCaseAndDeletedAtIsNullAndIdNot(String billNumber, String supplierName, UUID id);
 @Query(value="select nextval('kapas_purchase_entry_number_seq')", nativeQuery=true) long nextNumber();
	@Query(value="select nextval('kapas_purchase_lot_number_seq')", nativeQuery=true) long nextLotNumber();
}
