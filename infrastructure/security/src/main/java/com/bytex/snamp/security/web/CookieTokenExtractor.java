package com.bytex.snamp.security.web;

import com.google.common.base.Strings;
import com.sun.jersey.api.core.HttpRequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Cookie;
import java.util.Objects;
import java.util.Optional;
import static com.google.common.base.Strings.isNullOrEmpty;

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
        return cookie == null || isNullOrEmpty(cookie.getValue()) ? Optional.empty() : Optional.ofNullable(cookie.getValue());
    }

    @Override
    public Optional<String> extract(final HttpServletRequest request) {
        final javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null)
            for (final javax.servlet.http.Cookie cookie : cookies)
                if (Objects.equals(cookieName, cookie.getName()) && !Strings.isNullOrEmpty(cookie.getValue()))
                    return Optional.ofNullable(cookie.getValue());
        return Optional.empty();
    }
}
