package com.bytex.snamp.webconsole;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import javax.annotation.Priority;
import javax.ws.rs.ext.Provider;

/**
 * Filter for JWT based auth.
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@Provider
@Priority(1)
public class AuthenticationFilter implements ContainerRequestFilter {

    @Override
    public ContainerRequest filter(final ContainerRequest requestContext) {
        final JwtSecurityContext context = new JwtSecurityContext(requestContext);
        requestContext.setSecurityContext(context);
        return requestContext;
    }
}