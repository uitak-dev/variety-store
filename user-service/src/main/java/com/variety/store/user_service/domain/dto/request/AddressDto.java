package com.variety.store.user_service.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressDto {

    private String city;
    private String street;
    private String zipcode;
}
