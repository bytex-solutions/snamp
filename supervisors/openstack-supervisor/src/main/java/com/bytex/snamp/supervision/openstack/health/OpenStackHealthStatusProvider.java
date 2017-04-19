package com.bytex.snamp.supervision.openstack.health;

import com.bytex.snamp.supervision.def.DefaultHealthStatusProvider;
import com.bytex.snamp.supervision.openstack.SenlinClusterServiceHandler;
import org.openstack4j.api.senlin.SenlinClusterService;
import org.openstack4j.model.senlin.Cluster;

import javax.annotation.Nonnull;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class OpenStackHealthStatusProvider extends DefaultHealthStatusProvider implements SenlinClusterServiceHandler {
    public OpenStackHealthStatusProvider(@Nonnull final String groupName) {
        super(groupName);
    }

    @Override
    public void handle(final String clusterID, final SenlinClusterService clusterService) {
        final Cluster cluster = clusterService.get(clusterID);

    }
}
