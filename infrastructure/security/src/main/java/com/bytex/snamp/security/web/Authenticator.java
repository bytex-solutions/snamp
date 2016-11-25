package com.bytex.snamp.security.web;

import javax.security.auth.Subject;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

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
        return principal.createJwtToken(getTokenSecret());
    }

    protected String getTokenSecret(){
        return TokenSecretHolder.getInstance().getSecret(getBundleContextOfObject(this));
    }

    /**
     * Provides authentication using login and password.
     * @param context Login context.
==
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
