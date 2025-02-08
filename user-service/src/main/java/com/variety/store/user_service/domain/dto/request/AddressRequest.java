package com.variety.store.user_service.domain.dto.request;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.variety.store.user_service.domain.entity.QUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressRequest {

    private String state;
    private String city;
    private String area;
    private String street;
    private String buildingNumber;
    private String apartment;
    private String zipCode;
}
