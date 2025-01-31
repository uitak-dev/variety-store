package com.variety.store.user_service.domain.entity.base;

import jakarta.persistence.Column;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

public class TrackingEntity extends TimeEntity {

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;
}
