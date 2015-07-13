package com.itworks.snamp.connectors.openstack.hypervisor;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.openstack.OpenStackResourceAttribute;
import com.itworks.snamp.jmx.CompositeTypeBuilder;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.compute.ext.HypervisorService;
import org.openstack4j.model.compute.ext.Hypervisor;

import javax.management.MBeanException;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import java.util.List;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class AllHypervisorsAttribute extends OpenStackResourceAttribute<CompositeData[], HypervisorService> {
    public static final String NAME = "hypervisors";
    static final String DESCRIPTION = "All available hypervisors";
    static final ArrayType<CompositeData[]> TYPE;

    static{
        try {
            TYPE = new ArrayType<>(1, HypervisorAttribute.TYPE);
        } catch (final OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public AllHypervisorsAttribute(final String attributeID,
                                   final AttributeDescriptor descriptor,
                                   final OSClient client){
        super(attributeID, DESCRIPTION, TYPE, AttributeSpecifier.READ_ONLY, descriptor, client.compute().hypervisors());
    }

    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws OpenDataException Unable to read attribute value.
     */
    @Override
    public CompositeData[] getValue() throws OpenDataException {
        final List<? extends Hypervisor> visors = openStackService.list();
        final CompositeData[] result = new CompositeData[visors.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = HypervisorAttribute.getValueCore(visors.get(i));
        return result;
    }
}
