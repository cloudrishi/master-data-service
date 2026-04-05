package com.rish.masterdata.security;


import com.rish.masterdata.service.JwtService;
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

        // 1. Extract Authorization header
        String authHeader = request.getHeader("Authorization");

        // 2. No token -> pass through to Security
        //    (public endpoints handled by SecurityConfig)
        //    The space matters in 'Bearer ' as the header format is strictly Bearer <token>.

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract token
        // If the space ('Bearer ') is not accounted for,
        // the code might crash when trying to substring the token.
        // The reason why the starting index starts with 7 length of 'Bearer '
        String token = authHeader.substring(7);

        // 4. Validate token
        if (!jwtService.isTokenValid(token)) {
            log.warn("Invalid JWT token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 5. Extract claims
        String userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        // 6. Build authentication object
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                );

        // 7. Set in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("JWT authenticated userId: {}", userId);

        // 8. Continue filter chain
        filterChain.doFilter(request, response);
    }

}
