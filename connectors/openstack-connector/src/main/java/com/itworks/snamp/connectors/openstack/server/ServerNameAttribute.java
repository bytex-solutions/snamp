package com.itworks.snamp.connectors.openstack.server;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Server;

import javax.management.openmbean.SimpleType;

/**
 * Represents descriptive name of the server.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class ServerNameAttribute extends AbstractServerAttribute<String> {
    public static final String NAME = "serverName";
    static final String DESCRIPTION = "Descriptive name of the server";
    static final SimpleType<String> TYPE = SimpleType.STRING;

    public ServerNameAttribute(final String serverID,
                                     final String attributeID,
                                     final AttributeDescriptor descriptor,
                                     final OSClient client){
        super(serverID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static String getValueCore(final Server srv){
        return srv.getName();
    }

    @Override
    protected String getValue(final Server srv) {
        return getValueCore(srv);
    }
}
