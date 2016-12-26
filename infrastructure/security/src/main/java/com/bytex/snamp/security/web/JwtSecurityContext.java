package com.bytex.snamp.security.web;

import com.auth0.jwt.JWTVerifyException;
import com.bytex.snamp.core.LoggerProvider;
import com.sun.jersey.api.core.HttpRequestContext;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.logging.Logger;

/**
 * Represents security context initialized from JWT token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class JwtSecurityContext implements SecurityContext {
    private final JwtPrincipal principal;
    private final boolean secure;

    private JwtSecurityContext(final String jwToken, final String secret, final boolean secure) throws NoSuchAlgorithmException, IOException, InvalidKeyException, SignatureException {
        this.secure = secure;
        if (jwToken.isEmpty() || jwToken.equalsIgnoreCase("undefined")) {
            getLogger().warning("Empty token received");
            throw new SignatureException("Empty token received");
        }
        try {
            principal = new JwtPrincipal(jwToken, secret);
        } catch (final JWTVerifyException e){
            throw new SignatureException(e);
        }
    }

    JwtSecurityContext(final HttpRequestContext request, final String secret) throws NoSuchAlgorithmException, IOException, InvalidKeyException, SignatureException {
        this(request.getHeaderValue(HttpHeaders.AUTHORIZATION), secret, request.isSecure());
    }

    JwtSecurityContext(final HttpServletRequest request, final String secret) throws NoSuchAlgorithmException, IOException, InvalidKeyException, SignatureException {
        this(request.getHeader(HttpHeaders.AUTHORIZATION), secret, request.isSecure());
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
