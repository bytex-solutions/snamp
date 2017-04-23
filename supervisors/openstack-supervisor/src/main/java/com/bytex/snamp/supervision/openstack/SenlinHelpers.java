package com.bytex.snamp.supervision.openstack;

import org.openstack4j.api.senlin.SenlinClusterService;
import org.openstack4j.model.senlin.Cluster;

import java.util.Optional;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class SenlinHelpers {
    static Optional<String> getClusterIdByName(final SenlinClusterService clusterService,
                                               final String clusterName){
        return clusterService.list()
                .stream()
                .filter(cluster -> cluster.getName().equals(clusterName))
                .map(Cluster::getId)
                .findFirst();
    }
}
