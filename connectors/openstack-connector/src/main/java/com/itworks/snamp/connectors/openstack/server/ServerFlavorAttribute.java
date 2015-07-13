package com.itworks.snamp.connectors.openstack.server;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.openstack.flavor.FlavorAttribute;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Server;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;

/**
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class ServerFlavorAttribute extends AbstractServerAttribute<CompositeData> {
    public static final String NAME = "flavor";
    static final String DESCRIPTION = "Flavor used by the server";
    static final CompositeType TYPE = FlavorAttribute.TYPE;

    public ServerFlavorAttribute(final String serverID,
                                 final String attributeID,
                                 final AttributeDescriptor descriptor,
                                 final OSClient client){
        super(serverID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static CompositeData getValueCore(final Server srv) throws OpenDataException{
        return FlavorAttribute.getValueCore(srv.getFlavor());
    }

    @Override
    protected CompositeData getValue(final Server srv) throws OpenDataException {
        return getValueCore(srv);
    }
}
