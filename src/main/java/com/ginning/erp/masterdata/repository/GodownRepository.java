package com.ginning.erp.masterdata.repository;
import com.ginning.erp.masterdata.entity.Godown;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface GodownRepository extends JpaRepository<Godown, UUID> {
    List<Godown> findByDeletedAtIsNullOrderByNameAsc();
    Optional<Godown> findByIdAndDeletedAtIsNull(UUID id);
    boolean existsByCodeIgnoreCaseAndDeletedAtIsNull(String code);
    boolean existsByCodeIgnoreCaseAndDeletedAtIsNullAndIdNot(String code, UUID id);
}
