package com.variety.store.user_service.domain.entity;

import com.variety.store.user_service.domain.entity.base.Time;
import com.variety.store.user_service.domain.entity.value.Address;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends Time {

    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String phoneNumber;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();

    private boolean isDeleted = false;

    @Builder
    public User(Long id, String username, String password, String firstName, String lastName, String email, String phoneNumber, Address address) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    // Association convenience method
    public void addRole(Role role) {
        userRoles.add(new UserRole(this, role));
    }

    public User updateInfo(String firstName, String lastName, String phoneNumber, Address address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.address = address;

        return this;
    }

    public User delete() {
        isDeleted = true;
        return this;
    }
}
