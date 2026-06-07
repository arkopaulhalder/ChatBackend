package com.ChatSystem.AuthService.security;

import com.ChatSystem.AuthService.entity.AuthUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    @Getter
    private final AuthUser authUser;

    public CustomUserDetails(AuthUser authUser) {
        this.authUser = authUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authUser.getUserRoles().stream()
                .map(ur -> new SimpleGrantedAuthority(ur.getRole().getName()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return authUser.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return authUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return authUser.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return authUser.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return authUser.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return authUser.isEnabled();
    }
}