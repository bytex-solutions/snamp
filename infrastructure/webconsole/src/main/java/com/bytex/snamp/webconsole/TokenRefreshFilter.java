package com.bytex.snamp.webconsole;

import com.google.common.collect.ImmutableSet;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;

import javax.security.auth.Subject;
import javax.ws.rs.ext.Provider;
import java.security.Principal;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    public ContainerResponse filter(ContainerRequest containerRequest, ContainerResponse containerResponse) {
        // if user goes to auth method - we do not apply this filter
        if (!containerRequest.getPath().equalsIgnoreCase("auth")) {
            final JwtPrincipal jwtPrincipal;
            if (containerRequest.getSecurityContext() instanceof JwtSecurityContext) {
                jwtPrincipal = ((JwtSecurityContext)
                        containerRequest.getSecurityContext()).getUserPrincipal();
            } else {
                logger.fine(String.format("RequestContext has Security context but no JwtSecurityContext. " +
                         "Actual class is %s. Trying to create security context from token...",
                        containerRequest.getSecurityContext().getClass()));

                jwtPrincipal =  new JwtSecurityContext(containerRequest).getUserPrincipal();
            }
            logger.fine(String.format("TokenRefreshFilter is being applied. JWT principle is %s.", jwtPrincipal));
            // check if the token requires to be updated
            if (jwtPrincipal.isRefreshRequired()) {
                logger.fine("Update required. Doing...");
                final Subject subject = new Subject(false,
                    ImmutableSet.<Principal>builder()
                            .add(new UserPrincipal(jwtPrincipal.getName()))
                            .addAll(jwtPrincipal.getRoles().stream().map(RolePrincipal::new)
                                    .collect(Collectors.toList()))
                            .build(),
                    Collections.emptySet(),
                    Collections.emptySet()
                );
                logger.fine(String.format("Constructed subject: %s", subject));
                containerResponse.getHttpHeaders()
                        .add("Set-Cookie", String.format("%s=%s; Path=/;",
                                WebConsoleService.AUTH_COOKIE, WebConsoleService.issueAuthToken(subject)));

                logger.fine(String.format("Token for user %s was refreshed. New token is %s",
                        jwtPrincipal.getName(), containerResponse.getHttpHeaders().get("Set-Cookie")));

            }
        }
        return containerResponse;
    }
}