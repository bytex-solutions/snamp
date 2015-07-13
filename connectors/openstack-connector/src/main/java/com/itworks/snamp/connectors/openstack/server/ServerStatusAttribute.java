package com.itworks.snamp.connectors.openstack.server;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Server;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ServerStatusAttribute extends AbstractServerAttribute<String> {
    public static final String NAME = "status";
    static final String DESCRIPTION = "Server status";
    static final SimpleType<String> TYPE = SimpleType.STRING;

    public ServerStatusAttribute(final String serverID,
                                 final String attributeID,
                                 final AttributeDescriptor descriptor,
                                 final OSClient client) {
        super(serverID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static String getValueCore(final Server srv) {
        return srv.getStatus().value();
    }


    @Override
    protected String getValue(final Server srv) {
        return getValueCore(srv);
    }
}
