package com.variety.store.user_service.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.variety.store.user_service.domain.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String email;

    @JsonIgnore
    private String password;

    private String name;
    private String phoneNumber;
    private AddressDto address;

    private Set<RoleDto> roles;
}
