package com.variety.store.user_service.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {

    private Long id;
    private String username;
    private String password;

    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private AddressRequest address;

    private Set<Long> roles;
}
