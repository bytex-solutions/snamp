package com.bytex.snamp.web.serviceModel;

import java.security.Principal;

/**
 * Represents 
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
public interface PrincipalBoundedService<USERDATA> extends WebConsoleService {
    /**
     * Extracts user settings associated with this service.
     * @param principal User identity. Cannot be {@literal null}.
     * @return User settings associated with this service.
     */
    USERDATA getUserData(final Principal principal);

    void setUserData(final Principal principal, final USERDATA data);

    Class<USERDATA> getUserDataType();
}
