package com.ginning.erp.sales.repository;

import com.ginning.erp.sales.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface SaleRepository extends JpaRepository<Sale, UUID> {
    List<Sale> findByDeletedAtIsNullOrderByRawMaterialOutDateDescCreatedAtDesc();
    List<Sale> findByLotNumberIgnoreCaseAndDeletedAtIsNullOrderByRawMaterialOutDateDescCreatedAtDesc(String lotNumber);
    Optional<Sale> findByIdAndDeletedAtIsNull(UUID id);
    Optional<Sale> findBySalesIdIgnoreCaseAndDeletedAtIsNull(String salesId);
    Optional<Sale> findByBillIdIgnoreCaseAndDeletedAtIsNull(String billId);
    boolean existsByBillIdIgnoreCaseAndDeletedAtIsNull(String billId);
    @org.springframework.data.jpa.repository.Query(value="select nextval('sales_number_seq')", nativeQuery=true) long nextNumber();
}
