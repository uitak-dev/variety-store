package com.variety.store.user_service.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String username;
    private String password;

    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private AddressDto address;

    private Set<RoleDto> roles;
}
