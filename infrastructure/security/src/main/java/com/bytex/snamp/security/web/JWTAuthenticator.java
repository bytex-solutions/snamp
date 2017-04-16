package com.bytex.snamp.security.web;

import com.auth0.jwt.JWTVerifyException;

import javax.security.auth.Subject;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents abstract class for REST controllers with
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class JWTAuthenticator {

    /**
     * Issue auth token string.
     *
     * @param user the user
     * @return the string
     */
    private String issueAuthToken(final Subject user){
        final JwtPrincipal principal = new JwtPrincipal(user);
        return principal.createJwtToken(getTokenSecret());
    }

    protected String getTokenSecret(){
        return TokenSecretHolder.getInstance().getSecret(getBundleContextOfObject(this));
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

    /**
     * Provides authentication using login and password.
     * @param context Login context.
     * @return Secured authentication token.
     * @throws LoginException Unable to authenticate user.
     */
    public final String authenticate(final LoginContext context) throws LoginException {
        //login and issue new JWT token
        context.login();
        final Subject user = context.getSubject();
        if (user == null || user.getPrincipals().isEmpty())
            throw new AccountException("Cannot get any subject from login context");
        else
            return issueAuthToken(user);
    }
}
