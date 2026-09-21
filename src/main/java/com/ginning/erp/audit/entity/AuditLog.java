package com.ginning.erp.audit.entity;

import com.ginning.erp.common.entity.BaseEntity;
import com.ginning.erp.auth.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_entity_type", columnList = "entity_type"),
        @Index(name = "idx_entity_id", columnList = "entity_id"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
public class AuditLog extends BaseEntity {

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String action; // CREATE, UPDATE, DELETE, APPROVE, REJECT, LOGIN, LOGOUT

    @NotBlank
    @Column(nullable = false, length = 100)
    private String entityType;

    @Column(nullable = false)
    private String entityId;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String oldValue;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String newValue;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    public AuditLog() {
    }

    public AuditLog(User user, String action, String entityType, String entityId) {
        this.user = user;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
    }

    // Getters and Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

}
