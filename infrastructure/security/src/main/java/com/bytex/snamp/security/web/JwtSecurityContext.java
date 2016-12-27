package com.bytex.snamp.security.web;

import com.auth0.jwt.JWTVerifyException;
import com.bytex.snamp.core.LoggerProvider;
import com.sun.jersey.api.core.HttpRequestContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents security context initialized from JWT token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class JwtSecurityContext implements SecurityContext {
    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtPrincipal principal;
    private final boolean secure;

    private JwtSecurityContext(String jwToken, final String secret, final boolean secure) throws NoSuchAlgorithmException, IOException, InvalidKeyException, SignatureException {
        this.secure = secure;
        jwToken = removeBearerPrefix(jwToken);
        if (isNullOrEmpty(jwToken) || jwToken.equalsIgnoreCase("undefined")) {
            getLogger().warning("Empty token received");
            throw new SignatureException("Empty token received");
        }
        try {
            principal = new JwtPrincipal(jwToken, secret);
        } catch (final JWTVerifyException e) {
            throw new SignatureException(e);
        }
    }

    JwtSecurityContext(final HttpRequestContext request, final String secret) throws NoSuchAlgorithmException, IOException, InvalidKeyException, SignatureException {
        this(request.getHeaderValue(HttpHeaders.AUTHORIZATION), secret, request.isSecure());
    }

    JwtSecurityContext(final HttpServletRequest request, final String secret) throws NoSuchAlgorithmException, IOException, InvalidKeyException, SignatureException {
        this(getNeedfulHeader(request), secret, request.isSecure());
    }

    private static String getNeedfulHeader(final HttpServletRequest request) {
        String value = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (value ==  null || value.isEmpty() && request.getCookies() != null || request.getCookies().length > 0) {
            final Optional<Cookie> found = Arrays.stream(request.getCookies()).filter(cookie ->
                    cookie.getName().equalsIgnoreCase(WebSecurityFilter.DEFAULT_AUTH_COOKIE)
                        && !cookie.getValue().isEmpty()
                        && cookie.getValue().length() > BEARER_PREFIX.length() + 10
            ).findFirst();
            if (found.isPresent()) {
                return found.get().getValue();
            }
        }
        return value;
    }

    private static String removeBearerPrefix(final String str) {
        if (isNullOrEmpty(str))
            return str;
        else if (str.startsWith(BEARER_PREFIX))
            return str.substring(BEARER_PREFIX.length());
        else
            return str;
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    @Override
    public JwtPrincipal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(final String role) {
        return principal.isInRole(role);
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "JWT";
    }
}
