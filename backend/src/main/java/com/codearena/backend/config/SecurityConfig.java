package com.codearena.backend.config;

import com.codearena.backend.serviceImpl.UserDetailService;
import com.codearena.backend.utils.security.JWTAuthenticationEntryPoint;
import com.codearena.backend.utils.security.JWTAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JWTAuthenticationEntryPoint point;

    @Autowired
    private JWTAuthenticationFilter filter;

    @Autowired
    private UserDetailService userDetailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CorsConfig corsConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource())) // FIXED: Changed method name
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers(
                                    "/swagger-ui/**",
                                    "/swagger-ui.html",
                                    "/api/users/leaderboard",
                                    "/v3/api-docs/**",
                                    "/v3/api-docs.yaml"
                            ).permitAll()
                            .requestMatchers(
                                    "/api/v1/auth/**",
                                    "/api/v1/ai/**", // REMOVE if you want to protect AI endpoints
                                    "/api/v1/execute/**" // REMOVE if you want to protect execute endpoints
                            ).permitAll()
                            .requestMatchers("/ws-connect/**").permitAll()
                            .requestMatchers("/api/practice/**").authenticated()
                            .anyRequest()
                            .authenticated();
                })
                .exceptionHandling(ex -> ex.authenticationEntryPoint(point))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration builder) throws Exception {
        return builder.getAuthenticationManager();
    }
}