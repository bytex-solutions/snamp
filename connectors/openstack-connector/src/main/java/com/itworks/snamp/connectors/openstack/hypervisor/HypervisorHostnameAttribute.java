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
public final class HypervisorHostnameAttribute extends AbstractHypervisorAttribute<String> {
    public static final String NAME = "hostname";
    static final String DESCRIPTION = "Name of the host with hypervisor";
    static final SimpleType<String> TYPE = SimpleType.STRING;

    public HypervisorHostnameAttribute(final String entityID,
                                       final String attributeID,
                                       final AttributeDescriptor descriptor,
                                       final OSClient client) {
        super(entityID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static String getValueCore(final Hypervisor hv){
        return hv.getHypervisorHostname();
    }

    @Override
    protected String getValue(final Hypervisor hv) {
        return getValueCore(hv);
    }
}