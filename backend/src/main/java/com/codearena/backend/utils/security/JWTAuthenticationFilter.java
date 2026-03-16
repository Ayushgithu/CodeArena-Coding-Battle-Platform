package com.codearena.backend.utils.security;

import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/signup/request-otp",
            "/api/v1/auth/signup/verify",
            "/api/v1/auth/forgot-password/request-otp",
            "/api/v1/auth/forgot-password/verify",
            "/v3/api-docs",
            "/v3/api-docs.yaml",
            "/swagger-ui",
            "/swagger-ui.html",
            "/ws-connect",
            "/ws-connect/**"
    );

    @Autowired
    private JWTHelper jwtHelper;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Check if endpoint is public
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
            token = requestHeader.substring(7);
            try {
                username = jwtHelper.getUsernameFromToken(token);
                logger.debug("Extracted username from token: {}", username);
            } catch (Exception e) {
                logger.error("Failed to extract username from token: {}", e.getMessage());
            }
        } else {
            logger.warn("JWT Token does not begin with Bearer String");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (jwtHelper.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Authenticated user: {}", username);
                } else {
                    logger.warn("JWT validation failed for token");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                    return;
                }
            } catch (Exception e) {
                logger.error("Error loading user details: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                return;
            }
        } else if (!isPublicEndpoint(path)) {
            logger.warn("Unauthorized access attempt to: {}", path);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }
}