package com.bytex.snamp.security;

import javax.ws.rs.core.SecurityContext;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Role {
    boolean authorize(final SecurityContext context);
}
