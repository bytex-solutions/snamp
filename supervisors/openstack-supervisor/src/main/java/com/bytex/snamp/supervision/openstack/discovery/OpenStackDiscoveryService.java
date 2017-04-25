package com.bytex.snamp.supervision.openstack.discovery;

import com.bytex.snamp.json.JsonUtils;
import com.bytex.snamp.supervision.def.DefaultResourceDiscoveryService;
import com.bytex.snamp.supervision.discovery.ResourceDiscoveryException;
import com.bytex.snamp.supervision.openstack.ClusterNodes;
import com.google.common.collect.ImmutableSet;
import org.openstack4j.api.senlin.SenlinNodeService;
import org.openstack4j.model.senlin.Node;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.adaptors.ListAdaptor;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Represents discovery service based on Senlin.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */

public final class OpenStackDiscoveryService extends DefaultResourceDiscoveryService {
    private static final STGroup TEMPLATE_GROUP;

    static {
        TEMPLATE_GROUP = new STGroup('{', '}');
        ListAdaptor.register(TEMPLATE_GROUP);
    }

    private final String clusterID;
    private final ST connectionStringTemplate;      //pre-compiled template

    public OpenStackDiscoveryService(@Nonnull final String groupName,
                                     @Nonnull final String clusterID,
                                     final String connectionStringTemplate) {
        super(groupName);
        this.clusterID = clusterID;
        this.connectionStringTemplate = new ST(TEMPLATE_GROUP, connectionStringTemplate);
    }

    private String createConnectionString(final Node node){
        final Map<String, Object> nodeDetails = node.getDetails();
        nodeDetails.put("node", node.getMetadata());
        final ST connectionStringTemplate = new ST(this.connectionStringTemplate);
        nodeDetails.forEach(connectionStringTemplate::add);
        return connectionStringTemplate.render();
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
            if (!existingResources.contains(node.getName())) {
                node = nodeService.get(node.getId());//obtain detailed information about node
                final String connectionString = createConnectionString(node);
                registerResource(node.getName(), connectionString, JsonUtils.toPlainMap(node.getMetadata(), '.'));
            }
        }
        nodes.clear();  //help GC
    }
}
