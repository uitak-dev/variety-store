package com.variety.store.user_service.domain.entity;

import com.variety.store.user_service.domain.dto.response.RoleResponse;
import com.variety.store.user_service.domain.entity.base.Time;
import com.variety.store.user_service.domain.entity.value.Address;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    private Set<UserRole> userRoles;

    private boolean isDeleted = false;

    @Builder
    public User(Long id, String username, String password, String firstName, String lastName,
                String email, String phoneNumber, Address address) {

        this.id = id;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.userRoles = new HashSet<>();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        User user = (User) object;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Association convenience method
    public Set<Role> getRoles() {
        return userRoles.stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());
    }

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

    public void updateRoles(Set<Role> newRoles) {
        // 현재 UserRole에서 Role 목록 추출
        Set<Role> existingRoles = userRoles.stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());

        // 삭제할 역할: 기존 역할 목록 중에서 인자로 받은 역할 목록에 없는 역할.
        Set<Role> rolesToRemove = new HashSet<>(existingRoles);
        rolesToRemove.removeAll(newRoles);

        // 추가할 역할: 인자로 받은 역할 목록 중에서 기존 역할 목록에 없는 역할.
        Set<Role> rolesToAdd = new HashSet<>(newRoles);
        rolesToAdd.removeAll(existingRoles);

        // 역할 삭제 (Iterator 사용)
        Iterator<UserRole> iterator = userRoles.iterator();
        while (iterator.hasNext()) {
            UserRole userRole = iterator.next();
            if (rolesToRemove.contains(userRole.getRole())) {
                iterator.remove();
            }
        }

        // 역할 추가
        rolesToAdd.forEach(this::addRole);
    }

    public User delete() {
        isDeleted = true;
        return this;
    }
}
