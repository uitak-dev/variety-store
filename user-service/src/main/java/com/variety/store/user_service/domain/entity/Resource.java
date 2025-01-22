package com.variety.store.user_service.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resource {

    @Id
    @GeneratedValue
    @Column(name = "resource_id")
    private Long id;

    private String url;
    private String httpMethod;
    private String description;
    private int order;

    @OneToMany(mappedBy = "resource")
    private Set<RoleResource> roleResources = new HashSet<>();

    @Builder
    public Resource(String url, String httpMethod, String description, int order, Set<RoleResource> roleResources) {
        this.url = url;
        this.httpMethod = httpMethod;
        this.description = description;
        this.order = order;
        this.roleResources = roleResources;
    }

    public void addRole(Role role) {
        roleResources.add(new RoleResource(role, this));
    }
}
