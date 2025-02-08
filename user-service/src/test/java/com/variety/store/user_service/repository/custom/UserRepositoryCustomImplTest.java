package com.variety.store.user_service.repository.custom;

import com.variety.store.user_service.domain.dto.request.UserRequest;
import com.variety.store.user_service.domain.dto.response.UserResponse;
import com.variety.store.user_service.domain.dto.search.UserSearch;
import com.variety.store.user_service.domain.entity.Role;
import com.variety.store.user_service.domain.entity.User;
import com.variety.store.user_service.repository.RoleRepository;
import com.variety.store.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryCustomImplTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {

        // 테스트용 Role 저장
        Role adminRole = roleRepository.save(Role.builder()
                .name("ADMIN")
                .description("Administrator Role")
                .build());

        Role managerRole = roleRepository.save(Role.builder()
                .name("MANAGER")
                .description("Manager Role")
                .build());

        Role userRole = roleRepository.save(Role.builder()
                .name("USER")
                .description("Regular User Role")
                .build());

        // 테스트용 User 저장
        User user1 = userRepository.save(User.builder()
                .username("john_doe")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("010-1234-5678")
                .build());

        User user2 = userRepository.save(User.builder()
                .username("alice_smith")
                .password("password456")
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .phoneNumber("010-8765-4321")
                .build());

        // User-Role 매핑.
        user1.addRole(adminRole);
        user1.addRole(userRole);
        user2.addRole(managerRole);
        user2.addRole(userRole);

        userRepository.save(user1);
        userRepository.save(user2);
    }

    @Test
    void testSearchUserList() {

        // 검색 조건
        UserSearch userSearch = new UserSearch();
        userSearch.setUsername("john");
        userSearch.setRoleNames(List.of("ADMIN"));

        // 페이징
        PageRequest pageable = PageRequest.of(0, 10);

        // 쿼리 실행
        Page<UserResponse> result = userRepository.searchUserList(userSearch, pageable);

        // 검증
        assertThat(result).isNotEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("john_doe");
        assertThat(result.getContent().get(0).getRoles()).hasSize(2);
    }
}