package com.variety.store.user_service.domain.dto.request;

import com.querydsl.core.Tuple;
import com.variety.store.user_service.domain.entity.QUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressDto {

    private String state;
    private String city;
    private String area;
    private String street;
    private String buildingNumber;
    private String apartment;
    private String zipCode;

    public static AddressDto fromTuple(Tuple tuple) {
        return AddressDto.builder()
                .street(tuple.get(QUser.user.address.street))
                .city(tuple.get(QUser.user.address.city))
                .state(tuple.get(QUser.user.address.state))
                .zipCode(tuple.get(QUser.user.address.zipCode))
                .build();
    }
}
