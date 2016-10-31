package com.bytex.snamp.webconsole;

import com.auth0.jwt.JWTVerifyException;
import com.bytex.snamp.Box;
import com.bytex.snamp.core.DistributedServices;
import com.sun.jersey.api.core.HttpRequestContext;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Represents security context initialized from JWT token.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class JwtSecurityContext implements SecurityContext {

    static {
        final Box<Object> box = DistributedServices.getProcessLocalBox("JWT_SECRET");
        SECRET = box.hasValue() ? String.valueOf(box.get()) :
                String.valueOf(box.setIfAbsent(() -> UUID.randomUUID().toString()));
/*        if (!box.hasValue()) {
            box.setIfAbsent(() -> UUID.randomUUID().toString());
        }
        SECRET = String.valueOf(box.get());*/
    }
    /**
     * The Secret.
     */
    static final String SECRET;

    private final JwtPrincipal principal;
    private final boolean secure;

    private static final Logger logger = Logger.getLogger(JwtSecurityContext.class.getName());

    JwtSecurityContext(final HttpRequestContext request) throws WebApplicationException {
        secure = request.isSecure();
        // Get the HTTP Authorization header from the request
        final String authorizationHeader = request.getHeaderValue(HttpHeaders.AUTHORIZATION);

        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        // Extract the token from the HTTP Authorization header
        final String token = authorizationHeader.substring("Bearer".length()).trim();
        if (token.isEmpty() || token.equalsIgnoreCase("undefined")) {
            logger.info("Empty token received");
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        try {
            principal = new JwtPrincipal(token, SECRET);
        } catch (final JWTVerifyException | GeneralSecurityException | IOException e) {
            throw new WebApplicationException(e, Response.Status.UNAUTHORIZED);
        }
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
