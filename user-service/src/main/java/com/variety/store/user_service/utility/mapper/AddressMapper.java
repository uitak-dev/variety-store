package com.variety.store.user_service.utility.mapper;

import com.variety.store.user_service.domain.dto.request.AddressDto;
import com.variety.store.user_service.domain.entity.value.Address;

public class AddressMapper {

    public static Address convertToEntity(AddressDto addressDto) {
        if (addressDto == null) return null;

        return Address.builder()
                .city(addressDto.getCity())
                .street(addressDto.getStreet())
                .zipcode(addressDto.getZipcode())
                .build();
    }

    public static AddressDto convertToDto(Address address) {
        if (address == null) return null;

        return AddressDto.builder()
                .city(address.getCity())
                .street(address.getStreet())
                .zipcode(address.getZipcode())
                .build();
    }
}
