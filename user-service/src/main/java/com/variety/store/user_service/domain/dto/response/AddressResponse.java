package com.variety.store.user_service.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressResponse {

    private String state;
    private String city;
    private String area;
    private String street;
    private String buildingNumber;
    private String apartment;
    private String zipCode;
}
