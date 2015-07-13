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
public final class FlavorRamAttribute extends AbstractFlavorAttribute<Integer> {
    public static final String NAME = "RAM";
    static final String DESCRIPTION = "Available Random Access Memory";
    static final SimpleType<Integer> TYPE = SimpleType.INTEGER;

    public FlavorRamAttribute(final String entityID,
                              final String attributeID,
                              final AttributeDescriptor descriptor,
                              final OSClient client){
        super(entityID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static int getValueCore(final Flavor flavor){
        return flavor.getRam();
    }

    @Override
    protected Integer getValue(final Flavor flavor) {
        return getValueCore(flavor);
    }
}
