package com.ginning.erp.masterdata.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SupplierRequest {
    @NotBlank @Size(max = 50) private String code;
    @NotBlank @Size(max = 200) private String name;
    @Size(max = 150) private String contactPerson;
    @Size(max = 20) private String phone;
    @Email @Size(max = 255) private String email;
    @Size(max = 500) private String address;
    @Size(max = 20) private String gstin;
    @Size(max = 20) private String pan;
    public String getCode() { return code; } public void setCode(String v) { code = v; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public String getContactPerson() { return contactPerson; } public void setContactPerson(String v) { contactPerson = v; }
    public String getPhone() { return phone; } public void setPhone(String v) { phone = v; }
    public String getEmail() { return email; } public void setEmail(String v) { email = v; }
    public String getAddress() { return address; } public void setAddress(String v) { address = v; }
    public String getGstin() { return gstin; } public void setGstin(String v) { gstin = v; }
    public String getPan() { return pan; } public void setPan(String v) { pan = v; }
}
