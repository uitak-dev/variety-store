package com.variety.store.user_service.repository.custom;

import com.variety.store.user_service.domain.dto.request.UserRequest;
import com.variety.store.user_service.domain.dto.response.UserResponse;
import com.variety.store.user_service.domain.dto.search.UserSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {

    public Page<UserResponse> searchUserList(UserSearch userSearch, Pageable pageable);
}
