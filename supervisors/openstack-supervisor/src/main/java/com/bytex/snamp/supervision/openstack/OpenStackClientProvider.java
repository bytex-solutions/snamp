package com.bytex.snamp.supervision.openstack;

import org.openstack4j.api.OSClient;
import org.openstack4j.api.client.CloudProvider;
import org.openstack4j.api.client.IOSClientBuilder;
import org.openstack4j.api.exceptions.AuthenticationException;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class OpenStackClientProvider<V extends IOSClientBuilder<? extends OSClient<?>, ?>> implements Supplier<OSClient<?>> {
    private String userName;
    private String password;
    private String endpoint;
    private CloudProvider cloud = CloudProvider.UNKNOWN;

    final OpenStackClientProvider<V> setUserName(@Nonnull final String value){
        userName = value;
        return this;
    }

    final OpenStackClientProvider<V> setPassword(@Nonnull final String value){
        password = value;
        return this;
    }

    final OpenStackClientProvider<V> setEndpoint(@Nonnull final String value){
        endpoint = value;
        return this;
    }

    final OpenStackClientProvider<V> setCloudProvider(@Nonnull final CloudProvider value){
        cloud = value;
        return this;
    }

    abstract OpenStackClientProvider<V> setDomain(final String value);

    abstract OpenStackClientProvider<V> setProject(final String value);

    @Nonnull
    abstract V builder();

    @Nonnull
    public final OSClient<?> get() throws AuthenticationException {
        return builder()
                .endpoint(endpoint)
                .credentials(userName, password)
                .provider(cloud)
                .authenticate();
    }
}
