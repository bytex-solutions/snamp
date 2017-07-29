package com.bytex.snamp.supervision.openstack;

import com.bytex.snamp.internal.AbstractKeyedObjects;
import org.openstack4j.api.senlin.SenlinNodeService;
import org.openstack4j.model.senlin.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents collection of cluster nodes.
 * <p>
 *     Key is a node ID
 *     Value is a node instance
 * @author Roman Sakno
 * @since 2.0
 * @version 2.1
 */
public final class ClusterNodes extends AbstractKeyedObjects<String, Node> {
    private static final long serialVersionUID = 4696054938546279494L;
    private final Map<String, String> nameToIdMap;

    private ClusterNodes(){
        nameToIdMap = new HashMap<>();
    }

    public Optional<Node> getByName(final String name){
        final String nodeID = nameToIdMap.get(name);
        return isNullOrEmpty(nodeID) ? Optional.empty() : Optional.ofNullable(get(nodeID));
    }

    public Set<String> names() {
        return nameToIdMap.keySet();
    }

    public Set<String> ids(){
        return keySet();
    }

    public Optional<Node> getByID(final String id) {
        return Optional.ofNullable(get(id));
    }

    @Override
    public void clear() {
        nameToIdMap.clear();
        super.clear();
    }

    @Override
    public Node put(final Node node) {
        nameToIdMap.put(node.getName(), node.getId());
        return super.put(node);
    }

    public static ClusterNodes discover(final SenlinNodeService nodeService,
                                        final String clusterId) {
        final ClusterNodes nodes = new ClusterNodes();
        nodeService.list()
                .stream()
                .filter(node -> node.getClusterID().equals(clusterId))
                .forEach(nodes::put);
        return nodes;
    }

    @Override
    public String getKey(final Node node) {
        return node.getId();
    }
}
