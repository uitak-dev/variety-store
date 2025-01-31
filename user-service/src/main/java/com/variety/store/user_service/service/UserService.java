package com.variety.store.user_service.service;

import com.variety.store.user_service.domain.dto.request.RoleDto;
import com.variety.store.user_service.domain.dto.request.UserDto;
import com.variety.store.user_service.domain.entity.Role;
import com.variety.store.user_service.domain.entity.User;
import com.variety.store.user_service.repository.RoleRepository;
import com.variety.store.user_service.repository.UserRepository;
import com.variety.store.user_service.utility.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public static final String DEFAULT_ROLE_NAME = "ROLE_USER";

    /**
     * 사용자 생성(회원 가입).
     */
    public UserDto createUser(UserDto userDto) {

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일 입니다.");
        }

        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        userDto.setPassword(encodedPassword);

        User user = convertToEntity(userDto);

        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name("ROLE_USER")
                            .description("사용자 기본 권한")
                            .build();
                    return roleRepository.save(newRole);
                });

        user.addRole(defaultRole);
        userRepository.save(user);

        log.info("사용자 DB 저장 완료: {}", user.getEmail());

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

        user.updateInfo(updateUser.getFirstName(), updateUser.getLastName(),
                updateUser.getPhoneNumber(), updateUser.getAddress());

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
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(AddressMapper.convertToDto(user.getAddress()))
                .build();
    }

    public User convertToEntity(UserDto userDto) {

        return User.builder()
                .id(userDto.getId())
                .username(userDto.getUsername())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .email(userDto.getEmail())
                .password(userDto.getPassword())
                .phoneNumber(userDto.getPhoneNumber())
                .address(AddressMapper.convertToEntity(userDto.getAddress()))
                .build();
    }
}
