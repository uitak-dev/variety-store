package com.variety.store.user_service.service;

import com.variety.store.user_service.domain.dto.AddressDto;
import com.variety.store.user_service.domain.dto.RoleDto;
import com.variety.store.user_service.domain.dto.UserDto;
import com.variety.store.user_service.domain.entity.Address;
import com.variety.store.user_service.domain.entity.Role;
import com.variety.store.user_service.domain.entity.User;
import com.variety.store.user_service.repository.RoleRepository;
import com.variety.store.user_service.repository.UserRepository;
import com.variety.store.user_service.security.KeycloakService;
import com.variety.store.user_service.utility.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final KeycloakService keycloakService;

    public static final String DEFAULT_ROLE_NAME = "ROLE_USER";

    /**
     * 사용자 생성(회원 가입).
     */
    public UserDto createUser(UserDto userDto) {

        if( userRepository.existsByEmail(userDto.getEmail()) ) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(userDto.getPassword());

        User user = User.builder()
                .email(userDto.getEmail())
                .password(encodedPassword)
                .name(userDto.getName())
                .phoneNumber(userDto.getPhoneNumber())
                .build();

        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE_NAME)
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name(DEFAULT_ROLE_NAME)
                            .description("사용자 기본 권한")
                            .build();
                    return roleRepository.save(newRole);
                });

        user.addRole(defaultRole);
        userRepository.save(user);

        return convertToDto(user);
    }

    /**
     * 특정 사용자 기본 정보 조회.
     */
    public UserDto getUserBasicInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        return convertToDto(user);
    }

    /**
     * 특정 사용자 권한 조회.
     */
    public Set<RoleDto> getUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        return user.getUserRoles().stream()
                .map(userRole -> new RoleDto(userRole.getRole().getId(), userRole.getRole().getName(), userRole.getRole().getDescription()))
                .collect(Collectors.toSet());
    }

    /**
     * 사용자 기본 정보 수정.
     */
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        User updateUser = convertToEntity(userDto);

        user.updateInfo(updateUser.getName(), updateUser.getPhoneNumber(), updateUser.getAddress());

        userRepository.save(user);
        return convertToDto(user);
    }

    /**
     * 사용자 삭제(회원 탈퇴).
     */
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        userRepository.save(user.delete());
    }

    /**
     * 사용자 권한 추가.
     */
    public void addRoleToUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + roleId));

        user.addRole(role);
        userRepository.save(user);
    }

    /**
     * 사용자 권한 제거.
     */
    public void removeRoleFromUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        user.getUserRoles().removeIf(userRole -> userRole.getRole().getId().equals(roleId));

        userRepository.save(user);
    }

    public UserDto convertToDto(User user) {

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .address(AddressMapper.convertToDto(user.getAddress()))
                .build();
    }

    public User convertToEntity(UserDto userDto) {

        return User.builder()
                .id(userDto.getId())
                .email(userDto.getEmail())
                .password(userDto.getPassword())
                .name(userDto.getName())
                .phoneNumber(userDto.getPhoneNumber())
                .address(AddressMapper.convertToEntity(userDto.getAddress()))
                .build();
    }
}
