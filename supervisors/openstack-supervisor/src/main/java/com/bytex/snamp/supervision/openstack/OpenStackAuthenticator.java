package com.bytex.snamp.supervision.openstack;

import org.openstack4j.api.OSClient;
import org.openstack4j.api.client.IOSClientBuilder;
import org.openstack4j.api.exceptions.AuthenticationException;

import javax.annotation.Nonnull;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class OpenStackAuthenticator<V extends IOSClientBuilder<? extends OSClient<?>, ?>> {
    private String userName;
    private String password;

    final OpenStackAuthenticator setUserName(@Nonnull final String value){
        userName = value;
        return this;
    }

    final OpenStackAuthenticator setPassword(@Nonnull final String value){
        password = value;
        return this;
    }

    @Nonnull
    abstract V builder();

    @Nonnull
    final OSClient<?> authenticate() throws AuthenticationException {
        return builder().credentials(userName, password).authenticate();
    }
}
