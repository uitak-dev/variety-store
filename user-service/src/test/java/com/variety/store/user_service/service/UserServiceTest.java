package com.variety.store.user_service.service;

import com.variety.store.user_service.domain.dto.request.AddressRequest;
import com.variety.store.user_service.domain.dto.request.UserRequest;
import com.variety.store.user_service.domain.dto.response.UserResponse;
import com.variety.store.user_service.domain.entity.User;
import com.variety.store.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        userRequest = UserRequest.builder()
                .username("wjd1735")
                .password("rawPassword123")
                .firstName("의탁")
                .lastName("정")
                .email("Jeong@example.com")
                .phoneNumber("010-1234-5678")
                .build();
    }

    /**
     * 회원 가입 테스트
     */
    @Test
    void testCreateUser() {
        // 사용자 생성
        UserResponse savedUserResponse = userService.createUser(userRequest);

        // 검증
        assertNotNull(savedUserResponse);
        assertThat(savedUserResponse.getEmail()).isEqualTo("Jeong@example.com");
        assertNotNull(savedUserResponse.getId());

        // 데이터베이스에 저장된 사용자 확인
        User savedUser = userRepository.findByEmail("Jeong@example.com").orElseThrow();
        assertThat(savedUser.getEmail()).isEqualTo("Jeong@example.com");
        assertThat(passwordEncoder.matches("rawPassword123", savedUser.getPassword())).isTrue();
    }

    /**
     * 이메일 중복 테스트
     */
    @Test
    void testCreateUserWithDuplicateEmail() {
        userService.createUser(userRequest);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userRequest);
        });

        assertThat(exception.getMessage()).isEqualTo("이미 사용 중인 이메일 입니다.");
    }

    /**
     * 사용자 정보 수정 테스트
     */
    @Test
    void testUpdateUser() {
        // 사용자 생성
        UserResponse savedUser = userService.createUser(userRequest);

        // 사용자 정보 수정
        AddressRequest updateAddressRequest = AddressRequest.builder()
                .city("서울시")
                .state("관악구")
                .street("아리수로")
                .zipCode("123")
                .build();

        UserRequest updateUserRequest = UserRequest.builder()
                .id(savedUser.getId())
                .firstName("약용")
                .lastName("정")
                .phoneNumber("010-9876-5432")
                .address(updateAddressRequest)
                .build();

        // 사용자 정보 업데이트
        UserResponse updatedUserResponse = userService.updateUserInfo(savedUser.getId(), updateUserRequest);

        // 응답 객체 검증
        assertNotNull(updatedUserResponse);
        assertEquals(savedUser.getId(), updatedUserResponse.getId());
        assertEquals(savedUser.getEmail(), updatedUserResponse.getEmail()); // 이메일은 변경되지 않았는지 확인
        assertEquals(updateUserRequest.getFirstName(), updatedUserResponse.getFirstName());
        assertEquals(updateUserRequest.getLastName(), updatedUserResponse.getLastName());
        assertEquals(updateUserRequest.getPhoneNumber(), updatedUserResponse.getPhoneNumber());

        // 주소 정보 검증
        assertNotNull(updatedUserResponse.getAddress());
        assertEquals(updateAddressRequest.getCity(), updatedUserResponse.getAddress().getCity());
        assertEquals(updateAddressRequest.getState(), updatedUserResponse.getAddress().getState());
        assertEquals(updateAddressRequest.getStreet(), updatedUserResponse.getAddress().getStreet());
        assertEquals(updateAddressRequest.getZipCode(), updatedUserResponse.getAddress().getZipCode());

        // 데이터베이스에서 사용자 직접 조회하여 검증
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertEquals(updateUserRequest.getFirstName(), updatedUser.getFirstName());
        assertEquals(updateUserRequest.getLastName(), updatedUser.getLastName());
        assertEquals(updateUserRequest.getPhoneNumber(), updatedUser.getPhoneNumber());
        assertEquals(savedUser.getEmail(), updatedUser.getEmail()); // 이메일은 변경되지 않았는지 확인

        // 주소 정보 검증
        assertNotNull(updatedUser.getAddress());
        assertEquals(updateAddressRequest.getCity(), updatedUser.getAddress().getCity());
        assertEquals(updateAddressRequest.getState(), updatedUser.getAddress().getState());
        assertEquals(updateAddressRequest.getStreet(), updatedUser.getAddress().getStreet());
        assertEquals(updateAddressRequest.getZipCode(), updatedUser.getAddress().getZipCode());


        System.out.println(userRequest.getPassword());
        System.out.println(updatedUser.getPassword());


        // 패스워드 변경 여부 확인
        assertTrue(passwordEncoder.matches("rawPassword123", updatedUser.getPassword()));
    }
}