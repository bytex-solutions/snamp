package org.openstack4j.openstack.senlin.internal.bugfixed;

import org.openstack4j.model.senlin.Node;
import org.openstack4j.openstack.senlin.domain.SenlinNode;
import org.openstack4j.openstack.senlin.internal.SenlinNodeServiceImpl;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class BugFixedSenlinNodeServiceImpl extends SenlinNodeServiceImpl {
    @Override
    public Node get(final String nodeID) {
        return get(SenlinNode.class, uri("/nodes/%s", nodeID)).param("show_details", true).execute();
    }
}
