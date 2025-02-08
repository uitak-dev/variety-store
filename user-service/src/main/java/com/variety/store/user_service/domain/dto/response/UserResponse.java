package com.variety.store.user_service.domain.dto.response;

import com.variety.store.user_service.domain.dto.request.AddressRequest;
import com.variety.store.user_service.domain.dto.request.RoleRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String password;

    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private AddressResponse address;

    private Set<RoleResponse> roles;
}
