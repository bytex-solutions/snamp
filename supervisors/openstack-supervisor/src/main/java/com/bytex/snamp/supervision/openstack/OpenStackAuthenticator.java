package com.bytex.snamp.supervision.openstack;

import org.openstack4j.api.OSClient;
import org.openstack4j.api.client.CloudProvider;
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
    private String endpoint;
    private CloudProvider cloud = CloudProvider.UNKNOWN;

    final OpenStackAuthenticator<V> setUserName(@Nonnull final String value){
        userName = value;
        return this;
    }

    final OpenStackAuthenticator<V> setPassword(@Nonnull final String value){
        password = value;
        return this;
    }

    final OpenStackAuthenticator<V> setEndpoint(@Nonnull final String value){
        endpoint = value;
        return this;
    }

    final OpenStackAuthenticator<V> setCloudProvider(@Nonnull final CloudProvider value){
        cloud = value;
        return this;
    }

    abstract OpenStackAuthenticator<V> setDomain(final String value);

    abstract OpenStackAuthenticator<V> setProject(final String value);

    @Nonnull
    abstract V builder();

    @Nonnull
    final OSClient<?> authenticate() throws AuthenticationException {
        return builder()
                .endpoint(endpoint)
                .credentials(userName, password)
                .provider(cloud)
                .authenticate();
    }
}
