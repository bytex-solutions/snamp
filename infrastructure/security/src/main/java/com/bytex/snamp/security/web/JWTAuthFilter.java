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
import java.net.HttpCookie;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;
import java.util.Objects;
import java.util.logging.Logger;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Filter for JWT based auth - refreshes token in case it has 1/3 time to live.
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public class JWTAuthFilter implements ContainerResponseFilter, ContainerRequestFilter {

    private static final String PRINCIPAL_ATTRIBUTE = "principal";

    /**
     * Default name of cookie with authentication token.
     */
    public static final String DEFAULT_AUTH_COOKIE = "snamp-auth-token";
    private final String authCookieName;
    private final String securedPath;
    private final ImmutableList<JWTokenExtractor> extractors;
    private final ClusterMember clusterMember;
    private final Logger logger;

    private JWTAuthFilter(final String authCookieName,
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
        logger = LoggerProvider.getLoggerForObject(this);
    }

    public JWTAuthFilter(final String authCookieName,
                         final ClusterMember clusterMember,
                         final JWTokenLocation... tokenLocations){
        this(authCookieName, "/", clusterMember, tokenLocations);
    }

    public JWTAuthFilter(final ClusterMember clusterMember, final JWTokenLocation... tokenLocations) {
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

    private JwtSecurityContext createSecurityContext(final HttpRequestContext request) throws WebApplicationException {
        try {
            return new JwtSecurityContext(request, extractors, getTokenSecret());
        } catch (final InvalidKeyException | SignatureException e) {
            throw new WebApplicationException(e, Response.Status.UNAUTHORIZED);
        } catch (final Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private JwtSecurityContext createSecurityContext(final HttpServletRequest request) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        return new JwtSecurityContext(request, extractors, getTokenSecret());
    }

    public final void filter(final HttpServletRequest request) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException {
        if (authenticationRequired(request)) {
            final JwtSecurityContext securityContext = createSecurityContext(request);
            request.setAttribute(PRINCIPAL_ATTRIBUTE, securityContext.getUserPrincipal());
        }
    }

    public static Principal getPrincipal(final HttpServletRequest request) {
        final Object principal = request.getAttribute(PRINCIPAL_ATTRIBUTE);
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

    private HttpCookie refreshToken(final JwtPrincipal principal,
                                       final ContainerRequest containerRequest) {

        final HttpCookie authCookie = new HttpCookie(authCookieName, principal.refresh().createJwtToken(getTokenSecret()));
        authCookie.setPath(securedPath);
        authCookie.setSecure(containerRequest.isSecure());
        authCookie.setMaxAge(JwtPrincipal.TOKEN_LIFETIME.getSeconds());
        final String FORWARDED_HOST_HEADER = "X-Forwarded-Host";
        final String originalHost = containerRequest.getHeaderValue(FORWARDED_HOST_HEADER);
        if (!isNullOrEmpty(originalHost))
            authCookie.setDomain(originalHost);
        return authCookie;
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
                containerResponse.getHttpHeaders().add(HttpHeaders.SET_COOKIE, refreshToken(principal, containerRequest).toString());
            }
        }
        return containerResponse;
    }
}