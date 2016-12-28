package com.bytex.snamp.security.web;

import com.sun.jersey.api.core.HttpRequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import java.util.Optional;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Extracts token from HTTP Authorization header.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
final class AuthorizationTokenExtractor implements JWTokenExtractor {

    private static String removeBearerPrefix(final String str) {
        final String BEARER_PREFIX = "Bearer ";
        if (isNullOrEmpty(str))
            return str;
        else if (str.startsWith(BEARER_PREFIX))
            return emptyToNull(str.substring(BEARER_PREFIX.length()));
        else
            return str;
    }

    @Override
    public Optional<String> extract(final HttpRequestContext request) {
        return Optional.ofNullable(removeBearerPrefix(request.getHeaderValue(HttpHeaders.AUTHORIZATION)));
    }

    @Override
    public Optional<String> extract(final HttpServletRequest request) {
        return Optional.ofNullable(removeBearerPrefix(request.getHeader(HttpHeaders.AUTHORIZATION)));
    }
}
