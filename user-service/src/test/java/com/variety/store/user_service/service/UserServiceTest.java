package com.variety.store.user_service.service;

import com.variety.store.user_service.domain.dto.request.AddressDto;
import com.variety.store.user_service.domain.dto.request.UserDto;
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

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
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
        UserDto savedUserDto = userService.createUser(userDto);

        // 검증
        assertNotNull(savedUserDto);
        assertThat(savedUserDto.getEmail()).isEqualTo("Jeong@example.com");
        assertNotNull(savedUserDto.getId());

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
        userService.createUser(userDto);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userDto);
        });

        assertThat(exception.getMessage()).isEqualTo("이미 사용 중인 이메일 입니다.");
    }

    /**
     * 사용자 정보 수정 테스트
     */
    @Test
    void testUpdateUser() {
        UserDto savedUser = userService.createUser(userDto);

        // 사용자 정보 수정
        AddressDto updateAddressDto = AddressDto.builder()
                .state("서울시")
                .street("아리수로")
                .zipCode("123")
                .build();

        UserDto updateUserDto = UserDto.builder()
                .id(savedUser.getId())
                .firstName("약용")
                .lastName("정")
                .phoneNumber("010-1234-5678")
                .address(updateAddressDto)
                .build();

        UserDto updatedUserDto = userService.updateUser(savedUser.getId(), updateUserDto);

        assertNotNull(updatedUserDto);
        assertThat(updatedUserDto)
                .extracting(UserDto::getFirstName, UserDto::getLastName, UserDto::getPhoneNumber,
                        user -> user.getAddress().getCity(),
                        user -> user.getAddress().getStreet(),
                        user -> user.getAddress().getZipCode())
                .containsExactly(updateUserDto.getFirstName(), updatedUserDto.getLastName(), updateUserDto.getPhoneNumber(),
                        updateAddressDto.getCity(),
                        updateAddressDto.getStreet(),
                        updateAddressDto.getZipCode());
    }
}