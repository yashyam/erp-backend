package com.ginning.erp.masterdata.service;
import com.ginning.erp.common.exception.ApplicationException;
import com.ginning.erp.masterdata.dto.*;
import com.ginning.erp.masterdata.entity.Customer;
import com.ginning.erp.masterdata.repository.CustomerRepository;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime; import java.util.List; import java.util.UUID;
@Service public class CustomerService {
 private final CustomerRepository repository; public CustomerService(CustomerRepository repository){this.repository=repository;}
 @Transactional(readOnly=true) public List<CustomerResponse> list(){return repository.findByDeletedAtIsNullOrderByNameAsc().stream().map(CustomerResponse::from).toList();}
 @Transactional(readOnly=true) public CustomerResponse get(UUID id){return CustomerResponse.from(find(id));}
 @Transactional public CustomerResponse create(CustomerRequest r){if(repository.existsByCodeIgnoreCaseAndDeletedAtIsNull(r.getCode().trim()))throw duplicate();Customer e=new Customer();copy(r,e);return CustomerResponse.from(repository.save(e));}
 @Transactional public CustomerResponse update(UUID id,CustomerRequest r){Customer e=find(id);if(repository.existsByCodeIgnoreCaseAndDeletedAtIsNullAndIdNot(r.getCode().trim(),id))throw duplicate();copy(r,e);return CustomerResponse.from(repository.save(e));}
 @Transactional public void delete(UUID id){Customer e=find(id);e.setDeletedAt(LocalDateTime.now());repository.save(e);}
 private Customer find(UUID id){return repository.findByIdAndDeletedAtIsNull(id).orElseThrow(()->new ApplicationException("CUSTOMER_NOT_FOUND","Customer not found",404));}
 private ApplicationException duplicate(){return new ApplicationException("CUSTOMER_CODE_EXISTS","Customer code already exists",409);}
 private void copy(CustomerRequest r,Customer e){e.setCode(r.getCode().trim());e.setName(r.getName().trim());e.setContactPerson(t(r.getContactPerson()));e.setPhone(t(r.getPhone()));e.setEmail(t(r.getEmail()));e.setAddress(t(r.getAddress()));e.setGstin(t(r.getGstin()));e.setPan(t(r.getPan()));}
 private String t(String v){return v==null?null:v.trim();}
}
