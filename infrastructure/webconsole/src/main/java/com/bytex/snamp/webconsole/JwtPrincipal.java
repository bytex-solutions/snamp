package com.bytex.snamp.webconsole;

import com.auth0.jwt.JWTVerifyException;

import javax.security.auth.Subject;
import java.security.Principal;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class JwtPrincipal implements Principal {
    JwtPrincipal(final String token) throws JWTVerifyException{

    }

    /**
     * Returns the name of this principal.
     *
     * @return the name of this principal.
     */
    @Override
    public String getName() {
        return null;
    }

    boolean isInRole(final String roleName){
        return false;
    }

    /**
     * Returns true if the specified subject is implied by this principal.
     * <p>
     * <p>The default implementation of this method returns true if
     * {@code subject} is non-null and contains at least one principal that
     * is equal to this principal.
     * <p>
     * <p>Subclasses may override this with a different implementation, if
     * necessary.
     *
     * @param subject the {@code Subject}
     * @return true if {@code subject} is non-null and is
     * implied by this principal, or false otherwise.
     * @since 1.8
     */
    @Override
    public boolean implies(final Subject subject) {
        return false;
    }
}
