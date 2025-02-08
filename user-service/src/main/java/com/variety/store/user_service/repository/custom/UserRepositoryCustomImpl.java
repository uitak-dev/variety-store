package com.variety.store.user_service.repository.custom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
import com.variety.store.user_service.domain.dto.request.UserRequest;
import com.variety.store.user_service.domain.dto.response.UserResponse;
import com.variety.store.user_service.domain.dto.search.UserSearch;
import com.variety.store.user_service.domain.entity.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.variety.store.user_service.utility.mapper.UserMapper;
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
    public Page<UserResponse> searchUserList(UserSearch userSearch, Pageable pageable) {

        // 유저 목록을 조회하면서 Role과 Address 정보까지 한 번에 가져옴.
        List<User> results = queryFactory
                .selectFrom(user)
                .leftJoin(user.userRoles, userRole).fetchJoin()
                .leftJoin(userRole.role, role).fetchJoin()
                .where(
                        usernameEq(userSearch.getUsername()),
                        roleNameIn(userSearch.getRoleNames())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .distinct()
                .fetch();

        List<UserResponse> content = results.stream().map(UserMapper::convertToResponse).toList();

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

        if (!CollectionUtils.isEmpty(roleNames)) {
            return JPAExpressions
                    .selectOne()
                    .from(userRole)
                    .leftJoin(userRole.role, role)
                    .where(
                            userRole.user.eq(user),
                            role.name.in(roleNames)
                    )
                    .exists();
        }
        return null;
    }
}