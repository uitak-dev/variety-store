package com.variety.store.user_service.domain.dto.request;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.variety.store.user_service.domain.entity.QRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleRequest {

    private Long id;

    private String name;
    private String description;
}
