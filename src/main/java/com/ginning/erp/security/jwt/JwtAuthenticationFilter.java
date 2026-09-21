package com.ginning.erp.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = extractToken(request);

            if (jwt != null && jwtProvider.validateToken(jwt)) {
                String username = jwtProvider.getUsernameFromToken(jwt);
                List<String> roles = jwtProvider.getRolesFromToken(jwt);

                List<GrantedAuthority> authorities = new ArrayList<>();
                if (roles != null) {
                    authorities.addAll(roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList());
                }

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Failed to set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

}
