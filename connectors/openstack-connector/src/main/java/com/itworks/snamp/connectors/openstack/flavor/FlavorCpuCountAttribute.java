package com.itworks.snamp.connectors.openstack.flavor;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Flavor;

import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class FlavorCpuCountAttribute extends AbstractFlavorAttribute<Integer> {
    public static final String NAME = "CPU";
    static final String DESCRIPTION = "Number of reserved virtual CPUs";
    static final SimpleType<Integer> TYPE = SimpleType.INTEGER;

    public FlavorCpuCountAttribute(final String entityID,
                                   final String attributeID,
                                   final AttributeDescriptor descriptor,
                                   final OSClient client) {
        super(entityID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static int getValueCore(final Flavor flavor){
        return flavor.getVcpus();
    }

    @Override
    protected Integer getValue(final Flavor flavor) {
        return getValueCore(flavor);
    }
}