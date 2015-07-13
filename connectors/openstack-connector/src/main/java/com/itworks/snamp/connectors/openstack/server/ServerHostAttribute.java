package com.itworks.snamp.connectors.openstack.server;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Server;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class ServerHostAttribute extends AbstractServerAttribute<String> {
    public static final String NAME = "host";
    static final String DESCRIPTION = "Server hostname";
    static final SimpleType<String> TYPE = SimpleType.STRING;

    public ServerHostAttribute(final String serverID,
                               final String attributeID,
                               final AttributeDescriptor descriptor,
                               final OSClient client){
        super(serverID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static String getValueCore(final Server srv){
        return srv.getHost();
    }

    @Override
    protected String getValue(final Server srv) {
        return getValueCore(srv);
    }
}
