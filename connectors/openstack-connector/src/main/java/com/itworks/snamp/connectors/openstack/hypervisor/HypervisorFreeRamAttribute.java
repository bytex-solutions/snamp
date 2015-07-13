package com.itworks.snamp.connectors.openstack.hypervisor;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.ext.Hypervisor;

import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class HypervisorFreeRamAttribute extends AbstractHypervisorAttribute<Integer> {
    public static final String NAME = "freeRAM";
    static final String DESCRIPTION = "Amount of available RAM";
    static final SimpleType<Integer> TYPE = SimpleType.INTEGER;

    public HypervisorFreeRamAttribute(final String entityID,
                                      final String attributeID,
                                      final AttributeDescriptor descriptor,
                                      final OSClient client) {
        super(entityID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static int getValueCore(final Hypervisor hv){
        return hv.getFreeRam();
    }

    @Override
    protected Integer getValue(final Hypervisor hv) {
        return getValueCore(hv);
    }
}
