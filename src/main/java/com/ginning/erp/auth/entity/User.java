package com.ginning.erp.auth.entity;

import com.ginning.erp.common.entity.BaseEntity;
import com.ginning.erp.role.entity.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "username")
})
public class User extends BaseEntity {

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Boolean accountNonLocked = true;

    @Column(nullable = false)
    private Boolean accountNonExpired = true;

    @Column(nullable = false)
    private Boolean credentialsNonExpired = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime passwordChangedAt;

    @Column(length = 50)
    private String status = "ACTIVE";

    @Column(name = "auth_provider", nullable = false, length = 30)
    private String authProvider = "LOCAL";

    @Column(name = "provider_subject", length = 255)
    private String providerSubject;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToMany(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public User() {
    }

    public User(String email, String username, String passwordHash) {
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordChangedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        this.passwordChangedAt = LocalDateTime.now();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public Boolean getAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(Boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public Boolean getCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    public String getProviderSubject() {
        return providerSubject;
    }

    public void setProviderSubject(String providerSubject) {
        this.providerSubject = providerSubject;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

}
