package com.carmaintenance.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 用户主体类 - 用于Spring Security认证
 */
@Data
public class UserPrincipal implements UserDetails {

    private Long userId;
    private String username;
    private Integer userType;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long userId, String username, Integer userType, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.userType = userType;
        this.authorities = authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}