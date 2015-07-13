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
public final class HypervisorCpuVendorAttribute extends AbstractHypervisorAttribute<String> {
    public static final String NAME = "cpuVendor";
    static final String DESCRIPTION = "Manufacturer of physical CPU";
    static final SimpleType<String> TYPE = SimpleType.STRING;

    public HypervisorCpuVendorAttribute(final String hypervisorID,
                                        final String attributeID,
                                        final AttributeDescriptor descriptor,
                                        final OSClient client){
        super(hypervisorID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static String getValueCore(final Hypervisor hv){
        return hv.getCPUInfo().getVendor();
    }

    @Override
    protected String getValue(final Hypervisor hv) {
        return getValueCore(hv);
    }
}
