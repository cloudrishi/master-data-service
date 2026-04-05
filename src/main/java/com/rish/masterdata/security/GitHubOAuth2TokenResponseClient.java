package com.rish.masterdata.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.endpoint.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.endpoint.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
public class GitHubOAuth2TokenResponseClient implements
        OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(
            OAuth2AuthorizationCodeGrantRequest request) {

        // Build request body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", request.getClientRegistration()
                .getClientId());
        body.add("client_secret", request.getClientRegistration()
                .getClientSecret());
        body.add("code", request.getAuthorizationExchange()
                .getAuthorizationResponse().getCode());
        body.add("redirect_uri", "http://localhost:8080/login/oauth2/code/github");

        log.info("Redirect URI being sent: {}",
                request.getClientRegistration().getRedirectUri());

        // Build headers — tell GitHub we want JSON back
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(
                MediaType.APPLICATION_JSON));
        headers.setContentType(
                MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity =
                new HttpEntity<>(body, headers);

        // Call GitHub token endpoint
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://github.com/login/oauth/access_token",
                HttpMethod.POST,
                entity,
                Map.class
        );

        log.info("------------------GitHub full response: {}----------------", response);
        Map<String, Object> tokenResponse = response.getBody();

        log.info("GitHub full response: {}", tokenResponse);

        if (tokenResponse.containsKey("error")) {
            throw new RuntimeException(
                    "GitHub OAuth error: " + tokenResponse.get("error")
                            + " — " + tokenResponse.get("error_description")
            );
        }
        String accessToken = (String) tokenResponse
                .get("access_token");
        String scope = (String) tokenResponse
                .getOrDefault("scope", "");

        return OAuth2AccessTokenResponse
                .withToken(accessToken)
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .scopes(Collections.singleton(scope))
                .build();
    }
}