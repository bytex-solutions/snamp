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
public final class HypervisorCpuCoresAttribute extends AbstractHypervisorAttribute<Integer> {
    public static final String NAME = "cpuCores";
    static final String DESCRIPTION = "Number of physical CPU cores";
    static final SimpleType<Integer> TYPE = SimpleType.INTEGER;

    public HypervisorCpuCoresAttribute(final String hypervisorID,
                                       final String attributeID,
                                       final AttributeDescriptor descriptor,
                                       final OSClient client){
        super(hypervisorID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static int getValueCore(final Hypervisor hv){
        return hv.getCPUInfo().getTopology().getCores();
    }

    @Override
    protected Integer getValue(final Hypervisor hv) {
        return getValueCore(hv);
    }
}