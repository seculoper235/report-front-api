package com.example.reportfrontapi.domain.user.repository;

import com.example.reportfrontapi.common.repository.BaseRepository;
import com.example.reportfrontapi.domain.user.QUser;
import com.example.reportfrontapi.domain.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository extends BaseRepository<User, Long> {

    private static final QUser user = QUser.user;

    public UserRepository(EntityManager em) {
        super(User.class, em);
    }

    // 이메일로 유저 조회.
    public Optional<User> findByEmail(String email) {
        User result = selectFrom(user)
                .where(user.email.eq(email))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    // 사용자 행을 비관적 쓰기 잠금으로 조회(교환 시 잔액 검증/차감 직렬화용).
    public Optional<User> findByIdForUpdate(Long userId) {
        return Optional.ofNullable(
                selectFrom(user)
                        .where(user.userId.eq(userId))
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .fetchOne());
    }

    // 이메일 중복 여부.
    public boolean existsByEmail(String email) {
        Integer found = select(user.userId.intValue())
                .from(user)
                .where(user.email.eq(email))
                .fetchFirst();
        return found != null;
    }
}
