package com.ginning.erp.document.repository;
import com.ginning.erp.document.entity.*; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface TextExtractionRepository extends JpaRepository<TextExtraction,UUID>, JpaSpecificationExecutor<TextExtraction> {
 Optional<TextExtraction> findByExtractionNumber(String number);
 @Query(value="select nextval('text_extraction_number_seq')", nativeQuery=true) long nextNumber();
}
