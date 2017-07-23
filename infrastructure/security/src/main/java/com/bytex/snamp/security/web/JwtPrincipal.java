package com.bytex.snamp.security.web;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.bytex.snamp.Convert;
import com.bytex.snamp.ImportClass;
import com.bytex.snamp.MapUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.Subject;
import java.io.IOException;
import java.net.HttpCookie;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Roman Sakno, Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@ImportClass({Mac.class, SecretKeySpec.class})
final class JwtPrincipal implements Principal {
    private static final char ROLE_SPLITTER_STR = ';';
    private static final Joiner ROLE_JOINER = Joiner.on(ROLE_SPLITTER_STR).skipNulls();
    private static final Splitter ROLE_SPLITTER = Splitter.on(ROLE_SPLITTER_STR).trimResults();

    private static final String SUBJECT_FIELD = "sub";
    private static final String ROLES_FIELD = "roles";
    private static final String ISSUED_AT_FIELD = "iat";
    private static final String EXPIRATION_FIELD = "exp";
    private static final String ANONYMOUS_USER_NAME = "anonymous";


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
    private final ImmutableSet<String> roles;

    private final long createdAt;
    private final long expiredAt;

    /**
     * Instantiates a new Jwt principal.
     *
     * @param userName the user name
     * @param roles    the roles
     * @param lifeTime Token lifetime in milliseconds.
     */
    JwtPrincipal(final String userName, final Collection<String> roles, final long lifeTime) {
        createdAt = System.currentTimeMillis();
        expiredAt = createdAt + lifeTime;
        name = Objects.requireNonNull(userName);
        this.roles = ImmutableSet.copyOf(roles);
    }

    /**
     * Reconstructs JWT principal using authenticated subject.
     * @param subj Authenticated subject. Cannot be {@literal null}.
     * @param lifeTime Token lifetime. Cannot be {@literal null}.
     */
    JwtPrincipal(final Subject subj, final Duration lifeTime){
        this(getUserName(subj), getRoles(subj), lifeTime.toMillis());
    }

    private static String getUserName(final Subject subj){
        return subj.getPrincipals(UserPrincipal.class)
                .stream()
                .map(UserPrincipal::getName)
                .findFirst()
                .orElse(ANONYMOUS_USER_NAME);
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
     * @param secret A secret used to verify token.
     * @throws JWTVerifyException
     * @throws SignatureException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IOException
     */
    JwtPrincipal(final String token, final String secret) throws JWTVerifyException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        final JWTVerifier verifier = new JWTVerifier(secret);
        final Map<String, Object> claims = verifier.verify(token);
        //extract subject name from JWT
        if(claims.containsKey(SUBJECT_FIELD))
            name = Objects.toString(claims.get(SUBJECT_FIELD));
        else
            throw new JWTVerifyException("Subject is not specified");

        //extract set of roles from JWT
        if(claims.containsKey(ROLES_FIELD))
            roles = ImmutableSet.copyOf(ROLE_SPLITTER.split(MapUtils.getValue(claims, ROLES_FIELD, Objects::toString).orElse("")));
        else
            throw new JWTVerifyException("Roles are not specified");


        createdAt = Convert.toLong(claims.get(ISSUED_AT_FIELD)).orElse(0L);
        expiredAt = Convert.toLong(claims.get(EXPIRATION_FIELD)).orElse(0L);
    }

    @Override
    public boolean implies(final Subject subject) {
        final String userName = getUserName(subject);
        final Set<String> roles = getRoles(subject);
        return name.equals(userName) && roles.containsAll(this.roles);
    }

    /**
     * Refresh if required.
     */
    JwtPrincipal refresh() {
        return new JwtPrincipal(name, roles, getLifetime());
    }

    /**
     * Defines if the token should be refreshed on response filter stage.
     *
     * @return the boolean
     */
    boolean isRefreshRequired() {
        return expiredAt - System.currentTimeMillis() < (expiredAt - createdAt) * EXPIRATION_RATE;
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

    String createJwtToken(final String secret){
        final ImmutableMap<String, Object> claims = ImmutableMap.of(
                SUBJECT_FIELD, name,
                ISSUED_AT_FIELD, createdAt,
                EXPIRATION_FIELD, expiredAt,
                ROLES_FIELD, ROLE_JOINER.join(roles)
        );
        return new JWTSigner(secret).sign(claims);
    }

    private long getLifetime(){
        return expiredAt - createdAt;
    }

    HttpCookie createCookie(final String cookieName, final String secret) {
        final HttpCookie cookie = new HttpCookie(cookieName, createJwtToken(secret));
        cookie.setMaxAge(TimeUnit.MILLISECONDS.toSeconds(getLifetime()));
        cookie.setComment("SNAMP JWT authentication token");
        return cookie;
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
