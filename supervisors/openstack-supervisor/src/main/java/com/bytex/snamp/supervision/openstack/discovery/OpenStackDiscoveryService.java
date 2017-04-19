package com.bytex.snamp.supervision.openstack.discovery;

import com.bytex.snamp.supervision.def.DefaultResourceDiscoveryService;
import com.bytex.snamp.supervision.openstack.SenlinClusterServiceHandler;
import org.openstack4j.api.senlin.SenlinClusterService;

import javax.annotation.Nonnull;

/**
 * Represents discovery service based on Senlin.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */

public final class OpenStackDiscoveryService extends DefaultResourceDiscoveryService implements SenlinClusterServiceHandler {
    public OpenStackDiscoveryService(@Nonnull final String groupName) {
        super(groupName);
    }

    @Override
    public void handle(final String clusterID, final SenlinClusterService clusterService) {

    }
}
