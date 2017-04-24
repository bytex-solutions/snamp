package com.bytex.snamp.supervision.openstack.discovery;

import com.bytex.snamp.MapUtils;
import com.bytex.snamp.supervision.def.DefaultResourceDiscoveryService;
import com.bytex.snamp.supervision.discovery.ResourceDiscoveryException;
import com.bytex.snamp.supervision.openstack.ClusterNodes;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.openstack4j.api.senlin.SenlinNodeService;
import org.openstack4j.model.senlin.Node;

import javax.annotation.Nonnull;

/**
 * Represents discovery service based on Senlin.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */

public final class OpenStackDiscoveryService extends DefaultResourceDiscoveryService {
    private static final String CONNECTION_STRING_PARAM = "snampConnectionString";

    private final String clusterID;

    public OpenStackDiscoveryService(@Nonnull final String groupName,
                                     @Nonnull final String clusterID) {
        super(groupName);
        this.clusterID = clusterID;
    }

    /**
     * Imports nodes from cluster as a resources to SNAMP.
     * @param nodeService A client for retrieving nodes.
     * @param existingResources A set of existing resources.
     */
    public void synchronizeNodes(@Nonnull final SenlinNodeService nodeService, final ImmutableSet<String> existingResources) throws ResourceDiscoveryException {
        final ClusterNodes nodes = ClusterNodes.discover(nodeService, clusterID);
        //remove resource if they are not available as a node
        for (final String resourceName : existingResources)
            if (!nodes.names().contains(resourceName))
                removeResource(resourceName);
        //add resource if is not present as a node
        for (Node node : nodes.values()) {
            node = nodeService.get(node.getId());
            if (!existingResources.contains(node.getName())) {
                final String connectionString = MapUtils.getValue(node.getMetadata(), CONNECTION_STRING_PARAM, Object::toString).orElse("");
                registerResource(node.getName(), connectionString, Maps.transformValues(node.getMetadata(), Object::toString));
            }
        }
        nodes.clear();  //help GC
    }
}
