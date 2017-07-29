package com.bytex.snamp.security;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Objects;

/**
 * Represents anonymous user as principal.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.1
 */
public final class Anonymous implements Principal {
    /**
     * Represents name of the anonymous principal.
     */
    public static final String NAME = "anonymous";

    /**
     * Represents singleton instance of the anonymous principal.
     */
    public static final Anonymous INSTANCE = new Anonymous();

    private Anonymous(){

    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean implies(final Subject subject) {
        for (final Principal p : subject.getPrincipals())
            if (Objects.equals(p.getName(), getName()))
                return true;
        return false;
    }

    @Override
    public int hashCode() {
        return NAME.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof Anonymous;
    }
}
