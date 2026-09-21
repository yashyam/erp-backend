package com.ginning.erp.masterdata.entity;

import com.ginning.erp.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "godowns")
public class Godown extends BaseEntity {
    @Column(nullable = false, length = 50)
    private String code;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(length = 500)
    private String address;
    @Column(name = "contact_person", length = 150)
    private String contactPerson;
    @Column(length = 20)
    private String phone;
    @Column(precision = 15, scale = 3)
    private java.math.BigDecimal capacity;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public String getCode() { return code; } public void setCode(String v) { code = v; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public String getAddress() { return address; } public void setAddress(String v) { address = v; }
    public String getContactPerson() { return contactPerson; } public void setContactPerson(String v) { contactPerson = v; }
    public String getPhone() { return phone; } public void setPhone(String v) { phone = v; }
    public java.math.BigDecimal getCapacity() { return capacity; } public void setCapacity(java.math.BigDecimal v) { capacity = v; }
    public LocalDateTime getDeletedAt() { return deletedAt; } public void setDeletedAt(LocalDateTime v) { deletedAt = v; }
}
