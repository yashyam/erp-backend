package com.ginning.erp.masterdata.dto;

import com.ginning.erp.masterdata.entity.Supplier;
import java.time.LocalDateTime;
import java.util.UUID;

public class SupplierResponse {
    private UUID id; private String code; private String name; private String contactPerson; private String phone; private String email; private String address; private String gstin; private String pan; private LocalDateTime createdAt; private LocalDateTime updatedAt;
    public static SupplierResponse from(Supplier e) { SupplierResponse r = new SupplierResponse(); r.id=e.getId(); r.code=e.getCode(); r.name=e.getName(); r.contactPerson=e.getContactPerson(); r.phone=e.getPhone(); r.email=e.getEmail(); r.address=e.getAddress(); r.gstin=e.getGstin(); r.pan=e.getPan(); r.createdAt=e.getCreatedAt(); r.updatedAt=e.getUpdatedAt(); return r; }
    public UUID getId(){return id;} public String getCode(){return code;} public String getName(){return name;} public String getContactPerson(){return contactPerson;} public String getPhone(){return phone;} public String getEmail(){return email;} public String getAddress(){return address;} public String getGstin(){return gstin;} public String getPan(){return pan;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
    protected void setId(UUID v){id=v;} protected void setCode(String v){code=v;} protected void setName(String v){name=v;} protected void setContactPerson(String v){contactPerson=v;} protected void setPhone(String v){phone=v;} protected void setEmail(String v){email=v;} protected void setAddress(String v){address=v;} protected void setGstin(String v){gstin=v;} protected void setPan(String v){pan=v;} protected void setCreatedAt(LocalDateTime v){createdAt=v;} protected void setUpdatedAt(LocalDateTime v){updatedAt=v;}
}
