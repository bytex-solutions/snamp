package com.itworks.snamp.connectors.openstack.hypervisor;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.openstack.OpenStackAbsentConfigurationParameterException;
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

    public HypervisorHostIpAttribute(final String attributeID,
                                     final AttributeDescriptor descriptor,
                                     final OSClient client) throws OpenStackAbsentConfigurationParameterException{
        super(attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    @Override
    protected String getValue(final Hypervisor hv) throws Exception {
        return hv.getHostIP();
    }
}
