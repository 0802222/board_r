package com.cho.board.global.security;

import com.cho.board.user.entity.User;
import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(
            new SimpleGrantedAuthority(user.getRole().getKey())
        );
    }

    @Override
    public String getUsername() {
        // CustomUserDetails 에서 email 사용하므로 email 을 반환함
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 안함
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 안함
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 안함
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화
    }

    // 실제 User 엔티티에 접근할 때 사용
    public Long getUserId() {
        return user.getId();
    }
}
