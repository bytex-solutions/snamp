package com.bytex.snamp.security.web;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Filter for JWT based auth - refreshes token in case it has 1/3 time to live.
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@Provider
public class TokenRefreshFilter extends SecurityFilter implements ContainerResponseFilter {
    private final Logger logger = Logger.getLogger(TokenRefreshFilter.class.getName());
    private final String authCookieName;
    private final String securedPath;

    public TokenRefreshFilter(final String authCookieName, final String securedPath){
        this.authCookieName = Objects.requireNonNull(authCookieName);
        this.securedPath = Objects.requireNonNull(securedPath);
    }

    public TokenRefreshFilter(final String authCookieName){
        this(authCookieName, "/");
    }

    @Override
    public final ContainerResponse filter(final ContainerRequest containerRequest, final ContainerResponse containerResponse) {
        // if user goes to auth method - we do not apply this filter
        if (authenticationRequired(containerRequest)) {
            final JwtPrincipal jwtPrincipal;
            if (containerRequest.getSecurityContext() instanceof JwtSecurityContext) {
                jwtPrincipal = ((JwtSecurityContext)
                        containerRequest.getSecurityContext()).getUserPrincipal();
            } else {
                logger.fine(() -> String.format("RequestContext has Security context but no JwtSecurityContext. " +
                         "Actual class is %s. Trying to create security context from token...",
                        containerRequest.getSecurityContext().getClass()));

                jwtPrincipal =  new JwtSecurityContext(containerRequest).getUserPrincipal();
            }
            logger.fine(() -> String.format("TokenRefreshFilter is being applied. JWT principle is %s.", jwtPrincipal));
            // check if the token requires to be updated
            if (jwtPrincipal.isRefreshRequired()) {
                logger.fine(() -> String.format("Refresh of the token for user %s is required", jwtPrincipal.getName()));
                final String jwToken = jwtPrincipal.refreshToken().createJwtToken(TokenSecretHolder.getInstance().getSecret());
                containerResponse.getHttpHeaders().add(HttpHeaders.SET_COOKIE, authCookieName + '=' + jwToken + "; Path=" + securedPath + ';');

                logger.fine(() -> String.format("Token for user %s was refreshed. New token is %s",
                        jwtPrincipal.getName(), containerResponse.getHttpHeaders().get(HttpHeaders.SET_COOKIE)));

            }
        }
        return containerResponse;
    }
}