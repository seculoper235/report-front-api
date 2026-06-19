package com.example.reportfrontapi.domain.auth.application;

import com.example.reportfrontapi.domain.auth.DuplicateEmailException;
import com.example.reportfrontapi.domain.auth.InvalidCredentialsException;
import com.example.reportfrontapi.domain.auth.InvalidTokenException;
import com.example.reportfrontapi.domain.auth.application.dto.TokenResponse;
import com.example.reportfrontapi.domain.user.Role;
import com.example.reportfrontapi.domain.user.User;
import com.example.reportfrontapi.domain.user.repository.UserRepository;
import com.example.reportfrontapi.web.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AccessTokenBlacklistService blacklistService;

    @InjectMocks
    private AuthService authService;

    private User user(Long id) {
        return User.builder()
                .userId(id)
                .email("user@x.com")
                .password("ENC")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("회원가입: 이메일 미중복이면 비밀번호를 인코딩해 저장하고 userId를 반환한다")
    void signup_success() {
        given(userRepository.existsByEmail("user@x.com")).willReturn(false);
        given(passwordEncoder.encode("rawpw1234")).willReturn("ENC");
        given(userRepository.save(ArgumentMatchers.any(User.class))).willReturn(user(1L));

        Long userId = authService.signup("user@x.com", "rawpw1234");

        assertThat(userId).isEqualTo(1L);
        verify(passwordEncoder).encode("rawpw1234");
        verify(userRepository).save(ArgumentMatchers.any(User.class));
    }

    @Test
    @DisplayName("회원가입: 이메일이 중복이면 DuplicateEmailException을 던진다")
    void signup_duplicate() {
        given(userRepository.existsByEmail("user@x.com")).willReturn(true);

        assertThatThrownBy(() -> authService.signup("user@x.com", "rawpw1234"))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository, never()).save(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("로그인: 비밀번호가 일치하면 토큰을 발급하고 refresh를 저장한다")
    void login_success() {
        given(userRepository.findByEmail("user@x.com")).willReturn(Optional.of(user(1L)));
        given(passwordEncoder.matches("rawpw1234", "ENC")).willReturn(true);
        given(jwtTokenProvider.createAccessToken(1L, Role.USER)).willReturn("access");
        given(jwtTokenProvider.createRefreshToken(1L)).willReturn("refresh");

        TokenResponse response = authService.login("user@x.com", "rawpw1234");

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        verify(refreshTokenService).save(1L, "refresh");
    }

    @Test
    @DisplayName("로그인: 비밀번호가 틀리면 InvalidCredentialsException을 던진다")
    void login_wrongPassword() {
        given(userRepository.findByEmail("user@x.com")).willReturn(Optional.of(user(1L)));
        given(passwordEncoder.matches("wrong", "ENC")).willReturn(false);

        assertThatThrownBy(() -> authService.login("user@x.com", "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(refreshTokenService, never()).save(ArgumentMatchers.anyLong(), ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("로그인: 없는 이메일이면 InvalidCredentialsException을 던진다")
    void login_noUser() {
        given(userRepository.findByEmail("none@x.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("none@x.com", "rawpw1234"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("재발급: Redis 저장값과 일치하면 새 토큰을 발급하고 refresh를 회전 저장한다")
    void reissue_success() {
        given(jwtTokenProvider.validate("refresh")).willReturn(true);
        given(jwtTokenProvider.getUserId("refresh")).willReturn(1L);
        given(refreshTokenService.find(1L)).willReturn(Optional.of("refresh"));
        given(userRepository.findById(1L)).willReturn(Optional.of(user(1L)));
        given(jwtTokenProvider.createAccessToken(1L, Role.USER)).willReturn("new-access");
        given(jwtTokenProvider.createRefreshToken(1L)).willReturn("new-refresh");

        TokenResponse response = authService.reissue("refresh");

        assertThat(response.accessToken()).isEqualTo("new-access");
        verify(refreshTokenService).save(1L, "new-refresh");
    }

    @Test
    @DisplayName("재발급: Redis 저장값과 다르면 저장값을 삭제하고 401을 던진다")
    void reissue_mismatch() {
        given(jwtTokenProvider.validate("refresh")).willReturn(true);
        given(jwtTokenProvider.getUserId("refresh")).willReturn(1L);
        given(refreshTokenService.find(1L)).willReturn(Optional.of("stored-other"));

        assertThatThrownBy(() -> authService.reissue("refresh"))
                .isInstanceOf(InvalidTokenException.class);

        verify(refreshTokenService).delete(1L);
    }

    @Test
    @DisplayName("로그아웃: refresh를 삭제하고 access를 블랙리스트에 등록한다")
    void logout() {
        given(jwtTokenProvider.validate("access")).willReturn(true);
        given(jwtTokenProvider.getUserId("access")).willReturn(1L);

        authService.logout("access");

        verify(refreshTokenService).delete(1L);
        verify(blacklistService).blacklist("access");
    }
}
