package com.variety.store.user_service.domain.dto.request;

import com.querydsl.core.Tuple;
import com.variety.store.user_service.domain.entity.QRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDto {

    private Long id;

    private String name;
    private String description;

    public static RoleDto fromTuple(Tuple tuple) {
        return RoleDto.builder()
                .id(tuple.get(QRole.role.id))
                .name(tuple.get(QRole.role.name))
                .description(tuple.get(QRole.role.description))
                .build();
    }
}
