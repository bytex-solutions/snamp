package com.bytex.snamp.supervision.openstack;

import org.openstack4j.api.client.IOSClientBuilder.V2;
import org.openstack4j.openstack.OSFactory;

import javax.annotation.Nonnull;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class OpenStackAuthenticatorV2 extends OpenStackAuthenticator<V2> {
    static final String VERSION = "V2";

    @Nonnull
    @Override
    V2 builder() {
        return OSFactory.builderV2();
    }
}
