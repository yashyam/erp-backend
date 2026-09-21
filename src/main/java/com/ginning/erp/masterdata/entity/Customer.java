package com.ginning.erp.masterdata.entity;

import com.ginning.erp.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {
    @Column(nullable = false, length = 50)
    private String code;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(name = "contact_person", length = 150)
    private String contactPerson;
    @Column(length = 20)
    private String phone;
    @Column(length = 255)
    private String email;
    @Column(length = 500)
    private String address;
    @Column(length = 20)
    private String gstin;
    @Column(length = 20)
    private String pan;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public String getCode() { return code; } public void setCode(String v) { code = v; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public String getContactPerson() { return contactPerson; } public void setContactPerson(String v) { contactPerson = v; }
    public String getPhone() { return phone; } public void setPhone(String v) { phone = v; }
    public String getEmail() { return email; } public void setEmail(String v) { email = v; }
    public String getAddress() { return address; } public void setAddress(String v) { address = v; }
    public String getGstin() { return gstin; } public void setGstin(String v) { gstin = v; }
    public String getPan() { return pan; } public void setPan(String v) { pan = v; }
    public LocalDateTime getDeletedAt() { return deletedAt; } public void setDeletedAt(LocalDateTime v) { deletedAt = v; }
}
