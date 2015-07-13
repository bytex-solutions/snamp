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
public final class HypervisorCpuModelAttribute extends AbstractHypervisorAttribute<String> {
    public static final String NAME = "cpuModel";
    static final String DESCRIPTION = "Model of the physical CPU";
    static final SimpleType<String> TYPE = SimpleType.STRING;

    public HypervisorCpuModelAttribute(final String hypervisorID,
                                       final String attributeID,
                                       final AttributeDescriptor descriptor,
                                       final OSClient client){
        super(hypervisorID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static String getValueCore(final Hypervisor hv){
        return hv.getCPUInfo().getModel();
    }

    @Override
    protected String getValue(final Hypervisor hv) {
        return getValueCore(hv);
    }
}
