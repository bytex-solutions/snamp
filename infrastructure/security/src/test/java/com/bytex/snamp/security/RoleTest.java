package com.bytex.snamp.security;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Predicate;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class RoleTest extends Assert {
    @Test
    public void authorizationTest(){
        final Predicate<String> roleChecker = "snamp-user"::equals;
        assertTrue(Role.ADMIN.authorize(roleChecker, true));
        assertFalse(Role.ADMIN.authorize(roleChecker, false));
    }
}
