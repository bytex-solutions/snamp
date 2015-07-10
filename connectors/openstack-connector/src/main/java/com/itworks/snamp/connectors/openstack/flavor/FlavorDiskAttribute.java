package com.itworks.snamp.connectors.openstack.flavor;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.openstack.OpenStackAbsentConfigurationParameterException;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Flavor;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class FlavorDiskAttribute extends AbstractFlavorAttribute<Integer> {
    public static final String NAME = "disk";
    static final String DESCRIPTION = "Reserved disk space";
    static final SimpleType<Integer> TYPE = SimpleType.INTEGER;

    public FlavorDiskAttribute(final String attributeID,
                               final AttributeDescriptor descriptor,
                               final OSClient client) throws OpenStackAbsentConfigurationParameterException{
        super(attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    @Override
    protected Integer getValue(final Flavor flavor) {
        return flavor.getDisk();
    }
}
