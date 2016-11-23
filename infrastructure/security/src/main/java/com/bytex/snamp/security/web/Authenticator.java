package com.bytex.snamp.security.web;

import javax.security.auth.Subject;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * Represents abstract class for REST controllers with
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class Authenticator {

    /**
     * Issue auth token string.
     *
     * @param user the user
     * @return the string
     */
    private String issueAuthToken(final Subject user){
        final JwtPrincipal principal = new JwtPrincipal(user);
        return principal.createJwtToken(TokenSecretHolder.getInstance().getSecret());
    }

    /**
     * Provides authentication using login and password.
     * @param context Login context.
     * @param userName User name.
     * @param password Password.
     * @return Secured authentication token.
     * @throws LoginException Unable to authenticate user.
     */
    public final String authenticate(final LoginContext context, final String userName, final String password) throws LoginException {
        //login and issue new JWT token
        context.login();
        final Subject user = context.getSubject();
        if (user == null || user.getPrincipals().isEmpty())
            throw new AccountException("Cannot get any subject from login context");
        else
            return issueAuthToken(user);
    }
}
