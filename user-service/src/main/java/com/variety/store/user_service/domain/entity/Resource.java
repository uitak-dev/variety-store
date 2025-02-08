package com.variety.store.user_service.domain.entity;

import com.variety.store.user_service.domain.entity.base.Tracking;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resource extends Tracking {

    @Id
    @GeneratedValue
    @Column(name = "resource_id")
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String pattern;  // 자원 경로(url)

    private String httpMethod;
    private String description;
    private Long priority;

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ResourceRole> resourceRoles;

    @Builder
    public Resource(Long id, String name, String pattern, String httpMethod, String description, Long priority) {
        this.id = id;
        this.name = name;
        this.pattern = pattern;
        this.httpMethod = httpMethod;
        this.description = description;
        this.priority = priority;
        this.resourceRoles = new HashSet<>();
    }

    // Association convenience method
    public void addRole(Role role) {
        resourceRoles.add(new ResourceRole(this, role));
    }

    public void update(String name, String pattern, String httpMethod, String description, Long priority, Set<Role> newRoles) {

        // 기본 정보 수정.
        this.name = name;
        this.pattern = pattern;
        this.httpMethod = httpMethod;
        this.description = description;
        this.priority = priority;

        // 현재 UserRole에서 Role 목록 추출
        Set<Role> existingRoles = resourceRoles.stream()
                .map(ResourceRole::getRole)
                .collect(Collectors.toSet());

        // 삭제할 역할: 기존 역할 목록 중에서 인자로 받은 역할 목록에 없는 역할.
        Set<Role> rolesToRemove = new HashSet<>(existingRoles);
        rolesToRemove.removeAll(newRoles);

        // 추가할 역할: 인자로 받은 역할 목록 중에서 기존 역할 목록에 없는 역할.
        Set<Role> rolesToAdd = new HashSet<>(newRoles);
        rolesToAdd.removeAll(existingRoles);

        // 역할 삭제 (Iterator 사용)
        Iterator<ResourceRole> iterator = resourceRoles.iterator();
        while (iterator.hasNext()) {
            ResourceRole resourceRole = iterator.next();
            if (rolesToRemove.contains(resourceRole.getRole())) {
                iterator.remove();
            }
        }

        // 역할 추가
        rolesToAdd.forEach(this::addRole);
    }
}
