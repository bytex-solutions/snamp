package com.itworks.snamp.connectors.openstack.server;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.openstack.OpenStackResourceAttribute;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.compute.ComputeService;
import org.openstack4j.api.compute.ServerService;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.image.Image;

import javax.management.MBeanException;
import javax.management.openmbean.OpenType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractServerAttribute<T> extends OpenStackResourceAttribute<T, ComputeService> {
    protected final String serverID;

    AbstractServerAttribute(final String serverID,
                           final String attributeID,
                           final String description,
                           final OpenType<T> attributeType,
                           final AttributeDescriptor descriptor,
                           final OSClient client) {
        super(attributeID, description, attributeType, AttributeSpecifier.READ_ONLY, descriptor, client.compute());
        this.serverID = serverID;
    }

    protected abstract T getValue(final Server srv) throws Exception;

    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws Exception Unable to read attribute value.
     */
    @Override
    public final T getValue() throws Exception {
        final Server srv = openStackService.servers().get(serverID);
        if(srv == null) throw new MBeanException(new IllegalArgumentException(String.format("Image '%s' doesn't exist", srv)));
        else return getValue(srv);
    }

    /**
     * Sets value of this attribute.
     *
     * @param value The value of this attribute.
     * @throws Exception Unable to write attribute value.
     */
    @Override
    public void setValue(final T value) throws Exception {
        throw new MBeanException(new UnsupportedOperationException("Attribute is read-only"));
    }
}
