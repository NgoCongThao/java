package com.admin.backend.entity;

import com.admin.backend.util.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@FilterDef(
    name = "tenantFilter",
    parameters = @ParamDef(name = "tenantId", type = Long.class)
)
@Filter(
    name = "tenantFilter",
    condition = "tenant_id = :tenantId"
)
public abstract class BaseEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    @PrePersist
    public void prePersist() {
        if (tenantId == null) {
            tenantId = TenantContext.getTenantId();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (tenantId == null) {
            tenantId = TenantContext.getTenantId();
        }
    }
}