package com.variety.store.user_service.repository.custom;

import com.variety.store.user_service.domain.dto.request.UserDto;
import com.variety.store.user_service.domain.dto.search.UserSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {

    public Page<UserDto> searchUserList(UserSearch userSearch, Pageable pageable);
}
