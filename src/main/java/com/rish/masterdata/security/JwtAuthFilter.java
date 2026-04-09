package com.rish.masterdata.security;


import com.rish.masterdata.service.JwtService;
import com.rish.masterdata.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip JWT filter for OAuth2 routes
        return path.startsWith("/oauth2/") ||
                path.startsWith("/login/oauth2/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;

        // Try cookie first ← new
        token = CookieUtil.getJwtFromCookie(request);

        // Fall back to Authorization header
        if (token == null) {
            String authHeader =
                    request.getHeader("Authorization");
            if (authHeader != null &&
                    authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        // No token → pass through
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Validate token
        if (!jwtService.isTokenValid(token)) {
            log.warn("Invalid JWT token");
            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String userId = jwtService.extractUserId(token);
        String role   = jwtService.extractRole(token);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        log.debug("JWT authenticated userId: {}", userId);
        filterChain.doFilter(request, response);
    }
}
