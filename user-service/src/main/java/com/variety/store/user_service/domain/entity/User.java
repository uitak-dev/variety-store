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
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    private String name;
    private String email;
    private String password;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserRole> userRoles = new HashSet<>();

    // Association convenience method
    public void addRole(Role role) {
        userRoles.add(new UserRole(this, role));
    }

    @Builder
    public User(Long id, String name, String email, String password, Address address, Set<UserRole> userRoles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.address = address;
        this.userRoles = userRoles;
    }

    public User updateUserInfo(String name, Address address) {
        this.name = name;
        this.address = address;

        return this;
    }
}
