package com.codearena.backend.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {
    private final String userId;
    private final String email;
    private final String name;

    public CustomUserDetails(String username, String password,
                             Collection<? extends GrantedAuthority> authorities,
                             String userId, String email, String name) {
        super(username, password, authorities);
        this.userId = userId;
        this.email = email;
        this.name = name;
    }
}