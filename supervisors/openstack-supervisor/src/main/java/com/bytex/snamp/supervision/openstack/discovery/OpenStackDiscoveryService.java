package com.bytex.snamp.supervision.openstack.discovery;

import com.bytex.snamp.json.JsonUtils;
import com.bytex.snamp.supervision.def.DefaultResourceDiscoveryService;
import com.bytex.snamp.supervision.discovery.ResourceDiscoveryException;
import com.bytex.snamp.supervision.openstack.ClusterNodes;
import org.openstack4j.api.senlin.SenlinNodeService;
import org.openstack4j.model.senlin.Node;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.adaptors.ListAdaptor;
import org.stringtemplate.v4.compiler.CompiledST;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

import static com.bytex.snamp.internal.Utils.callUnchecked;

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
    private final CompiledST connectionStringTemplate;      //pre-compiled template

    public OpenStackDiscoveryService(@Nonnull final String groupName,
                                     @Nonnull final String clusterID,
                                     final String connectionStringTemplate) {
        super(groupName);
        this.clusterID = clusterID;
        this.connectionStringTemplate = TEMPLATE_GROUP.compile(TEMPLATE_GROUP.getFileName(),
                "ConnectionStringTemplate",
                null,
                connectionStringTemplate,
                null);
        this.connectionStringTemplate.hasFormalArgs = false;
    }

    private ST createTemplateRenderer(){
        final CompiledST compiledST = callUnchecked(connectionStringTemplate::clone);
        return TEMPLATE_GROUP.createStringTemplate(compiledST);
    }

    private String createConnectionString(final Node node){
        final Map<String, Object> nodeDetails = node.getDetails();
        nodeDetails.put("node", node.getMetadata());
        final ST connectionStringTemplate = createTemplateRenderer();
        nodeDetails.forEach(connectionStringTemplate::add);
        return connectionStringTemplate.render();
    }

    /**
     * Imports nodes from cluster as a resources to SNAMP.
     * @param nodeService A client for retrieving nodes.
     * @param existingResources A set of existing resources.
     * @return {@literal true}, if resources are modified; otherwise, {@literal false}.
     */
    public boolean synchronizeNodes(@Nonnull final SenlinNodeService nodeService, final Set<String> existingResources) throws ResourceDiscoveryException {
        final ClusterNodes nodes = ClusterNodes.discover(nodeService, clusterID);
        boolean synchronizationOccured = false;
        //remove resource if they are not available as a node
        for (final String resourceName : existingResources)
            if (!nodes.names().contains(resourceName)) {
                removeResource(resourceName);
                synchronizationOccured = true;
            }
        //add resource if is not present as a node
        for (Node node : nodes.values()) {
            if (!existingResources.contains(node.getName())) {
                node = nodeService.get(node.getId());//obtain detailed information about node
                if (node == null || node.getDetails() == null) continue;
                final String connectionString = createConnectionString(node);
                registerResource(node.getName(), connectionString, JsonUtils.toPlainMap(node.getMetadata(), '.'));
                synchronizationOccured = true;
            }
        }
        nodes.clear();  //help GC
        return synchronizationOccured;
    }
}
