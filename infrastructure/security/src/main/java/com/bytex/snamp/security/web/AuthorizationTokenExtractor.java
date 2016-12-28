package com.bytex.snamp.security.web;

import com.sun.jersey.api.core.HttpRequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import java.util.Optional;

/**
 * Extracts token from HTTP Authorization header.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
final class AuthorizationTokenExtractor implements JWTokenExtractor {
    @Override
    public Optional<String> extract(final HttpRequestContext request) {
        return Optional.ofNullable(JWTokenExtractor.removeBearerPrefix(request.getHeaderValue(HttpHeaders.AUTHORIZATION)));
    }

    @Override
    public Optional<String> extract(final HttpServletRequest request) {
        return Optional.ofNullable(JWTokenExtractor.removeBearerPrefix(request.getHeader(HttpHeaders.AUTHORIZATION)));
    }
}
