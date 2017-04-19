package com.bytex.snamp.supervision.openstack;

import org.openstack4j.api.senlin.SenlinClusterService;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface SenlinClusterServiceHandler {
    void handle(final String clusterID, final SenlinClusterService clusterService);
}
