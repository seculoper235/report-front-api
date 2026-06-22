package com.example.reportfrontapi.domain.user.model;

import com.example.reportfrontapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "RPT_USER",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_email", columnNames = "email")
)
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rpt_user_id", nullable = false)
    private Long userId;    // 유저 일련번호

    @Column(name = "email", length = 100, nullable = false)
    private String email;   // 로그인 이메일(아이디)

    @Column(name = "password", length = 100, nullable = false)
    private String password;    // BCrypt 해시 비밀번호

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role;  // 권한

    @Enumerated(EnumType.STRING)
    @Column(name = "cost_persona", length = 20)
    private CostPersona costPersona;  // 소비 페르소나(첫 로그인 시 선택, null=미선택)

    public static User of(String email, String encodedPassword) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .role(Role.USER)
                .build();
    }

    public void changePersona(CostPersona costPersona) {
        this.costPersona = costPersona;
    }
}
