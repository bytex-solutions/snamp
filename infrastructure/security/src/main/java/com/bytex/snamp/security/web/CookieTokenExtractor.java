package com.bytex.snamp.security.web;

import com.sun.jersey.api.core.HttpRequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Cookie;
import java.util.Objects;
import java.util.Optional;

/**
 * Extracts token from Cookie.
 */
final class CookieTokenExtractor implements JWTokenExtractor {
    private final String cookieName;

    CookieTokenExtractor(final String cookieName){
        this.cookieName = Objects.requireNonNull(cookieName);
    }

    @Override
    public Optional<String> extract(final HttpRequestContext request) {
        final Cookie cookie = request.getCookies().get(cookieName);
        return cookie == null ? Optional.empty() : Optional.ofNullable(JWTokenExtractor.removeBearerPrefix(cookie.getValue()));
    }

    @Override
    public Optional<String> extract(final HttpServletRequest request) {
        for (final javax.servlet.http.Cookie cookie : request.getCookies())
            if (Objects.equals(cookieName, cookie.getName()))
                return Optional.ofNullable(JWTokenExtractor.removeBearerPrefix(cookie.getValue()));
        return Optional.empty();
    }
}
