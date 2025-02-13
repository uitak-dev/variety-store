package com.variety.store.user_service.domain.entity;

import com.variety.store.user_service.domain.entity.base.Tracking;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends Tracking {

    @Id
    @GeneratedValue
    @Column(name = "role_id")
    private Long id;

    @Column(unique = true)
    private String name;

    private String description;

    @Builder
    public Role(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Role updateRole(String name, String description) {
        this.name = name;
        this.description = description;

        return this;
    }
}


