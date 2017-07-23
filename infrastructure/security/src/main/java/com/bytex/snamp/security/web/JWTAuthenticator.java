package com.bytex.snamp.security.web;

import com.auth0.jwt.JWTVerifyException;
import com.bytex.snamp.core.ClusterMember;

import javax.security.auth.Subject;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.HttpCookie;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.time.Duration;
import java.util.Objects;

/**
 * Represents abstract class for REST controllers with
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class JWTAuthenticator {
    /**
     * System property that can be used to modify lifetime of all JWT tokens in SNAMP.
     */
    public static final String TOKEN_LIFETIME_PROPERTY = "com.bytex.snamp.security.jwt.lifetime";

    /**
     * Default lifetime of token.
     */
    private static final String DEFAULT_TOKEN_LIFETIME = Duration.ofDays(7).toString();

    private final ClusterMember clusterMember;
    private final Duration tokenLifetime;

    public JWTAuthenticator(final ClusterMember clusterMember) {
        this.clusterMember = Objects.requireNonNull(clusterMember);
        final String tokenLifetime = System.getProperty(TOKEN_LIFETIME_PROPERTY, DEFAULT_TOKEN_LIFETIME);
        this.tokenLifetime = Duration.parse(tokenLifetime);
    }

    protected Duration getTokenLifetime() {
        return tokenLifetime;
    }

    protected String getTokenSecret(){
        return TokenSecretHolder.getInstance().getSecret(clusterMember);
    }

    public final Principal verify(final String userName, final String jwToken) throws GeneralSecurityException, IOException {
        final JwtPrincipal principal;
        try {
            principal = new JwtPrincipal(jwToken, getTokenSecret());
        } catch (final JWTVerifyException e) {
            throw new LoginException(e.getMessage());
        }
        if (!userName.equals(principal.getName()))
            throw new LoginException(String.format("Expected user name %s doesn't match to actual user name %s", userName, principal.getName()));
        else
            return principal;
    }

    private JwtPrincipal authenticateImpl(final LoginContext context) throws LoginException {
        //login and issue new JWT token
        context.login();
        final Subject user = context.getSubject();
        if (user == null || user.getPrincipals().isEmpty())
            throw new AccountException("Cannot get any subject from login context");
        else
            return new JwtPrincipal(user, getTokenLifetime());
    }

    /**
     * Provides authentication using login and password.
     * @param context Login context.
     * @return Secured authentication token.
     * @throws LoginException Unable to authenticate user.
     */
    public final String authenticate(final LoginContext context) throws LoginException {
        return authenticateImpl(context).createJwtToken(getTokenSecret());
    }

    protected final HttpCookie authenticate(final LoginContext context, final String cookieName) throws LoginException {
        return authenticateImpl(context).createCookie(cookieName, getTokenSecret());
    }
}
