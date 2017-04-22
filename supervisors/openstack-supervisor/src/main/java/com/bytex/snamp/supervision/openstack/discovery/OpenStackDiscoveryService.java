package com.bytex.snamp.supervision.openstack.discovery;

import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.supervision.def.DefaultResourceDiscoveryService;
import org.openstack4j.api.senlin.SenlinClusterService;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;

/**
 * Represents discovery service based on Senlin.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */

public final class OpenStackDiscoveryService extends DefaultResourceDiscoveryService {
    private static final String SERVICE_NAME = "snamp-discovery-service";
    private static final String SERVICE_TYPE_NAME = "discovery-service";

    public OpenStackDiscoveryService(@Nonnull final String groupName) {
        super(groupName);
        //register SNAMP as service discovery (only at active cluster node)
    }

    void syncWithClusterNodes(final SenlinClusterService service){
        
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }
}
