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

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phoneNumber;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();

    private boolean isDeleted = false;

    @Builder
    public User(Long id, String name, String email, String password, String phoneNumber, Address address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    // Association convenience method
    public void addRole(Role role) {
        userRoles.add(new UserRole(this, role));
    }

    public User updateInfo(String name, String phoneNumber, Address address) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;

        return this;
    }

    public User delete() {
        isDeleted = true;
        return this;
    }
}
