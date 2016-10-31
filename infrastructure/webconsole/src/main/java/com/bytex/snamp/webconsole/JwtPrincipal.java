package com.bytex.snamp.webconsole;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;

import javax.security.auth.Subject;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;
import java.time.Duration;
import java.util.*;
import java.util.function.LongSupplier;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import static com.bytex.snamp.MapUtils.getValueAsLong;

/**
 * @author Roman Sakno, Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
final class JwtPrincipal implements Principal {
    private static final String ROLE_SPLITTER_STR = ";";
    private static final Joiner ROLE_JOINER = Joiner.on(ROLE_SPLITTER_STR).skipNulls();
    private static final Splitter ROLE_SPLITTER = Splitter.on(ROLE_SPLITTER_STR).trimResults();
    /**
     * The Token lifetime.
     */
    private static final Duration TOKEN_LIFETIME = Duration.ofDays(7);
    private static final String SUBJECT_FIELD = "sub";
    private static final String ROLES_FIELD = "roles";
    private static final String ISSUED_AT_FIELD = "iat";
    private static final String EXPIRATION_FIELD = "exp";

    /**
     * Difference between rest time of the token and its whole lifetime.
     */
    private static final float EXPIRATION_RATE = 0.3F;

    /**
     * Principle name.
     */
    private final String name;

    /**
     * Array of roles (string mode).
     */
    private final Set<String> roles;

    private final long createdAt;
    private final long expiredAt;

    JwtPrincipal(final String userName, final Collection<String> roles){
        createdAt = System.currentTimeMillis();
        expiredAt = createdAt + TOKEN_LIFETIME.toMillis();
        name = Objects.requireNonNull(userName);
        this.roles = ImmutableSet.copyOf(roles);
    }

    /**
     * Reconstructs JWT principal using authenticated subject.
     * @param subj Authenticated subject. Cannot be {@literal null}.
     */
    JwtPrincipal(final Subject subj){
        this(getUserName(subj), getRoles(subj));
    }

    private static String getUserName(final Subject subj){
        return subj.getPrincipals(UserPrincipal.class)
                .stream()
                .map(UserPrincipal::getName)
                .findFirst()
                .orElse("anonymous");
    }

    private static Set<String> getRoles(final Subject subj){
        return subj.getPrincipals(RolePrincipal.class)
                .stream()
                .map(RolePrincipal::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Basic constructor to verify token and fill principle's fields.
     * @param token JWT token
     * @param secret A secret used to verify JWT token.
     * @throws JWTVerifyException
     * @throws SignatureException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IOException
     */
    JwtPrincipal(final String token, final String secret) throws JWTVerifyException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        final JWTVerifier verifier = new JWTVerifier(secret);
        final Map<String, Object> claims = verifier.verify(token);
        if(claims.containsKey(SUBJECT_FIELD))
            name = Objects.toString(claims.get(SUBJECT_FIELD));
        else
            throw new JWTVerifyException("Subject is not specified");
        if(claims.containsKey(ROLES_FIELD))
            roles = ImmutableSet.copyOf(
                ROLE_SPLITTER.split(Objects.toString(claims.get(ROLES_FIELD)))
            );
        else
            throw new JWTVerifyException("Roles are not specified");

        final ToLongFunction<Object> OBJ_TO_INT = iat -> iat instanceof Number ? ((Number)iat).longValue() : 0L;
        final LongSupplier ZERO = () -> 0;

        createdAt = getValueAsLong(claims, ISSUED_AT_FIELD, OBJ_TO_INT, ZERO);
        expiredAt = getValueAsLong(claims, EXPIRATION_FIELD, OBJ_TO_INT, ZERO);
    }

    @Override
    public boolean implies(final Subject subject) {
        final String userName = getUserName(subject);
        final Set<String> roles = getRoles(subject);
        return name.equals(userName) && this.roles.equals(roles);
    }

    /**
     * Defines if the token should be refreshed on response filter stage.
     *
     * @return the boolean
     */
    boolean isRefreshRequired() {
        final long currentTime = System.currentTimeMillis();
        return expiredAt - currentTime < (expiredAt - createdAt) * EXPIRATION_RATE;
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
    Set<String> getRoles() {
        return roles;
    }

    String asJwtString(final String secret){
        final ImmutableMap<String, Object> claims = ImmutableMap.of(
                SUBJECT_FIELD, name,
                ISSUED_AT_FIELD, createdAt,
                EXPIRATION_FIELD, expiredAt,
                ROLES_FIELD, ROLE_JOINER.join(roles)
        );
        return new JWTSigner(secret).sign(claims);
    }

    @Override
    public String toString() {
        return "JwtPrincipal{" +
                ", name='" + name + '\'' +
                ", roles=" + roles +
                ", refreshRequired=" + isRefreshRequired() +
                '}';
    }
}
