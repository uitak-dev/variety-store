package com.variety.store.user_service.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourceRequest {

    private Long id;

    private String name;
    private String pattern;     // 자원 경로(url)
    private String httpMethod;
    private String description;
    private Long priority;

    private Set<Long> roles;
}
