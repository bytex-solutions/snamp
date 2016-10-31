package com.bytex.snamp.webconsole;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Roman Sakno, Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
final class JwtPrincipal implements Principal {

    /**
     * Difference between rest time of the token and its whole lifetime.
     */
    private static final double EXPIRATION_RATE = 0.3;

    private static final Logger logger = Logger.getLogger(JwtPrincipal.class.getName());

    final String secret = "{{secret used for signing}}";

    /**
     * Principle name.
     */
    final String name;

    /**
     * Array of roles (string mode).
     */
    final List<String> roles;

    /**
     * Defines if the token should be refreshed on response filter stage.
     */
    final boolean refreshRequired;


    /**
     * Basic constructor to varify token and fill principle's fields.
     * @param token - JWT token
     * @throws JWTVerifyException
     * @throws SignatureException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IOException
     */
    JwtPrincipal(final String token) throws JWTVerifyException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        final JWTVerifier verifier = new JWTVerifier(secret);
        final Map<String, Object> claims = verifier.verify(token);
        name = (String) claims.get("sub");
        roles = java.util.Arrays.asList(((String) claims.get("roles")).split(";"));

        long tokenCreated = claims.containsKey("iat")? (int) claims.get("iat") :0;
        long expiration = claims.containsKey("exp")? (int) claims.get("exp") :0;
        final long currentTime = System.currentTimeMillis() / 1000L;

        refreshRequired = expiration - currentTime < (expiration - tokenCreated)* EXPIRATION_RATE;

        logger.fine(String.format("Time rest: %s, whole time: %s, time to check: %s, update required: %s",
                expiration - currentTime, expiration - tokenCreated,
                (expiration - tokenCreated)* EXPIRATION_RATE, refreshRequired));
    }

    /**
     * Is refresh required boolean.
     *
     * @return the boolean
     */
    public boolean isRefreshRequired() {
        return this.refreshRequired;
    }

    /**
     * Returns the name of this principal.
     *
     * @return the name of this principal.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Detect if the principle has certain role.
     * @param roleName - string representation of role
     * @return true if does
     */
    boolean isInRole(final String roleName){
        return roles.contains(roleName);
    }

    /**
     * Gets roles.
     *
     * @return the roles
     */
    public List<String> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return "JwtPrincipal{" +
                "secret='" + secret + '\'' +
                ", name='" + name + '\'' +
                ", roles=" + roles +
                ", refreshRequired=" + refreshRequired +
                '}';
    }
}
