package com.bytex.snamp.supervision.openstack;

import org.openstack4j.api.client.IOSClientBuilder.V3;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;

import javax.annotation.Nonnull;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class OpenStackAuthenticatorV3 extends OpenStackAuthenticator<V3> {
    static final String VERSION = "V3";
    private String domainName;
    private Identifier project;

    @Override
    OpenStackAuthenticatorV3 setDomain(final String value) {
        domainName = value;
        return this;
    }

    @Override
    OpenStackAuthenticator<V3> setProject(final String value) {
        project = isNullOrEmpty(value) ? null : Identifier.byName(value);
        return this;  //nothing to do
    }

    @Nonnull
    @Override
    V3 builder() {
        V3 result = OSFactory.builderV3();
        if(!isNullOrEmpty(domainName))
            result = result.domainName(domainName);
        if(project != null)
            result = result.scopeToProject(project);
        return result;
    }
}
