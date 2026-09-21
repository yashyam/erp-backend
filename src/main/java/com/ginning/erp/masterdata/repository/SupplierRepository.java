package com.ginning.erp.masterdata.repository;
import com.ginning.erp.masterdata.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    List<Supplier> findByDeletedAtIsNullOrderByNameAsc();
    Optional<Supplier> findByIdAndDeletedAtIsNull(UUID id);
    Optional<Supplier> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);
    boolean existsByCodeIgnoreCaseAndDeletedAtIsNull(String code);
    boolean existsByCodeIgnoreCaseAndDeletedAtIsNullAndIdNot(String code, UUID id);
}
