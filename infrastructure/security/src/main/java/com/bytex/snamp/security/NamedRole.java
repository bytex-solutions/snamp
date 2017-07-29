package com.bytex.snamp.security;

import com.google.common.collect.ImmutableSet;

import javax.ws.rs.core.SecurityContext;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents named role with aliases.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class NamedRole implements Role, Serializable {
    private static final long serialVersionUID = 1691570844453239399L;
    private final ImmutableSet<String> aliases;
    private final String name;

    public NamedRole(final String name, final String... aliases){
        this.name = Objects.requireNonNull(name);
        this.aliases = ImmutableSet.copyOf(aliases);
    }

    /**
     * Gets name of this role.
     * @return Role name.
     */
    public String getName(){
        return name;
    }

    @Override
    public boolean authorize(final SecurityContext context) {
        if (context.isUserInRole(name))
            return true;
        for (final String alias : aliases)
            if (context.isUserInRole(alias))
                return true;
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, aliases);
    }

    private boolean equals(final NamedRole other) {
        return other.name.equals(name) &&
                other.aliases.equals(aliases);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof NamedRole && equals((NamedRole) other);
    }

    @Override
    public String toString() {
        return getName();
    }
}
