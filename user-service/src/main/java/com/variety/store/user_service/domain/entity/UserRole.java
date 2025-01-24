package com.variety.store.user_service.domain.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Entity
@Getter
@EqualsAndHashCode(of = {"user", "role"})
public class UserRole {

    @Id
    @GeneratedValue
    @Column(name = "user_role_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;
    }
}
