package com.bytex.snamp.security.web;

import com.bytex.snamp.core.ClusterMember;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class AuthCookieTest extends Assert {
    private final ClusterMember member = ClusterMember.get(null);

    @Test
    public void cookieToStringTest(){
        final JwtPrincipal principal = new JwtPrincipal("user", ImmutableList.of("role"), 10_000L);
        final String secret = TokenSecretHolder.getInstance().getSecret(member);
        final String cookie = principal.createCookie(JWTAuthFilter.DEFAULT_AUTH_COOKIE, secret).toString();
        assertNotNull(cookie);
    }
}
