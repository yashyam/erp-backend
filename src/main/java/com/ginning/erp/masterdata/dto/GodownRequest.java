package com.ginning.erp.masterdata.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class GodownRequest {
    @NotBlank @Size(max = 50) private String code;
    @NotBlank @Size(max = 200) private String name;
    @Size(max = 500) private String address;
    @Size(max = 150) private String contactPerson;
    @Size(max = 20) private String phone;
    @DecimalMin(value = "0.0", inclusive = false) private BigDecimal capacity;
    public String getCode() { return code; } public void setCode(String v) { code = v; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public String getAddress() { return address; } public void setAddress(String v) { address = v; }
    public String getContactPerson() { return contactPerson; } public void setContactPerson(String v) { contactPerson = v; }
    public String getPhone() { return phone; } public void setPhone(String v) { phone = v; }
    public BigDecimal getCapacity() { return capacity; } public void setCapacity(BigDecimal v) { capacity = v; }
}
