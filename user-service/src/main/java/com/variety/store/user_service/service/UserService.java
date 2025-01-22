package com.variety.store.user_service.service;

import com.variety.store.user_service.domain.dto.AddressDto;
import com.variety.store.user_service.domain.dto.UserDto;
import com.variety.store.user_service.domain.entity.Address;
import com.variety.store.user_service.domain.entity.Role;
import com.variety.store.user_service.domain.entity.User;
import com.variety.store.user_service.repository.RoleRepository;
import com.variety.store.user_service.repository.UserRepository;
import com.variety.store.user_service.security.KeycloakService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final KeycloakService keycloakService;

    // 사용자 생성.
    public UserDto createUser(UserDto userDto) {

        if( userRepository.existsByEmail(userDto.getEmail()) ) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(userDto.getPassword());

        User user = User.builder()
                .email(userDto.getEmail())
                .password(encodedPassword)
                .name(userDto.getName())
                .address(new Address(
                        userDto.getAddress().getCity(),
                        userDto.getAddress().getStreet(),
                        userDto.getAddress().getZipcode()
                ))
                .build();

        return convertToDto(user);
    }

    // uuid로 사용자 조회.
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        return convertToDto(user);
    }

    // 이메일로 사용자 조회.
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        return convertToDto(user);
    }

    // 사용자 정보 수정.
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        String updateName = userDto.getName();
        Address updateAddress = new Address(
                userDto.getAddress().getCity(),
                userDto.getAddress().getStreet(),
                userDto.getAddress().getZipcode()
        );

        user.updateUserInfo(updateName, updateAddress);

//        userRepository.save(user);
        return convertToDto(user);
    }

    // 사용자 권한 추가.
    public void addRoleToUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + roleId));

        user.addRole(role);
        userRepository.save(user);
    }

    public UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .address(new AddressDto(
                        user.getAddress().getCity(),
                        user.getAddress().getStreet(),
                        user.getAddress().getZipcode()
                ))
                .build();
    }

    public User convertToEntity(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .email(userDto.getEmail())
                .name(userDto.getName())
                .address(new Address(
                        userDto.getAddress().getCity(),
                        userDto.getAddress().getStreet(),
                        userDto.getAddress().getZipcode()
                ))
                .build();
    }
}
