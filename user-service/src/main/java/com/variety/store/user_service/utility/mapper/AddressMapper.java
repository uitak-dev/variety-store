package com.variety.store.user_service.utility.mapper;

import com.variety.store.user_service.domain.dto.request.AddressRequest;
import com.variety.store.user_service.domain.dto.response.AddressResponse;
import com.variety.store.user_service.domain.entity.value.Address;

public class AddressMapper {

    public static AddressResponse convertToResponse(Address address) {

        if (address == null) return null;

        return AddressResponse.builder()
                .state(address.getState())
                .city(address.getCity())
                .area(address.getArea())
                .street(address.getStreet())
                .buildingNumber(address.getBuildingNumber())
                .apartment(address.getApartment())
                .zipCode(address.getZipCode())
                .build();
    }

    public static Address convertToEntity(AddressRequest addressRequest) {

        if (addressRequest == null) return null;

        return Address.builder()
                .state(addressRequest.getState())
                .city(addressRequest.getCity())
                .area(addressRequest.getArea())
                .street(addressRequest.getStreet())
                .buildingNumber(addressRequest.getBuildingNumber())
                .apartment(addressRequest.getApartment())
                .zipCode(addressRequest.getZipCode())
                .build();
    }
}
