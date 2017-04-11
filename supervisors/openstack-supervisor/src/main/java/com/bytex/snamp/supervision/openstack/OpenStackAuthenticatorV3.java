package com.bytex.snamp.supervision.openstack;

import org.openstack4j.api.client.IOSClientBuilder.V3;
import org.openstack4j.openstack.OSFactory;

import javax.annotation.Nonnull;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class OpenStackAuthenticatorV3 extends OpenStackAuthenticator<V3> {
    static final String VERSION = "V3";

    @Nonnull
    @Override
    V3 builder() {
        return OSFactory.builderV3();
    }
}
