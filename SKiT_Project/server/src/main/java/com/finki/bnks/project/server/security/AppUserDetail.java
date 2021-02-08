package com.finki.bnks.project.server.security;

import com.finki.bnks.project.server.domain.model.AppUser;
import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;
import java.util.Set;

public class AppUserDetail {
    private final Long appUserId;
    private final String username;
    private final boolean enabled;
    private final String secret;
    private final Set<GrantedAuthority> authorities;

    public AppUserDetail(AppUser user){
        this.appUserId = user.getId();
        this.username = user.getUsername();
        this.authorities = Set.of();
        this.secret = user.getSecret();
        this.enabled = Objects.requireNonNullElse(user.getEnabled(), false);
    }

    public Long getAppUserId() {
        return appUserId;
    }

    public String getUsername() {
        return username;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getSecret() {
        return secret;
    }

    public Set<GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
