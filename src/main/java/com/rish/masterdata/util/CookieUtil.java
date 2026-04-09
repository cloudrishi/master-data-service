package com.rish.masterdata.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

    private static final String COOKIE_NAME="jwt_token";
    private static final int MAX_AGE = 86400; // 24 hours

    // Set HttpOnly JWT cookie
    public static void setJwtCookie(
            HttpServletResponse response,
            String token) {

        ResponseCookie cookie = ResponseCookie
                .from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(MAX_AGE)
                .sameSite("Lax")
                .build();
        response.addHeader(
            "Set-Cookie",
            cookie.toString()
        );


    }

    public static String getJwtFromCookie(
            HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static void clearJwtCookie(
            HttpServletResponse response) {

        ResponseCookie cookie = ResponseCookie
                .from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(
                "Set-Cookie",
                cookie.toString()
        );
    }
}
