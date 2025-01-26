package com.variety.store.user_service.service;

import com.variety.store.user_service.domain.dto.AddressDto;
import com.variety.store.user_service.domain.dto.UserDto;
import com.variety.store.user_service.domain.entity.User;
import com.variety.store.user_service.repository.UserRepository;
import org.assertj.core.api.Assertions;
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
                .name("Jeong Ui Tak")
                .email("Jeong@example.com")
                .password("rawPassword123")
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

        assertThat(exception.getMessage()).isEqualTo("이미 사용 중인 이메일입니다.");
    }

    /**
     * 사용자 정보 수정 테스트
     */
    @Test
    void testUpdateUser() {
        UserDto savedUser = userService.createUser(userDto);

        // 사용자 정보 수정
        AddressDto updateAddressDto = new AddressDto("seoul", "아리수로", "123");
        UserDto updateUserDto = UserDto.builder()
                .id(savedUser.getId())
                .name("Updated Name")
                .phoneNumber("010-8765-4321")
                .address(updateAddressDto)
                .build();

        UserDto updatedUserDto = userService.updateUser(savedUser.getId(), updateUserDto);

        assertNotNull(updatedUserDto);
        assertThat(updatedUserDto)
                .extracting(UserDto::getName, UserDto::getPhoneNumber,
                        user -> user.getAddress().getCity(),
                        user -> user.getAddress().getStreet(),
                        user -> user.getAddress().getZipcode())
                .containsExactly(updateUserDto.getName(), updateUserDto.getPhoneNumber(),
                        updateAddressDto.getCity(),
                        updateAddressDto.getStreet(),
                        updateAddressDto.getZipcode());
    }
}