package com.ginning.erp.masterdata.service;
import com.ginning.erp.common.exception.ApplicationException;
import com.ginning.erp.masterdata.dto.*;
import com.ginning.erp.masterdata.entity.Supplier;
import com.ginning.erp.masterdata.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime; import java.util.List; import java.util.UUID;
@Service
public class SupplierService {
 private final SupplierRepository repository;
 public SupplierService(SupplierRepository repository){this.repository=repository;}
 @Transactional(readOnly=true) public List<SupplierResponse> list(){return repository.findByDeletedAtIsNullOrderByNameAsc().stream().map(SupplierResponse::from).toList();}
 @Transactional(readOnly=true) public SupplierResponse get(UUID id){return SupplierResponse.from(find(id));}
 @Transactional public SupplierResponse create(SupplierRequest r){if(repository.existsByCodeIgnoreCaseAndDeletedAtIsNull(r.getCode().trim())) throw duplicate(); if(similarNameExists(r.getName(), null)) throw similarDuplicate(); Supplier e=new Supplier(); copy(r,e); return SupplierResponse.from(repository.save(e));}
 @Transactional public SupplierResponse update(UUID id,SupplierRequest r){Supplier e=find(id); if(repository.existsByCodeIgnoreCaseAndDeletedAtIsNullAndIdNot(r.getCode().trim(),id)) throw duplicate(); if(similarNameExists(r.getName(), id)) throw similarDuplicate(); copy(r,e); return SupplierResponse.from(repository.save(e));}
 @Transactional public void delete(UUID id){Supplier e=find(id);e.setDeletedAt(LocalDateTime.now());repository.save(e);}
 private Supplier find(UUID id){return repository.findByIdAndDeletedAtIsNull(id).orElseThrow(()->new ApplicationException("SUPPLIER_NOT_FOUND","Supplier not found",404));}
 private ApplicationException duplicate(){return new ApplicationException("SUPPLIER_CODE_EXISTS","Supplier code already exists",409);}
 private ApplicationException similarDuplicate(){return new ApplicationException("SUPPLIER_SIMILAR_EXISTS","A similar supplier already exists. Use the existing supplier instead.",409);}
 private boolean similarNameExists(String name, UUID excludedId){String normalized=normalize(name); return repository.findByDeletedAtIsNullOrderByNameAsc().stream().anyMatch(s -> !s.getId().equals(excludedId) && normalize(s.getName()).equals(normalized));}
 private String normalize(String value){return value == null ? "" : value.toLowerCase().replaceAll("[^a-z0-9]", "").replaceAll("(traders|trader|cotton|company|co)$", "");}
 private void copy(SupplierRequest r,Supplier e){e.setCode(r.getCode().trim());e.setName(r.getName().trim());e.setContactPerson(trim(r.getContactPerson()));e.setPhone(trim(r.getPhone()));e.setEmail(trim(r.getEmail()));e.setAddress(trim(r.getAddress()));e.setGstin(trim(r.getGstin()));e.setPan(trim(r.getPan()));}
 private String trim(String v){return v==null?null:v.trim();}
}
