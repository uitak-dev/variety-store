package com.variety.store.user_service.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.Tuple;
import com.variety.store.user_service.domain.entity.QUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String username;
    private String password;

    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private AddressDto address;

    private Set<RoleDto> roles;

    public static UserDto fromTuple(Tuple tuple) {
        return UserDto.builder()
                .id(tuple.get(QUser.user.id))
                .username(tuple.get(QUser.user.username))
                .password(tuple.get(QUser.user.password))
                .email(tuple.get(QUser.user.email))
                .firstName(tuple.get(QUser.user.firstName))
                .lastName(tuple.get(QUser.user.lastName))
                .phoneNumber(tuple.get(QUser.user.phoneNumber))
                .address(AddressDto.fromTuple(tuple))
                .roles(new HashSet<>()) // 역할 추가는 별도로 처리
                .build();
    }

    public void addRole(RoleDto roleDto) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(roleDto);
    }
}
