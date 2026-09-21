package com.ginning.erp.role.entity;

import com.ginning.erp.common.entity.BaseEntity;
import com.ginning.erp.permission.entity.Permission;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
public class Role extends BaseEntity {

    @NotBlank
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }

}
