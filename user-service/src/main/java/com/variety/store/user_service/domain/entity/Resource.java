package com.variety.store.user_service.domain.entity;

import com.variety.store.user_service.domain.dto.ResourceDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resource {

    @Id
    @GeneratedValue
    @Column(name = "resource_id")
    private Long id;

    private String name;
    private String pattern;     // 자원 경로(url)
    private String httpMethod;
    private String description;
    private int order;
    private boolean isActive;

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ResourceRole> resourceRoles = new HashSet<>();

    @Builder
    public Resource(Long id, String name, String pattern, String httpMethod, String description, int order, boolean isActive, Set<ResourceRole> resourceRoles) {
        this.id = id;
        this.name = name;
        this.pattern = pattern;
        this.httpMethod = httpMethod;
        this.description = description;
        this.order = order;
        this.isActive = isActive;
        this.resourceRoles = resourceRoles;
    }

    // Association convenience method
    public void addResourceRole(ResourceRole resourceRole) {
        resourceRoles.add(resourceRole);
        resourceRole.setResource(this);
    }

    public Set<Role> getRoleSet() {
        return resourceRoles.stream()
                .map(ResourceRole::getRole)
                .collect(Collectors.toSet());
    }

    public void updateInfo(String name, String pattern, String httpMethod, String description, int order) {
        this.name = name;
        this.pattern = pattern;
        this.httpMethod = httpMethod;
        this.description = description;
        this.order = order;
    }
}
