package com.codearena.backend.serviceImpl;

import com.codearena.backend.config.CustomUserDetails;
import com.codearena.backend.entity.User;
import com.codearena.backend.entity.UserProfile;
import com.codearena.backend.repository.UserProfileRepository;
import com.codearena.backend.repository.UserRepository;
import com.codearena.backend.service.UserDetail;
import com.codearena.backend.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }



    // ==========================================
    // PART 1: SPRING SECURITY (Login Logic)
    // ==========================================
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getName()));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}