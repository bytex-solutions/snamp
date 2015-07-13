package com.itworks.snamp.connectors.openstack.server;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Server;

import javax.management.openmbean.SimpleType;
import java.util.Date;

/**
 * Represents server launching date and time.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class ServerLaunchedAtAttribute extends AbstractServerAttribute<Date> {
    public static final String NAME = "launchedAt";
    static final String DESCRIPTION = "Launching time of the server";
    static final SimpleType<Date> TYPE = SimpleType.DATE;

    public ServerLaunchedAtAttribute(final String serverID,
                                       final String attributeID,
                                       final AttributeDescriptor descriptor,
                                       final OSClient client) {
        super(serverID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static Date getValueCore(final Server srv) {
        return srv.getLaunchedAt();
    }

    @Override
    protected Date getValue(final Server srv) {
        return getValueCore(srv);
    }
}
