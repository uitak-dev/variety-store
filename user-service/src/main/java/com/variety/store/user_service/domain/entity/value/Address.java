package com.variety.store.user_service.domain.entity.value;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    /**
     * Country -> 나라 (대한민국)
     * State/Region -> 특별시/광역시/도 (서울특별시, 부산광역시, 경기도 등)
     * City -> 시/군/구 (예: 성남시, 수원시, 강남구)
     * Neighborhood/Area -> 동/읍/면 (예: 역삼동, 풍덕천동)
     * Street -> 도로명 (예: 테헤란로, 명동길)
     * Building Number -> 건물 번호 (예: 123)
     * Apartment/Unit Number -> 아파트/동/호수 (예: 101동 1001호)
     * Postal Code (ZIP Code) -> 우편번호 (예: 12345)
     * Landmark (if needed) -> 주변 랜드마크 (필요시 예: 서울타워 근처)
     */

    private String state;
    private String city;
    private String area;
    private String street;
    private String buildingNumber;
    private String apartment;
    private String zipCode;

    @Builder
    public Address(String state, String city, String area, String street, String buildingNumber, String apartment, String zipCode) {
        this.state = state;
        this.city = city;
        this.area = area;
        this.street = street;
        this.buildingNumber = buildingNumber;
        this.apartment = apartment;
        this.zipCode = zipCode;
    }
}
