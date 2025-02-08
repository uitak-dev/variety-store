package com.variety.store.user_service.service;

import com.variety.store.user_service.domain.dto.request.RoleRequest;
import com.variety.store.user_service.domain.dto.request.UserRequest;
import com.variety.store.user_service.domain.dto.response.RoleResponse;
import com.variety.store.user_service.domain.dto.response.UserResponse;
import com.variety.store.user_service.domain.entity.Role;
import com.variety.store.user_service.domain.entity.User;
import com.variety.store.user_service.repository.RoleRepository;
import com.variety.store.user_service.repository.UserRepository;
import com.variety.store.user_service.utility.mapper.AddressMapper;
import com.variety.store.user_service.utility.mapper.RoleMapper;
import com.variety.store.user_service.utility.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
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
    public UserResponse createUser(UserRequest userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일 입니다.");
        }

        String encodedPassword = passwordEncoder.encode(userRequest.getPassword());
        userRequest.setPassword(encodedPassword);

        User user = UserMapper.convertToEntity(userRequest);

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

        return UserMapper.convertToResponse(user);
    }

    /**
     * 특정 사용자 기본 정보 조회.
     */
    public UserResponse getUserBasicInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return UserMapper.convertToResponse(user);
    }

    /**
     * 특정 사용자 권한 조회.
     */
    public Set<RoleResponse> getUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        return user.getRoles().stream()
                .map(RoleMapper::convertToResponse)
                .collect(Collectors.toSet());
    }

    /**
     * 사용자 기본 정보 수정.
     */
    public UserResponse updateUserInfo(Long userId, UserRequest userRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        user.updateInfo(userRequest.getFirstName(), userRequest.getLastName(),
                userRequest.getPhoneNumber(), AddressMapper.convertToEntity(userRequest.getAddress()));

//        userRepository.save(user);
        return UserMapper.convertToResponse(user);
    }

    /**
     * 사용자 삭제(회원 탈퇴).
     */
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        user.delete();
//        userRepository.save(user);
    }

    /**
     * 사용자 권한 수정.
     */
    public void updateRoleToUser(Long userId, Set<Long> roleIdSet) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        Set<Role> updateRoles = new HashSet<>(roleRepository.findAllById(roleIdSet));

        user.updateRoles(updateRoles);
//        userRepository.save(user);
    }

}
