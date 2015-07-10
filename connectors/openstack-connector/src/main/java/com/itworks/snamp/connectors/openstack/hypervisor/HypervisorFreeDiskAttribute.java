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
public final class HypervisorFreeDiskAttribute extends AbstractHypervisorAttribute<Integer> {
    public static final String NAME = "freeDisk";
    static final String DESCRIPTION = "Amount of free disk space";
    static final SimpleType<Integer> TYPE = SimpleType.INTEGER;

    public HypervisorFreeDiskAttribute(final String attributeID,
                                       final AttributeDescriptor descriptor,
                                       final OSClient client) throws OpenStackAbsentConfigurationParameterException {
        super(attributeID, DESCRIPTION, TYPE, descriptor, client);
    }


    @Override
    protected Integer getValue(final Hypervisor hv) {
        return hv.getFreeDisk();
    }
}
