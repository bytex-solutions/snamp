package com.bytex.snamp.supervision.openstack;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.openstack4j.api.senlin.SenlinClusterService;
import org.openstack4j.api.senlin.SenlinNodeService;
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

    //key - node ID
    //value - node name
    public static BiMap<String, String> getNodes(final SenlinNodeService nodeService,
                                          final String clusterId){
        final BiMap<String, String> result = HashBiMap.create();
        nodeService.list()
                .stream()
                .filter(node -> node.getClusterID().equals(clusterId))
                .forEach(node -> result.put(node.getId(), node.getName()));
        return result;
    }
}
