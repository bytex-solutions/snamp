package com.bytex.snamp.webconsole;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import javax.ws.rs.ext.Provider;

/**
 * Filter for JWT based auth.
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@Provider
public class AuthenticationFilter implements ContainerRequestFilter {

    @Override
    public ContainerRequest filter(final ContainerRequest requestContext) {
        // if user goes to auth method - we do not apply this filter
        if (!requestContext.getPath().equalsIgnoreCase("auth")) {
            final JwtSecurityContext context = new JwtSecurityContext(requestContext);
            requestContext.setSecurityContext(context);
        }
        return requestContext;
    }
}