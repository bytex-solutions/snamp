package com.bytex.snamp.webconsole;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
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


    public ContainerRequest filter(ContainerRequest requestContext) {

        // Get the HTTP Authorization header from the request
        String authorizationHeader =
                String.valueOf(requestContext.getRequestHeaders().get(HttpHeaders.AUTHORIZATION));

        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new WebApplicationException(
                    Response
                            .status(Response.Status.UNAUTHORIZED)
                            .entity("Authorization header must be provided")
                            .build()
            );
        }

        // Extract the token from the HTTP Authorization header
        String token = authorizationHeader.substring("Bearer".length()).trim();

        try {
            // Validate the token
            validateToken(token);
        } catch (Exception e) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        }
        return requestContext;
    }

    private void validateToken(String token) throws Exception {
        // Check if it was issued by the server and if it's not expired
        // Throw an Exception if the token is invalid
    }
}