package com.ginning.erp.permission.entity;

import com.ginning.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "permissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
public class Permission extends BaseEntity {

    @NotBlank
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 50)
    private String resource;

    @Column(length = 50)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String description;

    public Permission() {
    }

    public Permission(String name, String resource, String action) {
        this.name = name;
        this.resource = resource;
        this.action = action;
    }

    public Permission(String name, String resource, String action, String description) {
        this.name = name;
        this.resource = resource;
        this.action = action;
        this.description = description;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
