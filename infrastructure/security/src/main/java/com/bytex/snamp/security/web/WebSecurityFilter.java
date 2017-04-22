package com.bytex.snamp.security.web;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.LoggerProvider;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Filter for JWT based auth - refreshes token in case it has 1/3 time to live.
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public class WebSecurityFilter implements ContainerResponseFilter, ContainerRequestFilter {

    private static final String PRINCIPAL_ATTRIBUTE = "principal";

    /**
     * Default name of cookie with authentication token.
     */
    public static final String DEFAULT_AUTH_COOKIE = "snamp-auth-token";
    private final String authCookieName;
    private final String securedPath;
    private final ImmutableList<JWTokenExtractor> extractors;
    private final ClusterMember clusterMember;

    private WebSecurityFilter(final String authCookieName,
                              final String securedPath,
                              final ClusterMember clusterMember,
                              final JWTokenLocation... tokenLocations) {
        this.authCookieName = Objects.requireNonNull(authCookieName);
        this.securedPath = Objects.requireNonNull(securedPath);
        this.clusterMember = Objects.requireNonNull(clusterMember);
        if (tokenLocations.length == 0)
            extractors = ImmutableList.of(new AuthorizationTokenExtractor());
        else {
            final ImmutableList.Builder<JWTokenExtractor> builder = ImmutableList.builder();
            for (final JWTokenLocation location : tokenLocations)
                switch (location) {
                    case AUTHORIZATION_HEADER:
                        builder.add(new AuthorizationTokenExtractor());
                        continue;
                    case COOKIE:
                        builder.add(new CookieTokenExtractor(authCookieName));
                }
            extractors = builder.build();
        }
    }

    public WebSecurityFilter(final String authCookieName,
                             final ClusterMember clusterMember,
                             final JWTokenLocation... tokenLocations){
        this(authCookieName, "/", clusterMember, tokenLocations);
    }

    public WebSecurityFilter(final ClusterMember clusterMember, final JWTokenLocation... tokenLocations) {
        this(DEFAULT_AUTH_COOKIE, clusterMember, tokenLocations);
    }

    protected boolean authenticationRequired(final HttpRequestContext request){
        return true;
    }

    protected boolean authenticationRequired(final HttpServletRequest request){
        return true;
    }

    protected String getTokenSecret(){
        return TokenSecretHolder.getInstance().getSecret(clusterMember);
    }

    private JwtSecurityContext createSecurityContext(final HttpRequestContext request) {
        try {
            return new JwtSecurityContext(request, extractors, getTokenSecret());
        } catch (final NoSuchAlgorithmException | IOException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (final InvalidKeyException | SignatureException e) {
            throw new WebApplicationException(e, Response.Status.UNAUTHORIZED);
        }
    }

    public final void filter(final HttpServletRequest request) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException {
        if(authenticationRequired(request)) {
            final JwtSecurityContext securityContext = new JwtSecurityContext(request, extractors, getTokenSecret());
            request.setAttribute(PRINCIPAL_ATTRIBUTE, securityContext.getUserPrincipal());
        }
    }

    public static Principal getPrincipal(final HttpServletRequest request) {
        Object principal = request.getAttribute(PRINCIPAL_ATTRIBUTE);
        return principal instanceof Principal ? (Principal) principal : request.getUserPrincipal();
    }

    //intercept HTTP request
    @Override
    public final ContainerRequest filter(final ContainerRequest requestContext) {
        // if user goes to auth method - we do not apply this filter
        if (authenticationRequired(requestContext))
            requestContext.setSecurityContext(createSecurityContext(requestContext));
        return requestContext;
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
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
                getLogger().fine(() -> String.format("RequestContext has Security context but not JwtSecurityContext. " +
                                "Actual class is %s. Trying to create security context from token...",
                        containerRequest.getSecurityContext().getClass()));
                principal = createSecurityContext(containerRequest).getUserPrincipal();
            }
            getLogger().fine(() -> String.format("TokenRefreshFilter is being applied. JWT principle is %s.", principal.getName()));
            // check if the token requires to be updated
            if (principal.isRefreshRequired()) {
                getLogger().fine(() -> String.format("Refresh of the token for user %s is required", principal.getName()));
                final String jwToken = principal.refresh().createJwtToken(getTokenSecret());
                containerResponse.getHttpHeaders().add(HttpHeaders.SET_COOKIE, authCookieName + '=' + jwToken + "; Path=" + securedPath + ';');
            }
        }
        return containerResponse;
    }
}