package com.bytex.snamp.security.web;

import com.sun.jersey.spi.container.ContainerRequest;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class SecurityFilter {
    protected boolean authenticationRequired(final ContainerRequest request){
        return true;
    }
}
