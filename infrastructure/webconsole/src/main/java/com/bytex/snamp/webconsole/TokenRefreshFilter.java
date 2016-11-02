package com.bytex.snamp.webconsole;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

/**
 * Filter for JWT based auth - refreshes token in case it has 1/3 time to live.
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@Provider
public class TokenRefreshFilter implements ContainerResponseFilter {

    private static final Logger logger = Logger.getLogger(TokenRefreshFilter.class.getName());

    @Override
    public ContainerResponse filter(final ContainerRequest containerRequest, final ContainerResponse containerResponse) {
        // if user goes to auth method - we do not apply this filter
        if (!containerRequest.getPath().equalsIgnoreCase(WebConsoleService.AUTHENTICATE_PATH)) {
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
                containerResponse.getHttpHeaders()
                        .add("Set-Cookie", String.format("%s=%s; Path=/;",
                                    WebConsoleService.AUTH_COOKIE,
                                    jwtPrincipal.refreshToken().createJwtToken(TokenSecretHolder.getSecret(this))
                                )
                        );

                logger.fine(() -> String.format("Token for user %s was refreshed. New token is %s",
                        jwtPrincipal.getName(), containerResponse.getHttpHeaders().get("Set-Cookie")));

            }
        }
        return containerResponse;
    }
}