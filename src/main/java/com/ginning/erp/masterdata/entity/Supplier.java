package com.ginning.erp.masterdata.entity;

import com.ginning.erp.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
public class Supplier extends BaseEntity {
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

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }
    public String getPan() { return pan; }
    public void setPan(String pan) { this.pan = pan; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
