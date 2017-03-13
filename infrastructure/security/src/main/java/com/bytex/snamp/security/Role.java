package com.bytex.snamp.security;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.function.Predicate;

/**
 * Represents SNAMP user roles.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public enum Role {
    VIEWER("viewer", "snamp-user"),
    MANAGER("manager", "snamp-supervisor"),
    ADMIN("admin", "snamp-admin");

    private final String roleName;
    private final ImmutableSet<String> aliases;
    public static final ImmutableSortedSet<Role> ALL_ROLES = ImmutableSortedSet.copyOf(values());

    Role(final String roleName, final String... aliases) {
        this.roleName = roleName;
        this.aliases = ImmutableSet.copyOf(aliases);
    }

    /**
     * Gets role name.
     * @return Role name.
     */
    public final String getName(){
        return roleName;
    }

    private boolean authorize(final Predicate<? super String> roleChecker) {
        if (roleChecker.test(getName()))
            return true;
        for (final String alias : aliases)
            if (roleChecker.test(alias))
                return true;
        return false;
    }

    public final boolean authorize(final Predicate<? super String> roleChecker, final boolean smartCheck) {
        if (smartCheck) {
            for (final Role role : ALL_ROLES.headSet(this, true))
                if (role.authorize(roleChecker))
                    return true;
            return false;
        } else
            return authorize(roleChecker);
    }

    public final void authorize(final SecurityContext context, final boolean smartCheck) throws WebApplicationException {
        if (!authorize(context::isUserInRole, smartCheck))
            throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    @Override
    public final String toString() {
        return getName();
    }
}
