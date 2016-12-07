package com.bytex.snamp.security.web;

import com.auth0.jwt.JWTVerifyException;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Objects;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Filter for JWT based auth - refreshes token in case it has 1/3 time to live.
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public class WebSecurityFilter implements ContainerResponseFilter, ContainerRequestFilter {
    /**
     * Default name of cookie with authentication token.
     */
    public static final String DEFAULT_AUTH_COOKIE = "snamp-auth-token";
    private final Logger logger = Logger.getLogger(WebSecurityFilter.class.getName());
    private final String authCookieName;
    private final String securedPath;

    private WebSecurityFilter(final String authCookieName, final String securedPath){
        this.authCookieName = Objects.requireNonNull(authCookieName);
        this.securedPath = Objects.requireNonNull(securedPath);
    }

    public WebSecurityFilter(final String authCookieName){
        this(authCookieName, "/");
    }

    public WebSecurityFilter(){
        this(DEFAULT_AUTH_COOKIE);
    }

    protected boolean authenticationRequired(final ContainerRequest request){
        return true;
    }

    private String getTokenSecret(){
        return TokenSecretHolder.getInstance().getSecret(getBundleContextOfObject(this));
    }

    private JwtSecurityContext createSecurityContext(final ContainerRequest request){
        try {
            return new JwtSecurityContext(request, getTokenSecret());
        } catch (final NoSuchAlgorithmException | IOException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (final JWTVerifyException | InvalidKeyException | SignatureException e){
            throw new WebApplicationException(e, Response.Status.UNAUTHORIZED);
        }
    }

    //intercept HTTP request
    @Override
    public final ContainerRequest filter(final ContainerRequest requestContext) {
        // if user goes to auth method - we do not apply this filter
        if (authenticationRequired(requestContext))
            requestContext.setSecurityContext(createSecurityContext(requestContext));
        return requestContext;
    }

    //intercept HTTP response
    @Override
    public final ContainerResponse filter(final ContainerRequest containerRequest, final ContainerResponse containerResponse) {
        // if user goes to auth method - we do not apply this filter
        if (authenticationRequired(containerRequest)) {
            final JwtPrincipal principal;
            if (containerRequest.getSecurityContext() instanceof JwtSecurityContext) {
                principal = ((JwtSecurityContext)
                        containerRequest.getSecurityContext()).getUserPrincipal();
            } else {
                logger.fine(() -> String.format("RequestContext has Security context but not JwtSecurityContext. " +
                                "Actual class is %s. Trying to create security context from token...",
                        containerRequest.getSecurityContext().getClass()));
                principal = createSecurityContext(containerRequest).getUserPrincipal();
            }
            logger.fine(() -> String.format("TokenRefreshFilter is being applied. JWT principle is %s.", principal.getName()));
            // check if the token requires to be updated
            if (principal.isRefreshRequired()) {
                logger.fine(() -> String.format("Refresh of the token for user %s is required", principal.getName()));
                final String jwToken = principal.refresh().createJwtToken(getTokenSecret());
                containerResponse.getHttpHeaders().add(HttpHeaders.SET_COOKIE, authCookieName + '=' + jwToken + "; Path=" + securedPath + ';');
            }
        }
        return containerResponse;
    }
}