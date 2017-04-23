package com.bytex.snamp.supervision.openstack.discovery;

import com.bytex.snamp.supervision.def.DefaultResourceDiscoveryService;

import javax.annotation.Nonnull;

/**
 * Represents discovery service based on Senlin.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */

public final class OpenStackDiscoveryService extends DefaultResourceDiscoveryService {
    private final String clusterID;

    public OpenStackDiscoveryService(@Nonnull final String groupName,
                                     @Nonnull final String clusterID) {
        super(groupName);
        this.clusterID = clusterID;
    }
}
