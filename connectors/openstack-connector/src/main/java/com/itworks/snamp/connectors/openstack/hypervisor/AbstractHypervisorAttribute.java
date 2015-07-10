package com.itworks.snamp.connectors.openstack.hypervisor;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.openstack.OpenStackAbsentConfigurationParameterException;
import com.itworks.snamp.connectors.openstack.OpenStackResourceAttribute;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.compute.ext.HypervisorService;
import org.openstack4j.model.compute.ext.Hypervisor;

import javax.management.MBeanException;
import javax.management.openmbean.OpenType;
import java.util.List;
import java.util.Objects;

import static com.itworks.snamp.connectors.openstack.OpenStackResourceConnectorConfigurationDescriptor.getEntityID;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractHypervisorAttribute<T> extends OpenStackResourceAttribute<T, HypervisorService> {
    private final String hypervisorID;

    AbstractHypervisorAttribute(final String attributeID,
                                final String description,
                                final OpenType<T> attributeType,
                                final AttributeDescriptor descriptor,
                                final OSClient client) throws OpenStackAbsentConfigurationParameterException{
        super(attributeID, description, attributeType, AttributeSpecifier.READ_ONLY, descriptor, client.compute().hypervisors());
        hypervisorID = getEntityID(descriptor);
    }

    private Hypervisor getHypervisor(final String hypervisorID){
        for(final Hypervisor hv: openStackService.list())
            if(Objects.equals(hypervisorID, hv.getId()))
                return hv;
        return null;
    }

    protected abstract T getValue(final Hypervisor hv) throws Exception;

    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws Exception Unable to read attribute value.
     */
    @Override
    public final T getValue() throws Exception {
        final Hypervisor hv = getHypervisor(hypervisorID);
        if(hv == null) throw new MBeanException(new IllegalArgumentException(String.format("Hypervisor '%s' doesn't exist", hypervisorID)));
        else return getValue(hv);
    }

    /**
     * Sets value of this attribute.
     *
     * @param value The value of this attribute.
     * @throws Exception Unable to write attribute value.
     */
    @Override
    public void setValue(final T value) throws Exception {
        throw new MBeanException(new UnsupportedOperationException("Attribute is read-only"));
    }
}
