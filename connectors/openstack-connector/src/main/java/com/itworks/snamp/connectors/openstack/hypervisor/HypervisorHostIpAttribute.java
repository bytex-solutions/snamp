package com.itworks.snamp.connectors.openstack.hypervisor;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.ext.Hypervisor;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class HypervisorHostIpAttribute extends AbstractHypervisorAttribute<String> {
    public static final String NAME = "hostIP";
    static final String DESCRIPTION = "IP address of the physical machine with hypervisor";
    static final SimpleType<String> TYPE = SimpleType.STRING;

    public HypervisorHostIpAttribute(final String entityID,
                                     final String attributeID,
                                     final AttributeDescriptor descriptor,
                                     final OSClient client) {
        super(entityID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static String getValueCore(final Hypervisor hv){
        return hv.getHostIP();
    }

    @Override
    protected String getValue(final Hypervisor hv) {
        return getValueCore(hv);
    }
}
