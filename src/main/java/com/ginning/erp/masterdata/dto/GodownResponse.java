package com.ginning.erp.masterdata.dto;

import com.ginning.erp.masterdata.entity.Godown;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class GodownResponse {
    private UUID id; private String code; private String name; private String address; private String contactPerson; private String phone; private BigDecimal capacity; private LocalDateTime createdAt; private LocalDateTime updatedAt;
    public static GodownResponse from(Godown e) { GodownResponse r=new GodownResponse(); r.id=e.getId(); r.code=e.getCode(); r.name=e.getName(); r.address=e.getAddress(); r.contactPerson=e.getContactPerson(); r.phone=e.getPhone(); r.capacity=e.getCapacity(); r.createdAt=e.getCreatedAt(); r.updatedAt=e.getUpdatedAt(); return r; }
    public UUID getId(){return id;} public String getCode(){return code;} public String getName(){return name;} public String getAddress(){return address;} public String getContactPerson(){return contactPerson;} public String getPhone(){return phone;} public BigDecimal getCapacity(){return capacity;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
