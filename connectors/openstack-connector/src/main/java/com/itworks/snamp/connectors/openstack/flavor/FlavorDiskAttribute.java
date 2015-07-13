package com.itworks.snamp.connectors.openstack.flavor;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
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

    public FlavorDiskAttribute(final String entityID,
                               final String attributeID,
                               final AttributeDescriptor descriptor,
                               final OSClient client) {
        super(entityID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static int getValueCore(final Flavor flavor){
        return flavor.getDisk();
    }

    @Override
    protected Integer getValue(final Flavor flavor) {
        return getValueCore(flavor);
    }
}
