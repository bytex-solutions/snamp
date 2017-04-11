package com.bytex.snamp.supervision.openstack;

import org.openstack4j.api.client.IOSClientBuilder.V2;
import org.openstack4j.openstack.OSFactory;

import javax.annotation.Nonnull;

import static com.google.common.base.Strings.emptyToNull;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class OpenStackAuthenticatorV2 extends OpenStackAuthenticator<V2> {
    static final String VERSION = "V2";
    private String tenantName;

    @Override
    OpenStackAuthenticatorV2 setDomain(@Nonnull final String domain) {
        return this;    //nothing to do because domains are not supported in V2
    }

    @Override
    OpenStackAuthenticatorV2 setProject(final String value) {
        tenantName = emptyToNull(value);
        return this;
    }

    @Nonnull
    @Override
    V2 builder() {
        return OSFactory.builderV2().tenantName(tenantName);
    }
}
