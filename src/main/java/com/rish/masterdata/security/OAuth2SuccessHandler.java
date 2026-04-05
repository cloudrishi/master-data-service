package com.rish.masterdata.security;


import com.rish.masterdata.entity.AccountStatus;
import com.rish.masterdata.entity.AuthProvider;
import com.rish.masterdata.entity.User;
import com.rish.masterdata.entity.UserCredentials;
import com.rish.masterdata.repository.UserCredentialsRepository;
import com.rish.masterdata.repository.UserRepository;
import com.rish.masterdata.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {


    private final UserRepository userRepository;
    private final UserCredentialsRepository credentialsRepository;
    private final JwtService jwtService;

    public void onAuthenticationSuccess (
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        log.info("OAuth2 attributes: {}", oAuth2User.getAttributes());

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        Integer id = oAuth2User.getAttribute("id");
        String login = oAuth2User.getAttribute("login");
        String githubId = id != null ? id.toString() : login;

        // Find or create user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createOAuthUser(email, login, name, githubId));

        // Generate JWT
        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        // Redirect with token
        getRedirectStrategy().sendRedirect(
                request,
                response,
                "/api/v1/auth/oauth2/success?token=" + token
        );
    }

    private User createOAuthUser(String email,
                                 String login,
                                 String name,
                                 String githubId) {


        // Split name into first/last
        String[] parts = (name != null && !name.isEmpty()) ?
                name.split(" ", 2) : new String[]{login, ""};

        String firstName   = parts[0];
        String lastName    = parts.length > 1 ? parts[1] : "";

        // Create user
        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .build();

        User savedUser = userRepository.save(user);

        // Create credentials — OAuth, no password
        UserCredentials credentials = UserCredentials.builder()
                .user(savedUser)
                .hashedPassword("")           // no password for OAuth
                .authProvider(AuthProvider.GITHUB)
                .providerUserId(githubId)
                .build();

        credentialsRepository.save(credentials);

        // Activate immediately — GitHub verified email
        savedUser.setAccountStatus(AccountStatus.ACTIVE);
        return userRepository.save(savedUser);

    }
}
