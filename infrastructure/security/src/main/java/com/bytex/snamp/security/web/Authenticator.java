package com.bytex.snamp.security.web;

import com.auth0.jwt.JWTVerifyException;

import javax.security.auth.Subject;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;

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

    public final Principal parsePrincipal(final String token) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        try {
            return new JwtPrincipal(token, getTokenSecret());
        } catch (final JWTVerifyException e) {
            throw new SignatureException("JWT is not valid", e);
        }
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
