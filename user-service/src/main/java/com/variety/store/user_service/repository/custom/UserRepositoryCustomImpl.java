package com.variety.store.user_service.repository.custom;

import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
import com.variety.store.user_service.domain.dto.request.AddressDto;
import com.variety.store.user_service.domain.dto.request.RoleDto;
import com.variety.store.user_service.domain.dto.request.UserDto;
import com.variety.store.user_service.domain.dto.search.UserSearch;
import com.variety.store.user_service.domain.entity.QRole;
import com.variety.store.user_service.domain.entity.QUser;
import com.variety.store.user_service.domain.entity.QUserRole;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public UserRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(JPQLTemplates.DEFAULT, em);
    }

    QUser user = QUser.user;
    QUserRole userRole = QUserRole.userRole;
    QRole role = QRole.role;

    // 검색 조건과 페이징이 가능한 회원 목록 조회.
    public Page<UserDto> searchUserList(UserSearch userSearch, Pageable pageable) {

        // 유저 목록을 조회하면서 Role과 Address 정보까지 한 번에 가져옴
        // 유저 목록 조회 (fetch 사용)
        List<Tuple> results = queryFactory
                .select(
                        user.id,
                        user.username,
                        user.password,
                        user.email,
                        user.firstName,
                        user.lastName,
                        user.phoneNumber,
                        user.address.street,
                        user.address.city,
                        user.address.state,
                        user.address.zipCode,
                        role.id,
                        role.name,
                        role.description
                )
                .from(user)
                .leftJoin(user.userRoles, userRole)
                .leftJoin(userRole.role, role)
                .where(
                        usernameEq(userSearch.getUsername()),
                        roleNameIn(userSearch.getRoleNames())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .distinct()
                .fetch();

        // 중복 제거를 위한 Map
        Map<Long, UserDto> userDtoMap = new LinkedHashMap<>();

        for (Tuple tuple : results) {
            Long userId = tuple.get(user.id);

            // UserDto 객체를 가져오거나 새로 생성
            UserDto userDto = userDtoMap.computeIfAbsent(userId, id -> UserDto.fromTuple(tuple));

            // RoleDto 추가
            userDto.addRole(RoleDto.fromTuple(tuple));
        }

        // 최종 변환된 리스트 생성
        List<UserDto> content = new ArrayList<>(userDtoMap.values());

        // 전체 개수 조회
        JPAQuery<Long> countQuery = queryFactory
                .select(user.countDistinct())
                .from(user)
                .leftJoin(user.userRoles, userRole).fetchJoin()
                .where(
                        usernameEq(userSearch.getUsername()),
                        roleNameIn(userSearch.getRoleNames())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression usernameEq(String username) {
        // null, isEmpty() 검사
        return StringUtils.hasLength(username) ? user.username.containsIgnoreCase(username) : null;
    }
    private BooleanExpression roleNameIn(List<String> roleNames) {
//        return !CollectionUtils.isEmpty(roleNames) ? userRole.role.name.in(roleNames) : null;
        if (!CollectionUtils.isEmpty(roleNames)) {
            return user.id.in(
                    JPAExpressions
                            .select(userRole.user.id)
                            .from(userRole)
                            .leftJoin(userRole.role, role)
                            .where(role.name.in(roleNames))
            );
        }

        return null;
    }
}