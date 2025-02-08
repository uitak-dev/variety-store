package com.variety.store.user_service.domain.entity;

import com.variety.store.user_service.domain.entity.base.Tracking;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@EqualsAndHashCode(of = {"role", "resource"}, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResourceRole extends Tracking {

    @Id
    @GeneratedValue
    @Column(name = "role_resource_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    public ResourceRole(Resource resource, Role role) {
        this.resource = resource;
        this.role = role;
    }
}
