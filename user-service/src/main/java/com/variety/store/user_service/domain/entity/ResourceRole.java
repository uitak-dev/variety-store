package com.variety.store.user_service.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@EqualsAndHashCode(of = {"role", "resource"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResourceRole {

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

    private ResourceRole(Role role) {
        this.role = role;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public static ResourceRole createResourceRole(Role role) {
        return new ResourceRole(role);
    }
}
