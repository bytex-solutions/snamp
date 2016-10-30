package com.bytex.snamp.webconsole;

import com.sun.jersey.api.core.HttpRequestContext;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Represents security context initialized from JWT token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class JwtSecurityContext implements SecurityContext {
    private final JwtPrincipal principal;
    private final boolean secure;

    JwtSecurityContext(final HttpRequestContext request) throws WebApplicationException{
        secure = request.isSecure();
        // Get the HTTP Authorization header from the request
        final String authorizationHeader = request.getHeaderValue(HttpHeaders.AUTHORIZATION);

        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        // Extract the token from the HTTP Authorization header
        final String token = authorizationHeader.substring("Bearer".length()).trim();
        principal = new JwtPrincipal(token);
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
