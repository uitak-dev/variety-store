package com.variety.store.user_service.repository;

import com.variety.store.user_service.domain.entity.User;
import com.variety.store.user_service.repository.custom.UserRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
