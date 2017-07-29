package com.bytex.snamp.security;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Provides role-based access control for Jersey-based services.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class RBAC {
    /**
     * Represents SNAMP user.
     * <p>
     *     SNAMP user is not allowed to modify global configuration but may change any user settings in Web Console.
     */
    public static final Role USER_ROLE = new NamedRole("viewer", "snamp-user");
    /**
     * Represents SNAMP admin.
     * <p>
     *     SNAMP admin can modify global configuration of the system.
     */
    public static final Role ADMIN_ROLE = new NamedRole("admin", "snamp-admin");

    private RBAC(){
        throw new InstantiationError();
    }

    /**
     * Checks authorization of request associated with the specified security context.
     * @param context Security context.
     * @param allowedRoles Allowed roles.
     * @return {@literal true} for successful authorization; otherwise, {@literal false}.
     */
    public static boolean checkAuthorization(final SecurityContext context, final Iterable<Role>allowedRoles) {
        for (final Role r : allowedRoles)
            if (r.authorize(context))
                return true;
        return false;
    }

    /**
     * Throws exception if authorization failed.
     * @param context Security context.
     * @param allowedRoles Allowed roles.
     * @throws WebApplicationException Authorization failed.
     */
    public static void authorize(final SecurityContext context, final Iterable<Role> allowedRoles) throws WebApplicationException {
        if (!checkAuthorization(context, allowedRoles))
            throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
}
