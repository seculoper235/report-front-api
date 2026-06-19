package com.example.reportfrontapi.domain.auth.application;

import com.example.reportfrontapi.domain.auth.DuplicateEmailException;
import com.example.reportfrontapi.domain.auth.InvalidCredentialsException;
import com.example.reportfrontapi.domain.auth.InvalidTokenException;
import com.example.reportfrontapi.domain.auth.application.dto.TokenResponse;
import com.example.reportfrontapi.domain.user.Role;
import com.example.reportfrontapi.domain.user.User;
import com.example.reportfrontapi.domain.user.repository.UserRepository;
import com.example.reportfrontapi.web.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenBlacklistService blacklistService;

    // 이메일 중복 확인 후 비밀번호를 BCrypt 해시로 저장.
    @Transactional
    public Long signup(String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
        User user = userRepository.save(User.of(email, passwordEncoder.encode(rawPassword)));
        return user.getUserId();
    }

    // 이메일/비밀번호 검증 후 자체 JWT 발급, refresh는 Redis 저장.
    @Transactional
    public TokenResponse login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return issueTokens(user.getUserId(), user.getRole());
    }

    // refresh 토큰 재발급: Redis 저장값과 비교 후 일치 시에만 회전 발급.
    @Transactional
    public TokenResponse reissue(String refreshToken) {
        if (!jwtTokenProvider.validate(refreshToken)) {
            throw new InvalidTokenException("유효하지 않은 refresh 토큰입니다.");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        String stored = refreshTokenService.find(userId)
                .orElseThrow(() -> new InvalidTokenException("저장된 refresh 토큰이 없습니다."));

        if (!stored.equals(refreshToken)) {
            // 탈취 의심: 저장된 토큰 제거 후 거부.
            refreshTokenService.delete(userId);
            throw new InvalidTokenException("refresh 토큰이 일치하지 않습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("존재하지 않는 사용자입니다."));

        return issueTokens(user.getUserId(), user.getRole());
    }

    // 로그아웃: refresh 무효화 + 현재 access 토큰 블랙리스트 등록.
    @Transactional
    public void logout(String accessToken) {
        if (!jwtTokenProvider.validate(accessToken)) {
            throw new InvalidTokenException("유효하지 않은 access 토큰입니다.");
        }
        Long userId = jwtTokenProvider.getUserId(accessToken);
        refreshTokenService.delete(userId);
        blacklistService.blacklist(accessToken);
    }

    private TokenResponse issueTokens(Long userId, Role role) {
        String accessToken = jwtTokenProvider.createAccessToken(userId, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);
        refreshTokenService.save(userId, refreshToken);
        return new TokenResponse(accessToken, refreshToken);
    }
}
