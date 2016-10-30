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

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class JwtPrincipal implements Principal {

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
     * Basic contructor to varify token and fill principle's fields.
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
}
