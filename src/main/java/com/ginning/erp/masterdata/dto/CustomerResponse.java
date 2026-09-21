package com.ginning.erp.masterdata.dto;

import com.ginning.erp.masterdata.entity.Customer;
import java.time.LocalDateTime;
import java.util.UUID;

public class CustomerResponse extends SupplierResponse {
    public static CustomerResponse from(Customer e) { CustomerResponse r = new CustomerResponse(); r.setValues(e); return r; }
    private void setValues(Customer e) { setId(e.getId()); setCode(e.getCode()); setName(e.getName()); setContactPerson(e.getContactPerson()); setPhone(e.getPhone()); setEmail(e.getEmail()); setAddress(e.getAddress()); setGstin(e.getGstin()); setPan(e.getPan()); setCreatedAt(e.getCreatedAt()); setUpdatedAt(e.getUpdatedAt()); }
}
