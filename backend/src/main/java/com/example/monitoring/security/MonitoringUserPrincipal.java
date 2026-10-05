package com.example.monitoring.security;

import com.example.monitoring.entity.Role;
import com.example.monitoring.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter
public class MonitoringUserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public MonitoringUserPrincipal(Long id, String email, boolean active,
                                   Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.active = active;
        this.authorities = authorities;
    }

    public static MonitoringUserPrincipal from(User user) {
        Collection<GrantedAuthority> auth = user.getRoles().stream()
                .map(Role::getCode)
                .map(code -> new SimpleGrantedAuthority("ROLE_" + code))
                .collect(Collectors.toSet());
        return new MonitoringUserPrincipal(
                user.getId(),
                user.getEmail(),
                Boolean.TRUE.equals(user.getIsActive()),
                auth
        );
    }

    public boolean isAdminOrSuperAdmin() {
        return authorities.stream().anyMatch(a -> {
            String r = a.getAuthority();
            return "ROLE_ADMIN".equals(r) || "ROLE_SUPER_ADMIN".equals(r);
        });
    }

    public boolean isSuperAdmin() {
        return authorities.stream()
                .anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return "";
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
        return active;
    }
}
