package com.ginning.erp.masterdata.service;
import com.ginning.erp.common.exception.ApplicationException;
import com.ginning.erp.masterdata.dto.*;
import com.ginning.erp.masterdata.entity.Godown;
import com.ginning.erp.masterdata.repository.GodownRepository;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime; import java.util.List; import java.util.UUID;
@Service public class GodownService {
 private final GodownRepository repository; public GodownService(GodownRepository repository){this.repository=repository;}
 @Transactional(readOnly=true) public List<GodownResponse> list(){return repository.findByDeletedAtIsNullOrderByNameAsc().stream().map(GodownResponse::from).toList();}
 @Transactional(readOnly=true) public GodownResponse get(UUID id){return GodownResponse.from(find(id));}
 @Transactional public GodownResponse create(GodownRequest r){if(repository.existsByCodeIgnoreCaseAndDeletedAtIsNull(r.getCode().trim()))throw duplicate();Godown e=new Godown();copy(r,e);return GodownResponse.from(repository.save(e));}
 @Transactional public GodownResponse update(UUID id,GodownRequest r){Godown e=find(id);if(repository.existsByCodeIgnoreCaseAndDeletedAtIsNullAndIdNot(r.getCode().trim(),id))throw duplicate();copy(r,e);return GodownResponse.from(repository.save(e));}
 @Transactional public void delete(UUID id){Godown e=find(id);e.setDeletedAt(LocalDateTime.now());repository.save(e);}
 private Godown find(UUID id){return repository.findByIdAndDeletedAtIsNull(id).orElseThrow(()->new ApplicationException("GODOWN_NOT_FOUND","Godown not found",404));}
 private ApplicationException duplicate(){return new ApplicationException("GODOWN_CODE_EXISTS","Godown code already exists",409);}
 private void copy(GodownRequest r,Godown e){e.setCode(r.getCode().trim());e.setName(r.getName().trim());e.setAddress(t(r.getAddress()));e.setContactPerson(t(r.getContactPerson()));e.setPhone(t(r.getPhone()));e.setCapacity(r.getCapacity());}
 private String t(String v){return v==null?null:v.trim();}
}
