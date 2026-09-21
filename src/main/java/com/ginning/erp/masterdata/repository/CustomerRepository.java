package com.ginning.erp.masterdata.repository;
import com.ginning.erp.masterdata.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    List<Customer> findByDeletedAtIsNullOrderByNameAsc();
    Optional<Customer> findByIdAndDeletedAtIsNull(UUID id);
    boolean existsByCodeIgnoreCaseAndDeletedAtIsNull(String code);
    boolean existsByCodeIgnoreCaseAndDeletedAtIsNullAndIdNot(String code, UUID id);
}
