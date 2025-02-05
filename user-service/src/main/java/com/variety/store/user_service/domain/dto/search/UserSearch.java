package com.variety.store.user_service.domain.dto.search;

import lombok.Data;

import java.util.List;

@Data
public class UserSearch {

    private String username;
    private List<String> roleNames;

}
